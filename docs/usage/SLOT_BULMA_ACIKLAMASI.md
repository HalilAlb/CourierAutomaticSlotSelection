# 📝 Kurye Uygulaması Slot Seçimi - Nasıl Çalışır?

## 🎯 Sorunuz: "Uygulama müsait slotlar bölümünü nasıl bulacak?"

**Cevap:** AccessibilityService ile ekrandaki tüm UI elementlerini okur ve resource ID veya text ile bulur.

---

## 🔧 AccessibilityService Nedir?

Android'in **Erişilebilirlik** özelliği. Görme engelliler için ekranı okuyan uygulamalar gibi çalışır:

- ✅ Ekrandaki tüm butonları, textleri, input alanlarını görebilir
- ✅ Butonlara tıklayabilir
- ✅ Input alanlarına yazabilir
- ✅ Liste öğelerini seçebilir

---

## 📱 Sizin Durumunuzda Nasıl Çalışacak?

### 1. **Kurye Uygulamasını Tespit Eder**
```kotlin
COURIER_APP_PACKAGE = "com.yemeksepeti.courier" // Gerçek paket adı
```

### 2. **Giriş Ekranını Bulur**
```kotlin
// "GİRİŞ" butonu varsa giriş ekranıdır
isLoginScreen() → ID/Şifre alanlarını bulur → Otomatik giriş yapar
```

### 3. **Slot Ekranına Gider**
```kotlin
// "Slotlarım" veya "Vardiyalar" butonunu bulur → Tıklar
navigateToSlotScreen()
```

### 4. **Müsait Slotları Bulur**
```kotlin
// Ekranda "16:00-19:00", "20:00-22:00" gibi textleri arar
// Sizin tercih ettiğiniz slotları seçer
performSlotSelection()
```

### 5. **Onaylar**
```kotlin
// "ONAYLA" veya "KAYDET" butonunu bulur → Tıklar
```

---

## 🚨 ÖNEMLİ: Gerçek Paket Adı Gerekli

**Şu anki kod "com.yemeksepeti.courier" olarak ayarlı, ama bu PLACEHOLDER!**

### Gerçek Paket Adını Bulmak İçin:

```bash
# 1. Kurye uygulamasını telefonunda aç
# 2. Terminal'de çalıştır:
adb shell dumpsys window | grep mCurrentFocus

# Çıktı:
mCurrentFocus=Window{... com.yemeksepeti.express/...}
                          ^^^^^^^^^^^^^^^^^^^^^^^^^
                          GERÇEK PAKET ADI
```

---

## 🛠️ Debug Modu Aktif

**Şu anda kod DEBUG modunda**, yani kurye uygulamanızı açtığınızda **tüm UI yapısını loglara yazdırır:**

```bash
# Logları görmek için:
adb logcat | grep CourierAccessibility
```

**Kurye uygulamasını açtığınızda göreceğiniz log örneği:**

```
=== YENİ EKRAN TESPİT EDİLDİ ===
├─ [LinearLayout] ID:com.yemek:id/login_root
  ├─ [EditText] ID:com.yemek:id/et_courier_id TEXT:'' 
  ├─ [EditText] ID:com.yemek:id/et_password TEXT:'' 
  ├─ [Button] ID:com.yemek:id/btn_login TEXT:'GİRİŞ YAP' [CLICKABLE]
```

Bu loglardan:
- **ID input field:** `et_courier_id`
- **Şifre input field:** `et_password`
- **Login butonu:** `btn_login`

gibi bilgileri öğrenip koda yazacaksınız.

---

## 📋 Yapmanız Gerekenler (Sırayla)

### ✅ 1. Paket Adını Bul
```bash
adb shell dumpsys window | grep mCurrentFocus
```

### ✅ 2. Koda Yaz
**Dosya:** `CourierAccessibilityService.kt` → 26. satır
```kotlin
private const val COURIER_APP_PACKAGE = "BURAYA_GERÇEK_PAKET_ADI"
```

### ✅ 3. Uygulamayı Kur
```bash
./gradlew assembleDebug
./install.sh
```

### ✅ 4. Accessibility Service'i Aç
**Telefon:** Ayarlar → Erişilebilirlik → Slot Asistanı → **AÇ**

### ✅ 5. Logları İzle
```bash
adb logcat | grep CourierAccessibility
```

### ✅ 6. Kurye Uygulamasını Aç
→ Loglar UI yapısını gösterecek

### ✅ 7. UI Elementlerini Not Al
Loglardan:
- Giriş ekranı butonları
- Slot ekranı butonları
- Onay butonu
gibi bilgileri al

### ✅ 8. Kodu Güncelle
`performAutoLogin()`, `isSlotSelectionScreen()` gibi fonksiyonlarda gerçek ID'leri kullan

---

## 📚 Rehberler Oluşturuldu

1. **[HIZLI_BASLANGIC.md](HIZLI_BASLANGIC.md)** → Adım adım başlangıç
2. **[KURYE_UYGULAMASI_ENTEGRASYON_REHBERI.md](KURYE_UYGULAMASI_ENTEGRASYON_REHBERI.md)** → Detaylı teknik rehber

---

## 🎯 Özet

**Uygulama müsait slotları nasıl bulacak?**

1. **Debug modunda** kurye uygulamasını açıyorsunuz
2. **Loglar** tüm UI elementlerini gösteriyor
3. **Resource ID'leri** not alıyorsunuz (örn: `com.yemek:id/slot_time`)
4. **Kodu güncelliyorsunuz** → Gerçek ID'lerle slot buluyor
5. **Otomatik seçim çalışıyor** ✅

---

## ⚡ Hemen Başla

```bash
# 1. PATH'i düzelt (sadece bir kere)
./setup-adb.sh && source ~/.zshrc

# 2. Build et
./gradlew assembleDebug

# 3. Kur
./install.sh

# 4. Logları izle
adb logcat | grep CourierAccessibility

# 5. Kurye uygulamasını aç → Logları gör → Kodu güncelle
```

**Başarılar! 🚀**
