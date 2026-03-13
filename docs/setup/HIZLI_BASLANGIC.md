# 🚀 Hızlı Başlangıç: UI Elementlerini Bul

## 1️⃣ Kurye Uygulaması Paket Adını Bul

Terminal'de çalıştır:

```bash
# Kurye uygulamasını telefonunda aç
# Sonra bu komutu çalıştır:
adb shell dumpsys window | grep -i mCurrentFocus
```

**Örnek çıktı:**
```
mCurrentFocus=Window{76b2bd9 u0 com.logistics.rider.yemeksepeti/com.foodora.courier.main.presentation.MainActivity}
                                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
```

Paket adı: **com.logistics.rider.yemeksepeti**

---

## 2️⃣ Paket Adını Koda Yaz

**Dosya:** `app/src/main/java/com/example/slotassistant/accessibility/CourierAccessibilityService.kt`

**26. satırda** değiştir:

```kotlin
// ❌ YANLIŞ (eski)
private const val COURIER_APP_PACKAGE = "Yemeksepeti Express"

// ✅ DOĞRU (gerçek paket adı)
private const val COURIER_APP_PACKAGE = "com.logistics.rider.yemeksepeti"
```

---

## 3️⃣ Accessibility Config'i Güncelle

**Dosya:** `app/src/main/res/xml/accessibility_service_config.xml`

**8. satırda** değiştir:

```xml
<!-- ❌ YANLIŞ -->
android:packageNames="Yemeksepeti Express"

<!-- ✅ DOĞRU -->
android:packageNames="com.logistics.rider.yemeksepeti"
```

---

## 4️⃣ Uygulamayı Kur ve Servisi Aç

```bash
# PATH'i düzelt (sadece bir kere)
./setup-adb.sh
source ~/.zshrc

# Uygulamayı kur
./install.sh
```

**Telefonda:**
1. **Ayarlar** → **Erişilebilirlik** → **Slot Asistanı** → **Aç**
2. İzin ver

---

## 5️⃣ Logları İzle

```bash
# Yeni terminal aç
adb logcat -c  # Eski logları temizle
adb logcat | grep CourierAccessibility
```

---

## 6️⃣ Kurye Uygulamasını Aç

Telefonda kurye uygulamasını aç → Loglar akmaya başlayacak:

```
=== YENİ EKRAN TESPİT EDİLDİ ===
Paket: com.logistics.rider.yemeksepeti
Sınıf: com.foodora.courier.main.presentation.MainActivity
├─ [LinearLayout] ID:com.yemek:id/login_container
  ├─ [EditText] ID:com.yemek:id/input_id TEXT:'' 
  ├─ [EditText] ID:com.yemek:id/input_password TEXT:'' 
  ├─ [Button] ID:com.yemek:id/btn_login TEXT:'GİRİŞ' [CLICKABLE]
=== EKRAN YAPISI BİTTİ ===
```

---

## 7️⃣ Önemli Bilgileri Not Al

### Giriş Ekranı
- **ID input field:** `com.yemek:id/input_id`
- **Şifre input field:** `com.yemek:id/input_password`
- **Login butonu:** `com.yemek:id/btn_login`

### Ana Ekran (Ana sayfa açıldığında log bak)
- **Slot butonu:** `com.yemek:id/btn_slots` veya TEXT:'Slotlarım'

### Slot Ekranı (Slot ekranını aç, log bak)
- **Slot liste:** `com.yemek:id/slots_recyclerview`
- **Slot saat:** `com.yemek:id/slot_time` TEXT:'16:00-19:00'
- **Onay butonu:** `com.yemek:id/btn_confirm`

---

## 8️⃣ Kodu Güncelle

### Örnek: Giriş Ekranı

**CourierAccessibilityService.kt** → `performAutoLogin()` fonksiyonu:

```kotlin
private fun performAutoLogin(rootNode: AccessibilityNodeInfo) {
    val courierId = preferencesManager.getCourierId()
    val password = preferencesManager.getCourierPassword()
    
    if (courierId.isEmpty() || password.isEmpty()) return
    
    // ✅ GERÇEK ID'LERI KULLAN (loglardan aldığın)
    val idField = findNodeByExactResourceId(rootNode, "com.yemek:id/input_id")
    val passwordField = findNodeByExactResourceId(rootNode, "com.yemek:id/input_password")
    val loginButton = findNodeByExactResourceId(rootNode, "com.yemek:id/btn_login")
    
    idField?.let { fillTextField(it, courierId) }
    passwordField?.let { fillTextField(it, password) }
    loginButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    
    Log.d(TAG, "Giriş yapıldı")
}
```

### Örnek: Slot Ekranı Tespit

**CourierAccessibilityService.kt** → `isSlotSelectionScreen()` fonksiyonu:

```kotlin
private fun isSlotSelectionScreen(rootNode: AccessibilityNodeInfo): Boolean {
    // ✅ GERÇEK ID KULLAN
    val confirmButton = findNodeByExactResourceId(rootNode, "com.yemek:id/btn_confirm")
    return confirmButton != null
}
```

---

## 9️⃣ Test Et

1. **Build:**
   ```bash
   ./gradlew assembleDebug
   ```

2. **Kur:**
   ```bash
   ./install.sh
   ```

3. **Dene:**
   - Uygulamayı aç
   - Kurye bilgilerini gir
   - Slot ekle
   - "Otomatik Kontrolü Başlat"
   - Kurye uygulamasını aç
   - Logları izle

---

## 🎯 İşte Bu Kadar!

**Artık uygulaman:**
✅ Kurye uygulamasını tespit edebiliyor
✅ Giriş yapabiliyor  
✅ Slot ekranını buluyor
✅ Otomatik slot seçimi yapıyor

**Sorun varsa:**
- Logları kontrol et: `adb logcat | grep CourierAccessibility`
- Detaylı rehber: [KURYE_UYGULAMASI_ENTEGRASYON_REHBERI.md](KURYE_UYGULAMASI_ENTEGRASYON_REHBERI.md)
