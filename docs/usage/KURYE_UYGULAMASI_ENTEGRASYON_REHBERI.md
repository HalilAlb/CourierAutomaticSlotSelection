# 🔍 Kurye Uygulaması Entegrasyon Rehberi

Bu rehber, AccessibilityService'in gerçek kurye uygulamanızda nasıl çalışacağını ve UI elementlerini nasıl bulacağını açıklar.

---

## 📱 AccessibilityService Nasıl Çalışır?

AccessibilityService, Android'in erişilebilirlik özelliğidir ve ekrandaki tüm UI elementlerini okuyabilir:

- **TextView** → Metinleri okur
- **EditText** → Giriş alanlarına yazabilir  
- **Button** → Butonlara tıklayabilir
- **RecyclerView/ListView** → Listelerdeki öğeleri görebilir

### Şu Anda Kod Nasıl Çalışıyor?

```kotlin
// 1. Giriş ekranını tespit et
isLoginScreen(rootNode) // "giriş", "login" gibi metinleri arar

// 2. ID ve şifre alanlarını bul
findNodesByText(rootNode, listOf("id", "kullanıcı", "kurye"))

// 3. Slot ekranına git
navigateToSlotScreen(rootNode) // "slot", "vardiya" gibi butonları arar

// 4. Slot seçimini yap
performSlotSelection(rootNode) // "16:00-19:00" gibi metinleri arar
```

**SORUN:** Gerçek kurye uygulamanızın:
- Paket adı nedir? (`COURIER_APP_PACKAGE`)
- Buton ID'leri nedir? (örn: `com.yemek:id/login_button`)
- Metin isimleri Türkçe mi İngilizce mi?

---

## 🎯 Adım 1: Kurye Uygulamasının Paket Adını Bul

### Yöntem 1: adb ile (Önerilen)
```bash
# Kurye uygulamasını aç
# Sonra terminalde çalıştır:
adb shell dumpsys window | grep -i mCurrentFocus
```

**Çıktı örneği:**
```
mCurrentFocus=Window{abc123 u0 com.yemeksepeti.courier/MainActivity}
```
→ Paket adı: `com.yemeksepeti.courier`

### Yöntem 2: Uygulama Listesi
```bash
adb shell pm list packages | grep -i yemek
```

### Yöntem 3: Play Store
- Kurye uygulamasının Play Store linkine git
- URL'deki `id=` kısmına bak
- Örnek: `play.google.com/store/apps/details?id=com.yemeksepeti.courier`

---

## 🔬 Adım 2: UI Elementlerini Keşfet (DEBUG MOD)

### Debug Modunu Aktif Et

1. **CourierAccessibilityService.kt** dosyasını aç
2. `DEBUG_MODE = true` olduğundan emin ol (zaten aktif)
3. `COURIER_APP_PACKAGE` yerine gerçek paket adını yaz:

```kotlin
private const val COURIER_APP_PACKAGE = "com.yemeksepeti.courier" // BURAYA GERÇEk PAKET ADI
```

### Logları Gör

```bash
# Terminal'de logları izle
adb logcat | grep CourierAccessibility
```

**Kurye uygulamasını açtığında şöyle loglar göreceksin:**

```
=== YENİ EKRAN TESPİT EDİLDİ ===
Paket: com.yemeksepeti.courier
Sınıf: com.yemek.LoginActivity
├─ [LinearLayout] ID:com.yemek:id/login_container [CLICKABLE] 
  ├─ [EditText] ID:com.yemek:id/input_courier_id TEXT:'' 
  ├─ [EditText] ID:com.yemek:id/input_password TEXT:'' 
  ├─ [Button] ID:com.yemek:id/btn_login TEXT:'GİRİŞ YAP' [CLICKABLE] 
=== EKRAN YAPISI BİTTİ ===
```

### Önemli Bilgileri Not Et

**Giriş Ekranı:**
- ID input field: `com.yemek:id/input_courier_id`
- Şifre input field: `com.yemek:id/input_password`
- Login butonu: `com.yemek:id/btn_login` veya TEXT: "GİRİŞ YAP"

**Ana Ekran:**
- Slot butonu: `com.yemek:id/btn_slots` veya TEXT: "Slotlarım"
- Hamburger menü: `com.yemek:id/menu_icon`

**Slot Ekranı:**
- Slot liste: `com.yemek:id/slots_recyclerview`
- Slot item: `com.yemek:id/slot_time_text` TEXT: "16:00 - 19:00"
- Onay butonu: `com.yemek:id/btn_confirm_slots` veya TEXT: "ONAYLA"

---

## ✏️ Adım 3: Kodu Gerçek Değerlerle Güncelle

### Örnek: Giriş Ekranı İyileştirme

**ŞU ANKİ KOD (genel aramalar):**
```kotlin
private fun isLoginScreen(rootNode: AccessibilityNodeInfo): Boolean {
    val keywords = listOf("giriş", "login", "oturum", "sign in")
    val nodes = findNodesByText(rootNode, keywords)
    return nodes.isNotEmpty()
}
```

**YENİ KOD (gerçek resource ID kullan):**
```kotlin
private fun isLoginScreen(rootNode: AccessibilityNodeInfo): Boolean {
    // Gerçek login butonunun ID'sini kontrol et
    val loginButton = findNodeByExactResourceId(rootNode, "com.yemek:id/btn_login")
    return loginButton != null
}
```

### Örnek: Giriş Yapma İyileştirme

**ŞU ANKİ KOD:**
```kotlin
val idField = findNodesByText(rootNode, listOf("id", "kullanıcı", "kurye")).firstOrNull {
    it.className == "android.widget.EditText"
}
```

**YENİ KOD:**
```kotlin
val idField = findNodeByExactResourceId(rootNode, "com.yemek:id/input_courier_id")
val passwordField = findNodeByExactResourceId(rootNode, "com.yemek:id/input_password")
val loginButton = findNodeByExactResourceId(rootNode, "com.yemek:id/btn_login")
```

### Örnek: Slot Seçimi İyileştirme

**ŞU ANKİ KOD:**
```kotlin
val slotText = "${preference.startTime}-${preference.endTime}"
val slotNodes = findNodesByText(rootNode, listOf(slotText, preference.startTime))
```

**YENİ KOD:**
```kotlin
// RecyclerView içindeki slot itemlarını bul
val slotsRecyclerView = findNodeByExactResourceId(rootNode, "com.yemek:id/slots_recyclerview")

// Her child item'a bak
for (i in 0 until (slotsRecyclerView?.childCount ?: 0)) {
    val slotItem = slotsRecyclerView?.getChild(i)
    val timeText = findNodeByExactResourceId(slotItem, "com.yemek:id/slot_time_text")
    
    if (timeText?.text?.contains(preference.startTime) == true) {
        slotItem?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }
}
```

---

## 🛠️ Pratik Test Senaryosu

### 1. Uygulamayı Kur ve Servisi Aç
```bash
./install.sh

# Accessibility Service'i aktif et:
# Ayarlar → Erişilebilirlik → Slot Asistanı → Aç
```

### 2. Logları İzle
```bash
adb logcat -c  # Eski logları temizle
adb logcat | grep -E "CourierAccessibility|SlotAssistant"
```

### 3. Kurye Uygulamasını Aç
- Giriş ekranında ne loglandı?
- Ana ekranda ne loglandı?
- Slot ekranında ne loglandı?

### 4. Logları Analiz Et
- Hangi ID'ler var?
- Hangi metinler var?
- Hangi butonlar clickable?

### 5. Kodu Güncelle
- `CourierAccessibilityService.kt` dosyasını aç
- `findNodeByExactResourceId()` ile gerçek ID'leri kullan
- Debug modunu kapat → `DEBUG_MODE = false`

---

## 📋 Örnek Log Analizi

### Giriş Ekranı Logu:
```
├─ [LinearLayout] ID:com.yemeksepeti.courier:id/login_root 
  ├─ [ImageView] ID:com.yemeksepeti.courier:id/logo 
  ├─ [EditText] ID:com.yemeksepeti.courier:id/et_courier_id TEXT:'' 
  ├─ [EditText] ID:com.yemeksepeti.courier:id/et_password TEXT:'' 
  ├─ [Button] ID:com.yemeksepeti.courier:id/btn_login TEXT:'GİRİŞ' [CLICKABLE]
```

**Buradan çıkarımlar:**
- ID input: `et_courier_id`
- Password input: `et_password`
- Login butonu: `btn_login`

### Güncellenmiş Kod:
```kotlin
private fun performAutoLogin(rootNode: AccessibilityNodeInfo) {
    val courierId = preferencesManager.getCourierId()
    val password = preferencesManager.getCourierPassword()
    
    if (courierId.isEmpty() || password.isEmpty()) return
    
    // GERÇEK RESOURCE ID'LERLE
    val idField = findNodeByExactResourceId(rootNode, "com.yemeksepeti.courier:id/et_courier_id")
    val passwordField = findNodeByExactResourceId(rootNode, "com.yemeksepeti.courier:id/et_password")
    val loginButton = findNodeByExactResourceId(rootNode, "com.yemeksepeti.courier:id/btn_login")
    
    idField?.let { fillTextField(it, courierId) }
    passwordField?.let { fillTextField(it, password) }
    loginButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
}
```

---

## ⚠️ Sık Karşılaşılan Sorunlar

### 1. "Accessibility Service çalışmıyor"
```bash
# Servis durumunu kontrol et
adb shell settings get secure enabled_accessibility_services

# Çıktı şöyle olmalı:
# com.example.slotassistant/.accessibility.CourierAccessibilityService
```

### 2. "Loglar gelmiyor"
- `COURIER_APP_PACKAGE` doğru mu?
- Accessibility Service aktif mi?
- Kurye uygulaması açık mı?

```bash
# Paket adını tekrar kontrol et
adb shell dumpsys window | grep mCurrentFocus
```

### 3. "Butonlar tıklanmıyor"
- Parent node clickable olabilir:
```kotlin
if (node.isClickable) {
    node.performAction(ACTION_CLICK)
} else if (node.parent?.isClickable == true) {
    node.parent.performAction(ACTION_CLICK)
}
```

### 4. "Text field'a yazamıyor"
```kotlin
// Önce focus yap
node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
Thread.sleep(200)

// Sonra yaz
val args = Bundle()
args.putCharSequence(ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
node.performAction(ACTION_SET_TEXT, args)
```

---

## 🎓 İleri Seviye: XPath Benzeri Arama

Bazen UI yapısı karmaşıktır. Örneğin:

```
RecyclerView
  ├─ CardView (Slot 1)
  │   ├─ TextView (Saat: "16:00-19:00")
  │   └─ CheckBox (Seç)
  ├─ CardView (Slot 2)
      ├─ TextView (Saat: "20:00-22:00")
      └─ CheckBox (Seç)
```

**Slot seçmek için:**
```kotlin
// RecyclerView'i bul
val recyclerView = findNodeByExactResourceId(rootNode, "com.yemek:id/slots_list")

// Her slot item'ı kontrol et
for (i in 0 until (recyclerView?.childCount ?: 0)) {
    val slotCard = recyclerView.getChild(i)
    
    // Saat textini bul
    val timeTextView = findFirstNodeByClassName(slotCard, "android.widget.TextView")
    val timeText = timeTextView?.text?.toString() ?: ""
    
    // Tercih edilen saat mi?
    if (timeText.contains(preference.startTime)) {
        // CheckBox'ı bul ve tıkla
        val checkbox = findFirstNodeByClassName(slotCard, "android.widget.CheckBox")
        checkbox?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        
        // Veya tüm kartı tıkla
        slotCard?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }
}
```

---

## ✅ Kontrol Listesi

- [ ] Kurye uygulamasının paket adını buldum (`adb shell dumpsys window`)
- [ ] `COURIER_APP_PACKAGE` değişkenine gerçek paket adını yazdım
- [ ] Debug modunu açtım (`DEBUG_MODE = true`)
- [ ] Logları izledim (`adb logcat | grep CourierAccessibility`)
- [ ] Giriş ekranı elementlerini not aldım
- [ ] Slot ekranı elementlerini not aldım
- [ ] `performAutoLogin()` fonksiyonunu gerçek ID'lerle güncelledim
- [ ] `isSlotSelectionScreen()` fonksiyonunu gerçek ID'lerle güncelledim
- [ ] `performSlotSelection()` fonksiyonunu gerçek ID'lerle güncelledim
- [ ] Debug modunu kapattım (`DEBUG_MODE = false`)
- [ ] Gerçek kurye uygulamasında test ettim

---

## 📞 Hata Ayıklama İpuçları

### Log Seviyelerini Değiştir
```kotlin
// Daha detaylı loglar için
Log.v(TAG, "Verbose log")  // Tüm detaylar
Log.d(TAG, "Debug log")    // Debug bilgileri
Log.i(TAG, "Info log")     // Genel bilgiler
Log.w(TAG, "Warning log")  // Uyarılar
Log.e(TAG, "Error log")    // Hatalar
```

### Ekran Görüntüsü Al
```bash
# UI'ı görmek için
adb exec-out screencap -p > screenshot.png
```

### Layout Inspector (Android Studio)
1. Android Studio aç
2. Tools → Layout Inspector
3. Cihazı seç
4. Kurye uygulamasını seç
5. Tüm UI ağacını görsel olarak gör

---

## 🎯 Özet

1. **Paket adını bul** → `adb shell dumpsys window`
2. **Debug modunu aç** → Logları izle
3. **UI elementlerini not al** → Resource ID'ler, text'ler
4. **Kodu güncelle** → Gerçek ID'leri kullan
5. **Test et** → Kurye uygulamasında dene
6. **İyileştir** → Hata varsa logları kontrol et

**Önemli:** Her kurye uygulaması farklıdır. Bu rehber genel bir yaklaşımdır, gerçek uygulamanıza göre uyarlayın.
