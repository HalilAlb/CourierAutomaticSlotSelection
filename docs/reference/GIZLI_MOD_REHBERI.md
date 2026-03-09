# 🔒 Gizli Mod Kullanım Rehberi

## ⚠️ ÖNEMLİ UYARILAR

**BU UYGULAMAYI ETİK KURALLARA UYGUN KULLANIN!**

1. **Accessibility Service** Android sistem güvenliği gereği **tamamen gizlenemez**
2. Sistem bildirimi **her zaman görünür** (Android politikası)
3. Ayarlar → Erişilebilirlik'te **görünür olmalı** (zorunlu)
4. Bu özellikler **sadece kişisel kullanım** içindir

---

## 🎯 Yapılan Değişiklikler

### ✅ 1. Launcher'dan Gizlendi
- **Uygulama listesinde görünmez**
- Ana ekran/uygulama çekmecesinde simge yok
- **Ayarlar → Uygulamalar**'da hala görünür (sistem gereksinimi)

### ✅ 2. Açma Yöntemleri

**a) ADB ile (Bilgisayardan):**
```bash
adb shell am start -n com.example.slotassistant/.ui.MainActivity
```

**b) ADB Kablosuz (WiFi):**
```bash
# Telefonunuzun IP'sini öğrenin (Ayarlar → WiFi → Detaylar)
adb connect 192.168.1.XXX:5555
adb shell am start -n com.example.slotassistant/.ui.MainActivity
```

**c) Shortcut Oluştur:**
1. Nova Launcher veya başka bir launcher kullanın
2. "Aktivite" widget'ı ekleyin
3. `Sistem` → `MainActivity` seçin
4. Widget'a istediğiniz ismi verin (ör: "Ayarlar")

**d) Tasker ile:**
```
Task: Uygulamayı Aç
- Action: System → Send Intent
  - Action: android.intent.action.MAIN
  - Package: com.example.slotassistant
  - Class: com.example.slotassistant.ui.MainActivity
```

### ✅ 3. İnsan Benzeri Davranış
```kotlin
// Rastgele gecikmeler (800ms - 2500ms)
humanDelay()

// Karakter karakter yazma (100ms - 300ms arası)
fillTextFieldHumanLike()
```

**Özellikler:**
- ⏱️ Her tıklama arasında rastgele gecikme
- ⌨️ Yazarken karakter karakter, değişken hızda
- 🤖 Bot tespitini zorlaştırır
- 👤 İnsan benzeri davranış kalıpları

### ✅ 4. Sessiz Mod Aktif
```kotlin
NotificationHelper.SILENT_MODE = true  // Varsayılan
```

**Etkiler:**
- 🔕 Bildirimler gösterilmez
- 📳 Titreşim yok
- 💡 LED ışık yok
- 🔔 Sesler yok

**Sessiz modu kapatmak için:**
```kotlin
// NotificationHelper.kt → 24. satır
var SILENT_MODE = false  // Bildirimleri aç
```

### ✅ 5. Debug Modu Kapalı
```kotlin
DEBUG_MODE = false  // Varsayılan
```

**Etkiler:**
- 📝 Loglar minimum seviyede
- 🔍 UI ağacı loglanmaz
- 📊 Performans artışı
- 🔒 Daha gizli çalışma

**Debug modunu açmak için (geliştirme):**
```kotlin
// CourierAccessibilityService.kt → 33. satır
DEBUG_MODE = true
```

### ✅ 6. Uygulama Adı Değişti
- **Eski:** "Slot Assistant"
- **Yeni:** "Sistem"

Daha sade ve dikkat çekmeyen bir isim.

---

## 📱 Kurulum ve Kullanım

### 1️⃣ İlk Kurulum
```bash
# PATH düzelt (sadece bir kere)
./setup-adb.sh && source ~/.zshrc

# Uygulamayı kur
./install.sh
```

### 2️⃣ Accessibility Service'i Aç
Bu adım **zorunlu** ve **gizlenemez**:

1. **Ayarlar** → **Erişilebilirlik**
2. **Sistem** → **Aç**
3. İzinleri onayla

⚠️ **NOT:** Sistem bildirimi gösterecektir: "Sistem ekranınızı gözlemliyor"

### 3️⃣ Uygulamayı Aç (Gizli Yöntemler)

**En kolay:** ADB ile
```bash
adb shell am start -n com.example.slotassistant/.ui.MainActivity
```

**Alternatif:** Widget/Shortcut oluştur

### 4️⃣ Ayarları Yap
1. Kurye bilgilerini gir
2. Slot tercihlerini ekle
3. "Otomatik Kontrolü Başlat"
4. Uygulamayı kapat

### 5️⃣ Arka Planda Çalışır
- WorkManager belirlenen saatte tetiklenir
- AccessibilityService otomatik işlemleri yapar
- **Sessiz mod aktifse bildirim gelmez**

---

## 🔍 Gizlilik Seviyeleri

### 🟢 Seviye 1: Temel Gizlilik (Varsayılan)
```
✅ Launcher'da görünmez
✅ Sessiz bildirimler
✅ İnsan benzeri davranış
❌ Accessibility bildirimi görünür (zorunlu)
❌ Ayarlar → Uygulamalar'da görünür
```

### 🟡 Seviye 2: Orta Gizlilik
```kotlin
// MainActivity.kt'ye ekle (onCreate içine)
window.setFlags(
    WindowManager.LayoutParams.FLAG_SECURE,
    WindowManager.LayoutParams.FLAG_SECURE
)
// Ekran görüntüsü alınamaz, ekran kaydı yapılamaz
```

### 🔴 Seviye 3: Maksimum Gizlilik (Root Gerektirir)
```bash
# System uygulaması yap (root)
adb root
adb remount
adb push app-debug.apk /system/priv-app/Sistem/
adb reboot

# Artık "sistem uygulaması" olarak çalışır
# Daha fazla izin ve daha az görünürlük
```

⚠️ **DİKKAT:** Root işlemleri garanti bozar ve risklidir!

---

## 🛡️ Güvenlik İpuçları

### ✅ Yapılması Gerekenler
1. **Paket adını değiştir:** `com.example.slotassistant` → `com.android.systemui` (dikkat çeker)
2. **Icon değiştir:** Android sistem iconları kullan
3. **Version kontrolü:** Güncellemeleri kontrol et
4. **Logları temizle:** Üretimde `DEBUG_MODE = false`
5. **Şifreleme:** Kurye bilgilerini encrypt et

### ❌ Yapılmaması Gerekenler
1. **Play Store'a yükleme:** Politika ihlali
2. **Başkalarına dağıtma:** Etik ihlal
3. **Kötüye kullanım:** Hukuki sorunlar
4. **Accessibility kötüye kullanma:** Yasaklanabilir

---

## 🔧 Teknik Detaylar

### İnsan Benzeri Gecikme Algoritması
```kotlin
// Minimum: 800ms, Maksimum: 2500ms
val delay = (MIN_HUMAN_DELAY..MAX_HUMAN_DELAY).random()
Thread.sleep(delay)

// Yazma hızı: 100-300ms/karakter
for (char in text) {
    Thread.sleep((100..300).random())
}
```

**Neden?**
- Gerçek insanlar 300-400ms'de tepki verir
- Bot'lar sabit hızda (ör: her zaman 100ms)
- Rastgelelik = insan benzeri

### Accessibility Tespit Önleme
```kotlin
// Event'leri filtreleme
if (event.packageName != COURIER_APP_PACKAGE) return

// Cooldown mekanizması
if (currentTime - lastAction < COOLDOWN) return

// Çift işlem önleme
if (isProcessing) return
```

---

## 📊 Logları İnceleme

### Sessiz Moddayken
```bash
# Sadece kritik hatalar
adb logcat | grep -E "ERROR|FATAL"
```

### Debug Modundayken
```bash
# Tüm detaylar
adb logcat | grep CourierAccessibility
```

### Log Temizleme
```bash
# Logları temizle
adb logcat -c
```

---

## ❓ Sık Sorulan Sorular

### S: Tamamen gizli olabilir mi?
**C:** Hayır. Android güvenlik politikaları gereği:
- Accessibility Service aktif olduğunda sistem bildirimi gösterir
- Ayarlar → Erişilebilirlik'te görünmelidir
- Bu zorunludur ve değiştirilemez

### S: Bildirim nasıl gizlenir?
**C:** Sessiz mod ile bildirimleri kapatabilirsin ama:
- Accessibility sistem bildirimi her zaman görünür
- Uygulama bildirimlerini `SILENT_MODE = true` ile kapatabilirsin

### S: Launcher'a eklenebilir mi?
**C:** Evet, AndroidManifest.xml'de LAUNCHER kategorisini ekle:
```xml
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
</intent-filter>
```

### S: Root gerekli mi?
**C:** Hayır. Temel özellikler root gerektirmez. Sadece sistem uygulaması yapmak için root gerekir.

### S: Bot olarak tespit edilir mi?
**C:** İnsan benzeri davranış özellikleri ile tespit riski azaltılır ama:
- %100 garanti yoktur
- Kurye uygulaması analiz yapabilir
- Aşırı kullanım fark edilebilir

---

## 🚀 Gelişmiş Özellikler

### IP Değiştirme (VPN)
```kotlin
// VPN kullanarak farklı lokasyon
// Not: Ayrı VPN uygulaması gerektirir
```

### Cihaz Parmak İzi Değiştirme
```bash
# Build.prop düzenleme (root gerektirir)
adb root
adb shell
vi /system/build.prop

# Değiştir:
ro.product.model=
ro.product.manufacturer=
ro.build.fingerprint=
```

### Rastgele User-Agent
```kotlin
// Retrofit ile
val client = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val request = chain.request().newBuilder()
            .header("User-Agent", getRandomUserAgent())
            .build()
        chain.proceed(request)
    }
    .build()
```

---

## 📝 Sonuç

Bu uygulama **maksimum gizlilik** için optimize edildi ama:

✅ **Yapabilecekleri:**
- Launcher'dan gizlenmek
- Sessiz çalışmak
- İnsan gibi davranmak
- Arka planda çalışmak

❌ **Yapamayacakları:**
- Accessibility bildirimini gizlemek (sistem kısıtlaması)
- %100 tespit edilmemek (garanti yoktur)
- Ayarlar'dan gizlenmek (yasal gereklilik)

**Sonuç:** Makul seviyede gizlilik sağlar, ama **dikkatli kullanın**!

---

## ⚖️ Yasal Uyarı

Bu uygulama **eğitim amaçlı** geliştirilmiştir. Kullanıcı:
- Yasalara uygun kullanmaktan sorumludur
- Kurye uygulamasının kullanım şartlarına uymakla yükümlüdür
- Etik kurallara uygun davranmalıdır

**Kötüye kullanımdan geliştirici sorumlu değildir.**
