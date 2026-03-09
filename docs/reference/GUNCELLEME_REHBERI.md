# 🔄 Otomatik Güncelleme Rehberi

## 🎯 3 Farklı Güncelleme Yöntemi

---

## 1️⃣ Geliştirme Aşamasında (Hızlı Test)

### Android Studio - Apply Changes

Kod değiştirdikten sonra:

**Klavye Kısayolu:**
```
Mac: ⌘ + \ (Command + Backslash)
Windows/Linux: Ctrl + \
```

**Veya Toolbar Butonları:**
- 🔄 **Apply Changes and Restart Activity** - Activity yeniden başlar
- ⚡ **Apply Code Changes** - Sadece kod güncellenir (en hızlı)
- 🔥 **Run 'app'** - Full rebuild (temiz başlangıç)

### Jetpack Compose Preview (UI için)

MainScreen.kt'yi açtığınızda sağ tarafta **Split/Design** moduna geçin:
```
View > Tool Windows > Split
```

Artık UI değişikliklerini **canlı** görebilirsiniz! 🎨

---

## 2️⃣ Manuel Test (APK Kurulumu)

### Her Güncellemede:

1. **Build APK:**
   ```bash
   ./gradlew assembleDebug
   ```

2. **APK Konumu:**
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Telefona Yükle:**
   ```bash
   # USB ile bağlı telefona yükle
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
   
   **`-r` parametresi:** Var olan uygulamayı siler ve yenisini kurar (veriler korunur)

### Otomatik Script

Dosya: `install.sh`
```bash
#!/bin/bash
echo "📦 APK Build ediliyor..."
./gradlew assembleDebug
echo "📱 Telefona yükleniyor..."
adb install -r app/build/outputs/apk/debug/app-debug.apk
echo "✅ Güncelleme tamamlandı!"
```

Kullanım:
```bash
chmod +x install.sh
./install.sh
```

---

## 3️⃣ Canlı/Üretim Ortamı (Otomatik Güncelleme)

### A) GitHub Releases (Önerilen - Ücretsiz)

#### Adım 1: GitHub Repository Oluşturun

```bash
cd /Users/halilalbayrak/Desktop/new-project
git init
git add .
git commit -m "İlk commit"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/slot-assistant.git
git push -u origin main
```

#### Adım 2: version.json Dosyasını Güncelleyin

Her güncelleme öncesi `version.json` dosyasını düzenleyin:

```json
{
  "versionCode": 2,          ← Her seferinde +1 artırın
  "versionName": "1.1.0",    ← Semantic versioning
  "updateUrl": "https://github.com/YOUR_USERNAME/slot-assistant/releases/download/v1.1.0/app-release.apk",
  "changelog": "🚀 Yeni Özellikler:\n- Çoklu slot desteği\n- Hata düzeltmeleri",
  "forceUpdate": false       ← true yaparsanız zorunlu güncelleme
}
```

#### Adım 3: Release APK Build Edin

```bash
# Release APK oluştur
./gradlew assembleRelease

# APK konumu:
# app/build/outputs/apk/release/app-release.apk
```

#### Adım 4: GitHub Release Oluşturun

1. GitHub'da `Releases > Create a new release`
2. Tag: `v1.1.0` (version.json ile aynı)
3. Title: `Versiyon 1.1.0 - Yeni Özellikler`
4. Description: Changelog'u yapıştırın
5. APK'yı sürükleyip bırakın
6. **Publish release**

#### Adım 5: UpdateChecker.kt'yi Güncelleyin

```kotlin
private const val UPDATE_URL = "https://raw.githubusercontent.com/YOUR_USERNAME/slot-assistant/main/version.json"
```

**Artık uygulama her açıldığında otomatik güncelleme kontrolü yapacak!** 🎉

---

### B) Firebase App Distribution (Profesyonel)

#### Kurulum:

1. **Firebase Console:** https://console.firebase.google.com/
2. Proje oluştur: "Slot Assistant"
3. Android uygulaması ekle

#### build.gradle.kts'ye ekleyin:

```kotlin
plugins {
    id("com.google.gms.google-services") version "4.4.0"
    id("com.google.firebase.appdistribution") version "4.0.1"
}

dependencies {
    implementation("com.google.firebase:firebase-appdistribution:16.0.0-beta13")
}
```

#### google-services.json İndirin:
Firebase Console > Project Settings > Download `google-services.json`
→ `app/` klasörüne kopyalayın

#### Her Güncellemede:

```bash
./gradlew assembleDebug appDistributionUploadDebug
```

**Kuryelere e-posta ile bildirim gider!** 📧

---

### C) Google Play Console (Opsiyonel)

Play Store'a yüklerseniz:
- **Internal Testing:** Sadece test kullanıcıları
- **Closed Testing:** Belirli grup
- **Open Testing:** Herkes test edebilir
- **Production:** Canlı yayın

Her güncelleme:
1. `versionCode` artır (örn: 1 → 2)
2. Release APK build et
3. Play Console'a yükle
4. Google otomatik güncelleme yapar ✅

---

## 🛠️ Hızlı Komutlar Özeti

```bash
# Geliştirme build + yükle
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk

# Release build
./gradlew assembleRelease

# Versiyon bilgisini göster
./gradlew -q printVersionName

# Tüm APK'ları temizle
./gradlew clean
```

---

## 📊 Versiyon Numarası Yönetimi

### build.gradle.kts

```kotlin
defaultConfig {
    versionCode = 2        ← Her güncelleme +1 (1, 2, 3, 4...)
    versionName = "1.1.0"  ← Görünen versiyon
}
```

### Semantic Versioning:
```
MAJOR.MINOR.PATCH
  1  .  1  .  0

1.0.0 → 1.0.1  (Küçük hata düzeltme)
1.0.0 → 1.1.0  (Yeni özellik)
1.0.0 → 2.0.0  (Büyük değişiklik)
```

---

## 🎯 Hangi Yöntemi Seçmeliyim?

| Durum | Yöntem | Otomatik? |
|-------|--------|-----------|
| **Geliştirme (tek cihaz)** | Apply Changes | ⚡ Anında |
| **Test (5-10 cihaz)** | ADB Install Script | 🔄 Manuel |
| **Küçük ekip** | GitHub Releases | ✅ Evet |
| **Profesyonel** | Firebase Distribution | ✅ Evet |
| **Halka açık** | Google Play Store | ✅ Evet |

---

## 🚀 Önerilen Workflow:

1. **Geliştirme:** Android Studio Apply Changes
2. **Test:** `./install.sh` scripti
3. **Canlı:** GitHub Releases + UpdateChecker

---

## ⚠️ Güvenlik Notları

### APK İmzalama (Üretim için zorunlu)

Keystore oluşturun:
```bash
keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias slot-assistant
```

build.gradle.kts'de:
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("release-key.jks")
        storePassword = "ŞIFRE"
        keyAlias = "slot-assistant"
        keyPassword = "ŞIFRE"
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
    }
}
```

**⚠️ release-key.jks dosyasını GİZLİ tutun!**

---

## 📞 Hızlı Yardım

**Uygulamayı güncelledim ama değişiklik görünmüyor:**
→ Uygulamayı kapat, cache temizle, tekrar aç

**UpdateChecker çalışmıyor:**
→ UpdateChecker.kt'deki UPDATE_URL'i kontrol edin
→ version.json dosyasının online erişilebilir olduğundan emin olun

**Apply Changes çalışmıyor:**
→ Full rebuild deneyin: Build > Rebuild Project
→ Invalidate Caches: File > Invalidate Caches > Restart

---

**Şimdi hangi yöntemi kullanmak istersiniz?** 🤔
1. GitHub Releases (otomatik)
2. Firebase Distribution
3. Sadece local script?

Seçtiğinizde detaylı kurulumu yapalım! 🚀
