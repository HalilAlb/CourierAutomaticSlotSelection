# 🔍 Slot Seçme Sorunu - Hata Ayıklama Rehberi

## ❌ Sorun: Yarın İçin Slot Seçilemiyor

Bu rehber, uygulamanın neden slot seçemediğini bulmak için adım adım size yardımcı olacak.

---

## 🛠️ Adım 1: Gerçek Paket Adını Bulun

Uygulama şu anda **placeholder** paket adı kullanıyor: `com.yemeksepeti.courier`

Gerçek adı bulun:

```bash
# 1. Yemeksepeti Express'i telefonunuzda AÇIN

# 2. Bu komutu çalıştırın:
./scripts/find-courier-app.sh

# VEYA manuel:
adb shell dumpsys window | grep mCurrentFocus
```

**Çıktı örnek:**
```
mCurrentFocus=Window{abc u0 com.yemeksepeti.courierexpress/...}
                           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                           Bu paket adıdır
```

### Paket Adını Kodda Güncelleme

[CourierAccessibilityService.kt](../app/src/main/java/com/example/slotassistant/accessibility/CourierAccessibilityService.kt) dosyasını açın:

```kotlin
// Satır 27 civarı:
private const val COURIER_APP_PACKAGE = "com.yemeksepeti.courierexpress" // ← Gerçek adı buraya
```

---

## 🛠️ Adım 2: Debug Modunu Kontrol Edin

✅ **Debug mod artık AÇIK** (方才 güncellendi)

Debug mod sayesinde tüm UI elementleri loglanacak.

---

## 🛠️ Adım 3: Yeni APK Kur

```bash
# Yeni debug modlu APK kur
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Uygulamayı aç
# Ayarlar > Erişilebilirlik > Slot Assistant > Açık olduğundan emin ol
```

---

## 🛠️ Adım 4: Test Et ve Log'ları İzle

### Terminal 1: Log'ları izle
```bash
# Gerçek zamanlı log takibi
adb logcat -c  # Log'ları temizle
adb logcat | grep -E "CourierAccessibility|SlotCheck"
```

### Terminal 2: Test et
```bash
# Uygulamada:
# 1. Kurye Ayarları → ID/Şifre gir
# 2. Slot ekle → 16:00-19:00
# 3. "Şimdi Kontrol Et" butonuna bas
```

---

## 📊 Log'larda Neyi Arayın?

### ✅ Başarılı Scenario:

```
CourierAccessibility: Kurye uygulaması açıldı: com.yemeksepeti.courierexpress
CourierAccessibility: Giriş ekranı tespit edildi
CourierAccessibility: Kurye ID girildi
CourierAccessibility: Şifre girildi
CourierAccessibility: Giriş butonu tıklandı
CourierAccessibility: Ana ekran tespit edildi
CourierAccessibility: Slot ekranı butonu tıklandı
CourierAccessibility: Slot seçildi: 16:00-19:00
CourierAccessibility: Slot seçimi onaylandı
```

### ❌ Sorunlu Scenario 1: Paket Adı Yanlış

```
# Hiçbir log görünmüyor
```
**Çözüm:** Adım 1'e dönün, gerçek paket adını bulun

---

### ❌ Sorunlu Scenario 2: UI Element Bulunamıyor

```
CourierAccessibility: Kurye uygulaması açıldı
CourierAccessibility: UI hierarchy dumped: <tüm elementler listelenecek>
# Ama "Giriş ekranı tespit edildi" yok
```

**Çözüm:** Log'lardaki UI hierarchy'ye bakın, giriş alanlarının gerçek text/id'lerini görün

---

### ❌ Sorunlu Scenario 3: Slot Bulunamıyor

```
CourierAccessibility: Ana ekran tespit edildi
CourierAccessibility: Slot ekranı butonu tıklandı
# Ama "Slot seçildi" yok
```

**Çözüm:** Slot'ların UI'daki text formatını kontrol edin

---

## 🔧 Olası Sorunlar ve Çözümler

### 1. Paket Adı Yanlış
- **Belirti:** Hiç log gelmiyor
- **Çözüm:** `./scripts/find-courier-app.sh` ile gerçek adı bul

### 2. AccessibilityService Kapalı
- **Belirti:** Hiç log gelmiyor
- **Çözüm:** Ayarlar > Erişilebilirlik > Slot Assistant > AÇ

### 3. Giriş Alanları Farklı ID/Text
- **Belirti:** "Giriş ekranı tespit edildi" yok
- **Çözüm:** Log'dan gerçek field name'leri bul, kodda güncelle

### 4. Slot Formatı Farklı
- **Belirti:** "Slot seçildi" yok
- **Çözüm:** Slot'ların ekranda nasıl göründüğünü kontrol et
  - Örnek: "16:00-19:00" mı yoksa "16:00 - 19:00" (boşluklu) mu?

### 5. Tarih/Gün Problemi
- **Belirti:** Slot seçilmiyor ama UI tespit ediliyor
- **Çözüm:** 
  - Kurye Ayarları'nda "Slot Seçim Günü" = Yarının günü olmalı
  - Örnek: Yarın Pazartesi ise → "MONDAY" seçilmeli (şu an sadece Çarşamba/Perşembe var)

---

## 🎯 En Yaygın Hata: Tarih Sistemi

**Problem:** Uygulama "yarın" için değil, belirli günler için çalışıyor.

**Mevcut Durum:**
- Slot Seçim Günü: Çarşamba veya Perşembe
- Uygulama sadece o günlerde çalışır

**Örnek:**
```
Bugün: Pazar
Yarın: Pazartesi
Slot Seçim Günü Ayarı: Çarşamba

Result: Çalışmaz! Çünkü yarın Pazartesi, ama sistem Çarşamba bekliyor.
```

**Geçici Çözüm:**
1. "Şimdi Kontrol Et" kullan → Bugün için slotları kontrol eder
2. VEYA: Yarının gününe göre ayarı değiştir (kod güncellemesi gerekir)

---

## 📝 Sonraki Adımlar

1. ✅ Gerçek paket adını bul: `./scripts/find-courier-app.sh`
2. ✅ Paket adını kodda güncelle
3. ✅ Yeniden build et: `./gradlew assembleDebug`
4. ✅ APK kur: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
5. ✅ Log'ları izle: `adb logcat | grep CourierAccessibility`
6. ✅ "Şimdi Kontrol Et" ile test et
7. ✅ Log çıktısını analiz et

---

## 🆘 Hala Çalışmıyor mu?

Log çıktısını bize gönderin:

```bash
# Son test'in tüm log'unu kaydet
adb logcat -d | grep -A 10 -B 10 "CourierAccessibility" > debug_logs.txt
```

**debug_logs.txt** dosyasını kontrol ederek sorunu çözebiliriz.
