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
        
        // Kurye uygulamasının package name'i buraya yazılacak
        // adb shell dumpsys window | grep mCurrentFocus komutuyla bul
        // Örnek: "com.yemeksepeti.courier"
        private const val COURIER_APP_PACKAGE = "com.yemeksepeti.courier"
        
        // DEBUG MOD - Tüm UI elementlerini loglamak için true yap
        // Kurye uygulamanızın yapısını öğrendikten sonra false yapın
        private const val DEBUG_MODE = false
        
        // İnsan benzeri davranış için rastgele gecikme aralıkları (ms)
        private const val MIN_HUMAN_DELAY = 800L
        private const val MAX_HUMAN_DELAY = 2500L
        private const val MIN_TYPING_DELAY = 100L
        private const val MAX_TYPING_DELAY = 300L
    }
    
    private lateinit var preferencesManager: PreferencesManager
    private var isLoginAttempted = false
    private var isSlotSelectionActive = false
    private var isNavigatingToSlots = false
    private var lastNavigationAttempt = 0L
    private val NAVIGATION_COOLDOWN = 5000L // 5 saniye cooldown
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        preferencesManager = PreferencesManager(applicationContext)
        
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or 
                        AccessibilityEvent.TYPE_VIEW_CLICKED or
                        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            packageNames = arrayOf(COURIER_APP_PACKAGE)
        }
        
        serviceInfo = info
        if (DEBUG_MODE) Log.d(TAG, "Accessibility Service başlatıldı")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        
        if (event.packageName != COURIER_APP_PACKAGE) return
        
        val rootNode = rootInActiveWindow ?: return
        
        try {
            // DEBUG: Tüm ekran yapısını logla
            if (DEBUG_MODE && event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                Log.d(TAG, "=== YENİ EKRAN TESPİT EDİLDİ ===")
                Log.d(TAG, "Paket: ${event.packageName}")
                Log.d(TAG, "Sınıf: ${event.className}")
                logNodeHierarchy(rootNode, 0)
                Log.d(TAG, "=== EKRAN YAPISI BİTTİ ===")
            }
            
            when {
                // Giriş ekranı tespit edildi
                isLoginScreen(rootNode) && !isLoginAttempted -> {
                    if (DEBUG_MODE) Log.d(TAG, "Giriş ekranı tespit edildi")
                    performAutoLogin(rootNode)
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
                    performSlotSelection(rootNode)
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
        val slotKeywords = listOf("slot", "vardiya", "seç", "select", "saat", "time", "çalışma saatleri", "work hours")
        
        return findNodesByText(rootNode, slotKeywords).isNotEmpty()
    }
    
    /**
     * Slot seçim ekranına navigasyon yapar
     */
    private fun navigateToSlotScreen(rootNode: AccessibilityNodeInfo) {
        isNavigatingToSlots = true
        
        // Slot ekranına gitme butonunu bul
        // Olası buton metinleri: "Slot Seç", "Vardiya", "Çalışma Saatleri", "Slotlar", vb.
        val navigationKeywords = listOf(
            "slot", "vardiya", "çalışma saatleri", "work hours",
            "saat seç", "slot seç", "blok seç", "zaman seç"
        )
        
        val slotButton = findNodesByText(rootNode, navigationKeywords).firstOrNull { node ->
            node.isClickable || node.parent?.isClickable == true
        }
        
        if (slotButton != null) {
            val clickableNode = if (slotButton.isClickable) slotButton else slotButton.parent
            clickableNode?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d(TAG, "Slot ekranı butonu tıklandı")
        } else {
            // Buton bulunamadı, menü/hamburger butonunu dene
            val menuButton = findNodesByResourceId(rootNode, listOf(
                "menu", "navigation", "drawer", "toolbar"
            )).firstOrNull { it.isClickable }
            
            menuButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            if (DEBUG_MODE) Log.d(TAG, "Menü butonu tıklandı, slot seçeneği bekleniyor")
            
            // Menü açıldıktan sonra slot butonunu ara
            Thread.sleep(1000)
            rootInActiveWindow?.let { root ->
                val menuSlotButton = findNodesByText(root, navigationKeywords).firstOrNull { it.isClickable }
                menuSlotButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                if (DEBUG_MODE) Log.d(TAG, "Menüden slot seçeneği tıklandı")
                root.recycle()
            }
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
            
            // Her bir tercih için slot ara ve insan gibi seç
            for (preference in timePreferences) {
                val slotText = "${preference.startTime}-${preference.endTime}"
                val slotNodes = findNodesByText(rootNode, listOf(slotText, preference.startTime))
                
                for (node in slotNodes) {
                    if (node.isClickable || node.parent?.isClickable == true) {
                        humanDelay() // İnsan benzeri rastgele gecikme
                        val clickableNode = if (node.isClickable) node else node.parent
                        clickableNode?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (DEBUG_MODE) Log.d(TAG, "Slot seçildi: $slotText")
                    }
                }
            }
            
            // Onay/Kaydet butonunu bul ve insan gibi tıkla
            humanDelay(MIN_HUMAN_DELAY + 700, MAX_HUMAN_DELAY + 1500) // Kontrol etme süresi
            val confirmButton = findNodesByText(rootNode, listOf("onayla", "kaydet", "tamamla", "confirm", "save")).firstOrNull {
                it.isClickable
            }
            
            confirmButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            if (DEBUG_MODE) Log.d(TAG, "Slot seçimi onaylandı")
            
        } finally {
            // 3 saniye sonra reset et (tekrar seçim yapabilmek için)
            Thread.sleep(3000)
            isSlotSelectionActive = false
        }
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
