package com.example.slotassistant.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.slotassistant.utils.PreferencesManager

/**
 * Kurye uygulamasını izleyen ve otomatik slot seçimi yapan Accessibility Service
 * 
 * Bu servis şunları yapar:
 * 1. Kurye uygulamasının açılışını tespit eder
 * 2. Giriş ekranını bulur ve otomatik giriş yapar
 * 3. Slot seçim ekranını bulur
 * 4. Tercih edilen slotları otomatik seçer
 */
class CourierAccessibilityService : AccessibilityService() {
    
    companion object {
        private const val TAG = "CourierAccessibility"
        
        // DEBUG MOD - Tüm UI elementlerini loglamak için true yap
        // Kurye uygulamanızın yapısını öğrendikten sonra false yapın
        private const val DEBUG_MODE = true // ✅ Test için açık
        
        // İnsan benzeri davranış için rastgele gecikme aralıkları (ms)
        private const val MIN_HUMAN_DELAY = 800L
        private const val MAX_HUMAN_DELAY = 2500L
        private const val MIN_TYPING_DELAY = 100L
        private const val MAX_TYPING_DELAY = 300L
    }
    
    private lateinit var preferencesManager: PreferencesManager
    private var courierAppPackage: String = "com.logistics.rider.yemeksepeti" // Varsayılan
    private var isLoginAttempted = false
    private var isSlotSelectionActive = false
    private var isNavigatingToSlots = false
    private var isWaitingForConfirmation = false
    private var isDaySelected = false // Gün seçildi mi?
    private var lastNavigationAttempt = 0L
    private val NAVIGATION_COOLDOWN = 5000L // 5 saniye cooldown
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        preferencesManager = PreferencesManager(applicationContext)
        
        // Paket adını yükle
        courierAppPackage = preferencesManager.getCourierAppPackage()
        if (DEBUG_MODE) Log.d(TAG, "Kurye uygulaması paket adı: $courierAppPackage")
        
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or 
                        AccessibilityEvent.TYPE_VIEW_CLICKED or
                        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            packageNames = arrayOf(courierAppPackage)
        }
        
        serviceInfo = info
        if (DEBUG_MODE) Log.d(TAG, "Accessibility Service başlatıldı - İzlenen paket: $courierAppPackage")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        
        // Paket adını kontrol et
        if (event.packageName != courierAppPackage) {
            if (DEBUG_MODE && event.packageName != null) {
                Log.d(TAG, "Farklı uygulama tespit edildi: ${event.packageName} (Beklenen: $courierAppPackage)")
            }
            return
        }
        
        val rootNode = rootInActiveWindow ?: return
        
        try {
            // DEBUG: Tüm ekran yapısını logla
            if (DEBUG_MODE) {
                when (event.eventType) {
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                        Log.d(TAG, "=== YENİ EKRAN TESPİT EDİLDİ ===")
                        Log.d(TAG, "Paket: ${event.packageName}")
                        Log.d(TAG, "Sınıf: ${event.className}")
                        logNodeHierarchy(rootNode, 0)
                        Log.d(TAG, "=== EKRAN YAPISI BİTTİ ===")
                    }
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                        // WebView içeriği değiştiğinde (günler, slotlar yüklendiğinde)
                        if (event.className?.contains("WebView") == true || 
                            rootNode.findAccessibilityNodeInfosByViewId("com.logistics.rider.yemeksepeti:id/webView").isNotEmpty()) {
                            Log.d(TAG, "📱 WEBVIEW İÇERİK DEĞİŞİMİ 📱")
                            logNodeHierarchy(rootNode, 0)
                            Log.d(TAG, "📱 WEBVIEW İÇERİK BİTTİ 📱")
                        }
                    }
                }
            }
            
            when {
                // Giriş ekranı tespit edildi
                isLoginScreen(rootNode) && !isLoginAttempted -> {
                    if (DEBUG_MODE) Log.d(TAG, "Giriş ekranı tespit edildi")
                    performAutoLogin(rootNode)
                }
                
                // Onay ekranı - Slot ayırt butonu
                isWaitingForConfirmation && isConfirmationDialog(rootNode) -> {
                    if (DEBUG_MODE) Log.d(TAG, "Onay ekranı tespit edildi")
                    confirmSlotReservation(rootNode)
                }
                
                // Ana sayfa - slot ekranına git
                isHomeScreen(rootNode) && !isNavigatingToSlots -> {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastNavigationAttempt > NAVIGATION_COOLDOWN) {
                        if (DEBUG_MODE) Log.d(TAG, "Ana sayfa tespit edildi - Slot ekranına gidiliyor")
                        navigateToSlotScreen(rootNode)
                        lastNavigationAttempt = currentTime
                    }
                }
                
                // Slot seçim ekranı tespit edildi
                isSlotSelectionScreen(rootNode) -> {
                    if (DEBUG_MODE) Log.d(TAG, "Slot seçim ekranı tespit edildi")
                    isNavigatingToSlots = false
                    
                    // Slot ekranına yeni girildiğinde gün seçimini resetle
                    if (!isSlotSelectionActive) {
                        isDaySelected = false
                        if (DEBUG_MODE) Log.d(TAG, "Gün seçimi resetlendi (yeni ekran)")
                    }
                    
                    // Önce günü seç, sonra slotu seç
                    if (!isDaySelected) {
                        selectPreferredDay(rootNode)
                    } else {
                        performSlotSelection(rootNode)
                    }
                }
                
                // Ana sayfa - giriş başarılı
                event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    isLoginAttempted = false // Reset login flag
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Hata: ${e.message}", e)
        } finally {
            rootNode.recycle()
        }
    }
    
    /**
     * Giriş ekranını kontrol eder
     */
    private fun isLoginScreen(rootNode: AccessibilityNodeInfo): Boolean {
        // Giriş ekranını tanımak için kullanılabilecek kelimeler
        val loginKeywords = listOf("giriş", "login", "şifre", "password", "kurye id", "courier")
        
        return findNodesByText(rootNode, loginKeywords).isNotEmpty()
    }
    
    /**
     * Ana sayfa kontrolü
     */
    private fun isHomeScreen(rootNode: AccessibilityNodeInfo): Boolean {
        // Ana sayfayı tanımak için kullanılabilecek kelimeler
        val homeKeywords = listOf("ana sayfa", "home", "dashboard", "anasayfa", "hoşgeldin", "welcome", "siparişler", "orders")
        
        return findNodesByText(rootNode, homeKeywords).isNotEmpty() && 
               !isLoginScreen(rootNode) && 
               !isSlotSelectionScreen(rootNode)
    }
    
    /**
     * Slot seçim ekranını kontrol eder
     */
    private fun isSlotSelectionScreen(rootNode: AccessibilityNodeInfo): Boolean {
        // Slot ekranını tanımak için kullanılabilecek kelimeler
        // Loglardan gördük: "Müsait slotlar", "Ayırt", "Yaklaşan Slotlar"
        val slotKeywords = listOf("Müsait slot", "Yaklaşan slot", "Ayırt", "March 2026")
        
        return findNodesByText(rootNode, slotKeywords).isNotEmpty()
    }
    
    /**
     * Onay ekranını (dialog) kontrol eder
     */
    private fun isConfirmationDialog(rootNode: AccessibilityNodeInfo): Boolean {
        // Onay ekranında olabilecek kelimeler
        val confirmKeywords = listOf("slot ayırt", "onayla", "confirm", "rezervasyon", "emin misiniz")
        
        return findNodesByText(rootNode, confirmKeywords).isNotEmpty()
    }
    
    /**
     * Tercih edilen günü seçer
     * Kullanıcının seçtiği hafta (0=bu hafta, 1=gelecek hafta) ve günleri kullanır
     */
    private fun selectPreferredDay(rootNode: AccessibilityNodeInfo) {
        // Kullanıcının seçtiği hafta (0=bu hafta, 1=gelecek hafta)
        val weeksAhead = preferencesManager.getWeeksAhead()
        
        // Tercih edilen günleri al
        val selectedDays = preferencesManager.getSelectedDays()
        
        if (DEBUG_MODE) {
            val weekText = if (weeksAhead == 0) "Bu hafta" else "Gelecek hafta"
            Log.d(TAG, "=== GÜN SEÇİMİ DEBUG ===")
            Log.d(TAG, "PreferencesManager.getWeeksAhead() = $weeksAhead")
            Log.d(TAG, "Hafta metni: $weekText")
            Log.d(TAG, "Seçili günler: $selectedDays")
        }
        
        if (selectedDays.isEmpty()) {
            if (DEBUG_MODE) Log.w(TAG, "Seçili gün yok, mevcut gün kullanılacak")
            isDaySelected = true
            return
        }
        
        // İlk seçili günü kullan
        val firstSelectedDay = selectedDays.first()
        
        // WorkDay'i gün kısaltmasına çevir
        val dayOfWeek = when (firstSelectedDay) {
            "MONDAY" -> "Pt"
            "TUESDAY" -> "Sa"
            "WEDNESDAY" -> "Ça"
            "THURSDAY" -> "Pe"
            "FRIDAY" -> "Cu"
            "SATURDAY" -> "Ct"
            "SUNDAY" -> "Pz"
            else -> ""
        }
        
        if (dayOfWeek.isEmpty()) {
            if (DEBUG_MODE) Log.w(TAG, "Geçersiz gün: $firstSelectedDay")
            isDaySelected = true
            return
        }
        
        // Hedef tarihi hesapla
        val targetDateText = calculateTargetDate(dayOfWeek, weeksAhead)
        
        if (targetDateText.isEmpty()) {
            if (DEBUG_MODE) Log.w(TAG, "❌ Hedef tarih hesaplanamadı")
            isDaySelected = true
            return
        }
        
        if (DEBUG_MODE) Log.d(TAG, "🎯 Aranan gün butonu: '$targetDateText' ($firstSelectedDay)")
        
        // Gün butonunu bul (örn: "Cu 20")
        val dayButton = findNodesByText(rootNode, listOf(targetDateText)).firstOrNull { 
            it.isClickable && it.isEnabled 
        }
        
        if (dayButton != null) {
            humanDelay()
            dayButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            isDaySelected = true
            if (DEBUG_MODE) Log.d(TAG, "✅ Gün butonu tıklandı: $targetDateText")
        } else {
            if (DEBUG_MODE) Log.w(TAG, "❌ Gün butonu bulunamadı: $targetDateText (mevcut gün kullanılacak)")
            isDaySelected = true
        }
    }
    
    /**
     * Tercih edilen gün ve hafta bilgisinden hedef tarihi hesaplar
     * @param dayOfWeek Gün kısaltması: "Cu", "Pe", "Ct" vb.
     * @param weeksAhead Kaç hafta sonrası: 0 = bu hafta, 1 = gelecek hafta
     * @return Buton metni (örn: "Cu 20") veya boş string
     */
    private fun calculateTargetDate(dayOfWeek: String, weeksAhead: Int): String {
        try {
            // Gün kısaltmalarını Java Calendar.DAY_OF_WEEK değerleriyle eşle
            val dayMap = mapOf(
                "Pz" to java.util.Calendar.SUNDAY,    // 1
                "Pt" to java.util.Calendar.MONDAY,    // 2
                "Sa" to java.util.Calendar.TUESDAY,   // 3
                "Ça" to java.util.Calendar.WEDNESDAY, // 4
                "Pe" to java.util.Calendar.THURSDAY,  // 5
                "Cu" to java.util.Calendar.FRIDAY,    // 6
                "Ct" to java.util.Calendar.SATURDAY   // 7
            )
            
            val targetDayOfWeek = dayMap[dayOfWeek] ?: return ""
            
            // Bugünün tarihi
            val calendar = java.util.Calendar.getInstance()
            val today = calendar.get(java.util.Calendar.DAY_OF_WEEK)
            val todayDate = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            
            // Hedef güne kadar kaç gün var
            var daysToAdd = (targetDayOfWeek - today + 7) % 7
            if (daysToAdd == 0 && weeksAhead > 0) {
                daysToAdd = 7 // Aynı gün ama gelecek hafta
            }
            
            // Hafta sayısını ekle
            daysToAdd += (weeksAhead * 7)
            
            // Tarihi hesapla
            calendar.add(java.util.Calendar.DAY_OF_MONTH, daysToAdd)
            val targetDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            
            if (DEBUG_MODE) {
                Log.d(TAG, "📅 Tarih hesaplama: Bugün=$todayDate, Hedef gün=$dayOfWeek, Eklenecek gün=$daysToAdd, Sonuç=$targetDay")
            }
            
            // Buton metni: "Cu 20"
            return "$dayOfWeek $targetDay"
            
        } catch (e: Exception) {
            if (DEBUG_MODE) Log.e(TAG, "Tarih hesaplama hatası: ${e.message}")
            return ""
        }
    }
    
    /**
     * Slot rezervasyonunu onaylar
     */
    private fun confirmSlotReservation(rootNode: AccessibilityNodeInfo) {
        // "Slot ayırt", "Onayla", "Confirm" gibi butonları ara
        val confirmButton = findNodesByText(rootNode, listOf("slot ayırt", "onayla", "confirm", "tamam", "ok")).firstOrNull { node ->
            node.isClickable || node.parent?.isClickable == true || node.parent?.parent?.isClickable == true
        }
        
        if (confirmButton != null) {
            // Tıklanabilir parent'ı bul
            val clickableNode = when {
                confirmButton.isClickable -> confirmButton
                confirmButton.parent?.isClickable == true -> confirmButton.parent
                confirmButton.parent?.parent?.isClickable == true -> confirmButton.parent?.parent
                else -> null
            }
            
            clickableNode?.let {
                humanDelay()
                it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                if (DEBUG_MODE) Log.d(TAG, "✅ Slot rezervasyonu onaylandı!")
                isWaitingForConfirmation = false
                isSlotSelectionActive = false
            }
        } else {
            if (DEBUG_MODE) Log.w(TAG, "Onay butonu bulunamadı")
        }
    }
    
    /**
     * Slot seçim ekranına navigasyon yapar
     */
    private fun navigateToSlotScreen(rootNode: AccessibilityNodeInfo) {
        isNavigatingToSlots = true
        
        // Loglardan gördük: "Müsait slotları görüntüle" butonu var
        val viewSlotsButton = findNodesByText(rootNode, listOf("müsait slotları görüntüle")).firstOrNull { node ->
            node.isClickable || node.parent?.isClickable == true || node.parent?.parent?.isClickable == true
        }
        
        if (viewSlotsButton != null) {
            // Tıklanabilir parent'ı bul
            val clickableNode = when {
                viewSlotsButton.isClickable -> viewSlotsButton
                viewSlotsButton.parent?.isClickable == true -> viewSlotsButton.parent
                viewSlotsButton.parent?.parent?.isClickable == true -> viewSlotsButton.parent?.parent
                else -> null
            }
            
            clickableNode?.let {
                humanDelay()
                it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                if (DEBUG_MODE) Log.d(TAG, "Müsait slotları görüntüle butonu tıklandı")
            }
            return
        }
        
        // Alternatif: Yan menüden "Müsait slotlar" seçeneği
        val menuSlotOption = findNodesByText(rootNode, listOf("müsait slotlar")).firstOrNull { node ->
            node.isClickable || node.parent?.isClickable == true
        }
        
        if (menuSlotOption != null) {
            val clickableNode = if (menuSlotOption.isClickable) menuSlotOption else menuSlotOption.parent
            clickableNode?.let {
                humanDelay()
                it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                if (DEBUG_MODE) Log.d(TAG, "Menüden müsait slotlar seçildi")
            }
        } else {
            if (DEBUG_MODE) Log.w(TAG, "Slot ekranına gitme butonu bulunamadı")
        }
    }
    
    /**
     * Otomatik giriş yapar
     */
    private fun performAutoLogin(rootNode: AccessibilityNodeInfo) {
        val courierId = preferencesManager.getCourierId()
        val password = preferencesManager.getCourierPassword()
        
        if (courierId.isEmpty() || password.isEmpty()) {
            if (DEBUG_MODE) Log.w(TAG, "Kurye bilgileri eksik, giriş yapılamıyor")
            return
        }
        
        // ID alanını bul (genellikle "id", "kullanıcı", "kurye" gibi kelimeler içerir)
        val idField = findNodesByText(rootNode, listOf("id", "kullanıcı", "kurye")).firstOrNull {
            it.className == "android.widget.EditText"
        }
        
        // Şifre alanını bul
        val passwordField = findNodesByText(rootNode, listOf("şifre", "password")).firstOrNull {
            it.className == "android.widget.EditText"
        }
        
        // Giriş butonunu bul
        val loginButton = findNodesByText(rootNode, listOf("giriş", "login", "onayla")).firstOrNull {
            it.isClickable
        }
        
        // Alanları insan gibi doldur (rastgele gecikmelerle)
        idField?.let {
            humanDelay()
            fillTextFieldHumanLike(it, courierId)
            if (DEBUG_MODE) Log.d(TAG, "Kurye ID girildi")
        }
        
        passwordField?.let {
            humanDelay()
            fillTextFieldHumanLike(it, password)
            if (DEBUG_MODE) Log.d(TAG, "Şifre girildi")
        }
        
        // İnsan gibi bekle ve giriş yap
        loginButton?.let {
            humanDelay(MIN_HUMAN_DELAY + 500, MAX_HUMAN_DELAY + 1000) // Daha uzun düşünme süresi
            it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            isLoginAttempted = true
            if (DEBUG_MODE) Log.d(TAG, "Giriş butonu tıklandı")
        }
    }
    
    /**
     * Slot seçimi yapar
     */
    private fun performSlotSelection(rootNode: AccessibilityNodeInfo) {
        if (isSlotSelectionActive) return // Çift işlem önleme
        
        isSlotSelectionActive = true
        
        try {
            val timePreferences = preferencesManager.getTimePreferences()
            
            if (timePreferences.isEmpty()) {
                if (DEBUG_MODE) Log.w(TAG, "Tercih edilen slot yok")
                return
            }
            
            if (DEBUG_MODE) Log.d(TAG, "Tercih edilen slotlar: ${timePreferences.map { "${it.startTime}-${it.endTime}" }}")
            
            // Her bir tercih için slot ara
            for (preference in timePreferences) {
                if (DEBUG_MODE) Log.d(TAG, "Aranan slot: ${preference.startTime} - ${preference.endTime}")
                
                // Başlangıç ve bitiş saatlerini ayrı ayrı bul
                // Kurye uygulamasında slotlar üç ayrı TextView: "23:45", " – ", "01:15"
                val startTimeNodes = findNodesByExactText(rootNode, preference.startTime)
                
                if (DEBUG_MODE) Log.d(TAG, "Başlangıç saati '${preference.startTime}' için ${startTimeNodes.size} node bulundu")
                
                for (startNode in startTimeNodes) {
                    // Başlangıç saatinin yakınında bitiş saatini ara
                    val endTimeNode = findEndTimeNearStartTime(startNode, preference.endTime)
                    
                    if (endTimeNode != null) {
                        if (DEBUG_MODE) Log.d(TAG, "Eşleşen slot bulundu: ${preference.startTime} - ${preference.endTime}")
                        
                        // Bu slotun "Ayırt" butonunu bul
                        val reserveButton = findReserveButtonForTimeSlot(startNode, endTimeNode)
                        
                        if (reserveButton != null) {
                            if (DEBUG_MODE) {
                                // TEST MODU: Sadece bul, tıklama
                                Log.d(TAG, "✅ SLOT BULUNDU (TEST MODU): ${preference.startTime} - ${preference.endTime}")
                                Log.d(TAG, "⚠️ DEBUG_MODE=true olduğu için ayırt butonu tıklanmıyor")
                                Log.d(TAG, "📌 Otomatik ayırt için DEBUG_MODE=false yapın")
                            } else {
                                // CANLI MOD: Ayırt et
                                humanDelay()
                                reserveButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                Log.d(TAG, "Ayırt butonu tıklandı: ${preference.startTime} - ${preference.endTime}")
                                
                                // Onay ekranını bekle
                                isWaitingForConfirmation = true
                                Log.d(TAG, "Onay ekranı bekleniyor...")
                            }
                            
                            // İlk eşleşen slotu bulduk, çık
                            return
                        } else {
                            if (DEBUG_MODE) Log.w(TAG, "Slot bulundu ama Ayırt butonu bulunamadı")
                        }
                    }
                }
            }
            
            if (DEBUG_MODE) Log.w(TAG, "Hiçbir tercih edilen slot bulunamadı")
            
        } finally {
            // 3 saniye sonra reset et (tekrar seçim yapabilmek için)
            Thread.sleep(3000)
            isSlotSelectionActive = false
            isDaySelected = false // Gün seçimini resetle
        }
    }
    
    /**
     * Tam olarak verilen metni içeren node'ları bulur
     */
    private fun findNodesByExactText(node: AccessibilityNodeInfo, searchText: String): List<AccessibilityNodeInfo> {
        val results = mutableListOf<AccessibilityNodeInfo>()
        findNodesByExactTextRecursive(node, searchText, results)
        return results
    }
    
    private fun findNodesByExactTextRecursive(node: AccessibilityNodeInfo, searchText: String, results: MutableList<AccessibilityNodeInfo>) {
        node.text?.toString()?.let { text ->
            if (text.trim() == searchText) {
                results.add(node)
            }
        }
        
        // Child node'ları kontrol et
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                findNodesByExactTextRecursive(child, searchText, results)
            }
        }
    }
    
    /**
     * Başlangıç saati node'unun yakınında bitiş saatini bulur
     * Aynı parent altında veya sibling olarak olmalı
     */
    private fun findEndTimeNearStartTime(startNode: AccessibilityNodeInfo, endTime: String): AccessibilityNodeInfo? {
        // Önce parent'a git
        val parent = startNode.parent ?: return null
        
        // Parent'ın tüm child'larında bitiş saatini ara
        for (i in 0 until parent.childCount) {
            parent.getChild(i)?.let { child ->
                if (child.text?.toString()?.trim() == endTime) {
                    return child
                }
                
                // Child'ın sub-tree'sinde de ara
                val found = findNodesByExactText(child, endTime).firstOrNull()
                if (found != null) return found
            }
        }
        
        // Bulunamadıysa bir üst parent'a git
        val grandParent = parent.parent
        if (grandParent != null) {
            for (i in 0 until grandParent.childCount) {
                grandParent.getChild(i)?.let { child ->
                    val found = findNodesByExactText(child, endTime).firstOrNull()
                    if (found != null) return found
                }
            }
        }
        
        return null
    }
    
    /**
     * Başlangıç ve bitiş saati node'ları için "Ayırt" butonunu bulur
     */
    private fun findReserveButtonForTimeSlot(startNode: AccessibilityNodeInfo, endNode: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // Önce aynı parent altındaki sibling'lerde ara
        val parent = startNode.parent
        if (parent != null) {
            val childCount = parent.childCount
            var foundStartNode = false
            
            // Parent'ın tüm child'larını gez
            for (i in 0 until childCount) {
                val child = parent.getChild(i) ?: continue
                
                // Başlangıç node'unu bulana kadar geç
                if (child == startNode) {
                    foundStartNode = true
                    continue
                }
                
                // Başlangıç node'undan sonra, "Ayırt" butonunu ara
                if (foundStartNode) {
                    val text = child.text?.toString()?.lowercase()
                    if (text != null && text.contains("ayırt") && child.isClickable) {
                        if (DEBUG_MODE) Log.d(TAG, "Ayırt butonu bulundu (sibling)")
                        return child
                    }
                }
            }
        }
        
        // Sibling'lerde bulunamadıysa, parent tree'de yukarı çık
        var currentParent: AccessibilityNodeInfo? = parent
        var depth = 0
        
        while (currentParent != null && depth < 7) {
            // Bu parent'ın sub-tree'sinde "Ayırt" butonu ara
            val reserveButton = findNodeInSubtree(currentParent, "ayırt")
            
            if (reserveButton != null && reserveButton.isClickable) {
                if (DEBUG_MODE) Log.d(TAG, "Ayırt butonu bulundu (parent depth: $depth)")
                return reserveButton
            }
            
            currentParent = currentParent.parent
            depth++
        }
        
        return null
    }
    
    /**
     * Belirli bir node'un alt ağacında text arayan yardımcı fonksiyon
     */
    private fun findNodeInSubtree(node: AccessibilityNodeInfo, searchText: String): AccessibilityNodeInfo? {
        // Önce kendisini kontrol et
        node.text?.toString()?.let { text ->
            if (text.contains(searchText, ignoreCase = true)) {
                return node
            }
        }
        
        node.contentDescription?.toString()?.let { desc ->
            if (desc.contains(searchText, ignoreCase = true)) {
                return node
            }
        }
        
        // Child'ları kontrol et
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                val result = findNodeInSubtree(child, searchText)
                if (result != null) return result
            }
        }
        
        return null
    }
    
    /**
     * Metin içeren node'ları bulur
     */
    private fun findNodesByText(rootNode: AccessibilityNodeInfo, keywords: List<String>): List<AccessibilityNodeInfo> {
        val results = mutableListOf<AccessibilityNodeInfo>()
        
        for (keyword in keywords) {
            val nodes = rootNode.findAccessibilityNodeInfosByText(keyword)
            results.addAll(nodes)
        }
        
        return results
    }
    
    /**
     * Resource ID içeren node'ları bulur
     */
    private fun findNodesByResourceId(rootNode: AccessibilityNodeInfo, resourceIds: List<String>): List<AccessibilityNodeInfo> {
        val results = mutableListOf<AccessibilityNodeInfo>()
        
        fun searchRecursively(node: AccessibilityNodeInfo) {
            val viewId = node.viewIdResourceName
            if (viewId != null) {
                for (id in resourceIds) {
                    if (viewId.contains(id, ignoreCase = true)) {
                        results.add(node)
                        break
                    }
                }
            }
            
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { child ->
                    searchRecursively(child)
                }
            }
        }
        
        searchRecursively(rootNode)
        return results
    }
    
    /**
     * Text field'a metin yazar
     */
    private fun fillTextField(node: AccessibilityNodeInfo, text: String) {
        node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        
        val arguments = android.os.Bundle()
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }
    
    /**
     * DEBUG: Tüm UI ağacını loglar
     * Kurye uygulamasının yapısını anlamak için kullan
     */
    private fun logNodeHierarchy(node: AccessibilityNodeInfo?, depth: Int = 0) {
        node ?: return
        
        val indent = "  ".repeat(depth)
        val viewId = node.viewIdResourceName ?: "NO_ID"
        val className = node.className ?: "NO_CLASS"
        val text = node.text?.toString() ?: ""
        val contentDesc = node.contentDescription?.toString() ?: ""
        val isClickable = node.isClickable
        val isEnabled = node.isEnabled
        
        val info = buildString {
            append("$indent├─ ")
            append("[$className] ")
            if (viewId != "NO_ID") append("ID:$viewId ")
            if (text.isNotEmpty()) append("TEXT:'$text' ")
            if (contentDesc.isNotEmpty()) append("DESC:'$contentDesc' ")
            if (isClickable) append("[CLICKABLE] ")
            if (!isEnabled) append("[DISABLED] ")
        }
        
        Log.d(TAG, info)
        
        // Child node'ları logla
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                logNodeHierarchy(child, depth + 1)
            }
        }
    }
    
    /**
     * Belirli bir resource ID'ye sahip node'u bulur
     */
    private fun findNodeByExactResourceId(rootNode: AccessibilityNodeInfo, resourceId: String): AccessibilityNodeInfo? {
        if (rootNode.viewIdResourceName == resourceId) {
            return rootNode
        }
        
        for (i in 0 until rootNode.childCount) {
            rootNode.getChild(i)?.let { child ->
                val result = findNodeByExactResourceId(child, resourceId)
                if (result != null) return result
            }
        }
        
        return null
    }
    
    /**
     * İnsan benzeri rastgele gecikme (bot tespitini önlemek için)
     */
    private fun humanDelay(minDelay: Long = MIN_HUMAN_DELAY, maxDelay: Long = MAX_HUMAN_DELAY) {
        val delay = (minDelay..maxDelay).random()
        Thread.sleep(delay)
    }
    
    /**
     * Text field'a insan gibi yazar (karakter karakter, rastgele hızda)
     */
    private fun fillTextFieldHumanLike(node: AccessibilityNodeInfo, text: String) {
        node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        humanDelay(200, 500) // Focus sonrası düşünme
        
        // Karakter karakter yaz (daha doğal)
        for (char in text) {
            val currentText = node.text?.toString() ?: ""
            val newText = currentText + char
            
            val arguments = android.os.Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, newText)
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            
            // Her karakter arası rastgele gecikme (yazma hızı)
            Thread.sleep((MIN_TYPING_DELAY..MAX_TYPING_DELAY).random())
        }
    }
    
    override fun onInterrupt() {
        if (DEBUG_MODE) Log.d(TAG, "Service interrupted")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (DEBUG_MODE) Log.d(TAG, "Service destroyed")
    }
}
