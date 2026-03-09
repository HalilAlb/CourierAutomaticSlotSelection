# 📱 Otomatik Vardiya/Slot Seçim Asistanı

Modern Android mimarisi kullanılarak geliştirilmiş, otomatik slot rezervasyon uygulaması.

## 🎯 Özellikler

- ✅ **Çoklu Slot Tercihi** - Birden fazla saat aralığı belirleme
- ✅ **Çoklu Gün Desteği** - Pazartesi'den Pazar'a tüm günler
- ✅ **Kurye Entegrasyonu** - Kurye uygulamasına otomatik giriş
- ✅ **5 Seviyeli Sistem** - Her seviye için özel zamanlama
- ✅ **Otomatik Slot Seçimi** - AccessibilityService ile otomatik işlem
- ✅ **İnsan Benzeri Davranış** - Bot tespitini önleme mekanizmaları
- ✅ **Sessiz Mod** - Arkaplanda sorunsuz çalışma
- ✅ **Otomatik Güncelleme** - GitHub Releases entegrasyonu
- ✅ **Modern UI** - Jetpack Compose & Material 3
- ✅ **MVVM Mimarisi** - Temiz ve test edilebilir kod

## 📚 Dokümantasyon

### Kurulum Rehberleri
- **[Hızlı Başlangıç](docs/setup/HIZLI_BASLANGIC.md)** - 9 adımda kurulum
- **[APK Yükleme Rehberi](docs/setup/APK_YUKLEME_REHBERI.md)** - APK kurulum detayları

### Kullanım Rehberleri
- **[Kurye Uygulaması Entegrasyonu](docs/usage/KURYE_UYGULAMASI_ENTEGRASYON_REHBERI.md)** - Detaylı entegrasyon
- **[Slot Bulma Açıklaması](docs/usage/SLOT_BULMA_ACIKLAMASI.md)** - UI element bulma sistemi
- **[Yemeksepeti Entegrasyon](docs/usage/YEMEKSEPETI_ENTEGRASYON.md)** - Kurye sistemi detayları

### Referans
- **[Proje Yapısı](docs/reference/PROJECT_STRUCTURE.md)** - Kod organizasyonu
- **[Güncelleme Rehberi](docs/reference/GUNCELLEME_REHBERI.md)** - Otomatik güncelleme
- **[Gizli Mod](docs/reference/GIZLI_MOD_REHBERI.md)** - Bot tespitini önleme

### Scriptler
- **`scripts/install.sh`** - APK otomatik kurulum
- **`scripts/setup-adb.sh`** - ADB PATH ayarı

## 🏗️ Proje Yapısı

```
new-project/
├── app/                           # Android Uygulama
│   └── src/main/java/com/example/slotassistant/
│       ├── data/                  # Data Katmanı
│       │   ├── model/Models.kt
│       │   └── repository/
│       ├── ui/                    # UI Katmanı (Compose)
│       │   ├── screen/MainScreen.kt
│       │   ├── theme/
│       │   └── MainActivity.kt
│       ├── viewmodel/             # ViewModel
│       ├── worker/                # WorkManager
│       ├── accessibility/         # AccessibilityService (Kurye)
│       ├── notification/          # Bildirimler
│       └── utils/                 # Yardımcılar
│
├── docs/                          # Dokümantasyon
│   ├── setup/                     # Kurulum rehberleri
│   ├── usage/                     # Kullanım rehberleri
│   └── reference/                 # Referans dökümanlar
│
└── scripts/                       # Yardımcı scriptler
    ├── install.sh                 # APK kurulum
    └── setup-adb.sh               # ADB ayarı
```

Detaylı proje yapısı için: [PROJECT_STRUCTURE.md](docs/reference/PROJECT_STRUCTURE.md)

## 🔧 Teknoloji Stack

| Kategori | Teknoloji | Versiyon |
|----------|-----------|----------|
| **Dil** | Kotlin | 1.9.22 |
| **UI** | Jetpack Compose | BOM 2024.01.00 |
| | Material 3 | - |
| | Compose Compiler | 1.5.8 |
| **Mimari** | MVVM | - |
| | ViewModel | 2.7.0 |
| | DataStore | 1.0.0 |
| **Network** | Retrofit | 2.9.0 |
| | Gson | 2.10.1 |
| **Background** | WorkManager | 2.9.0 |
| **Accessibility** | AccessibilityService | - |
| **Build** | Gradle | 8.13 |
| | JDK | 17 |
| **SDK** | Min SDK | 26 (Android 8.0) |
| | Target SDK | 34 (Android 14) |

## � Hızlı Başlangıç

### 1. Gereksinimler
- **macOS**: Homebrew yüklü
- **Java**: JDK 17
- **Gradle**: 8.13
- **Android SDK**: Platform Tools

### 2. Kurulum

```bash
# 1. ADB PATH ayarı
./scripts/setup-adb.sh
source ~/.zshrc

# 2. APK kur (Debug veya Release seçimi)
./scripts/install.sh

# 3. Uygulamayı aç ve Accessibility iznini ver
```

**Detaylı kurulum**: [HIZLI_BASLANGIC.md](docs/setup/HIZLI_BASLANGIC.md)

### 3. Kullanım

1. **Kurye Bilgileri**: Kurye ID ve şifresini gir
2. **Seviye Seç**: Kurye seviyesini belirle (LEVEL_1 - LEVEL_5)
3. **Slot Saati**: Manuel olarak slot seçim saatini ayarla
4. **Günler**: Hangi günler aktif olacağını seç
5. **Zamanla**: WorkManager ile otomasyonu başlat

## 🛠️ Build & Deploy

### Debug APK
```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### Release APK (İmzalı)
```bash
./gradlew assembleRelease
# APK: app/build/outputs/apk/release/app-release.apk
```

### Script ile Kurulum
```bash
# Otomatik build ve kurulum
./scripts/install.sh

# Seçenekler:
# 1. Debug APK (hızlı test için)
# 2. Release APK (production için)
```

**APK yükleme sorunları**: [APK_YUKLEME_REHBERI.md](docs/setup/APK_YUKLEME_REHBERI.md)

## 🔐 Özellikler Detayı

### Bot Tespitini Önleme
```kotlin
// İnsan benzeri rastgele gecikmeler
private const val MIN_HUMAN_DELAY = 800L
private const val MAX_HUMAN_DELAY = 2500L

// Karakter karakter yazma
MIN_TYPING_DELAY = 100L
MAX_TYPING_DELAY = 300L
```

**Detaylı bilgi**: [GIZLI_MOD_REHBERI.md](docs/reference/GIZLI_MOD_REHBERI.md)

### 5 Seviyeli Kurye Sistemi
| Seviye | Slot Saati | Öncelik | İzin Verilen Günler |
|--------|-----------|---------|---------------------|
| LEVEL_1 | 11:00 | En Yüksek | Tüm günler |
| LEVEL_2 | 12:00 | Yüksek | Tüm günler |
| LEVEL_3 | 13:00 | Orta | Tüm günler |
| LEVEL_4 | 14:00 | Düşük | Pazartesi-Cuma |
| LEVEL_5 | 15:00 | En Düşük | Pazartesi-Cuma |

**Not**: Manuel olarak da zaman ayarlanabilir.

## 🐛 Sorun Giderme

### AccessibilityService Çalışmıyor
```bash
# Ayarlar > Erişilebilirlik > Slot Assistant
# Servisi aktif edin
```

### ADB Bulunamadı
```bash
# PATH ayarı
./scripts/setup-adb.sh
source ~/.zshrc

# Kontrol
adb --version
```

### Gradle Build Hatası
```bash
# Clean build
./gradlew clean build

# Gradle Wrapper yenile
gradle wrapper --gradle-version 8.13
```

### APK Yüklenmiyor
- **Güvenlik**: Ayarlar > Bilinmeyen Kaynaklar > İzin ver
- **İmzalı APK**: Release APK kullan (debug.keystore ile imzalı)
- **Detay**: [APK_YUKLEME_REHBERI.md](docs/setup/APK_YUKLEME_REHBERI.md)

## 📊 Build Status

✅ **Debug Build**: Başarılı  
✅ **Release Build**: Başarılı (ProGuard aktif)  
✅ **APK İmzalama**: debug.keystore  
✅ **Lint Kontrol**: Devre dışı (release için)

## 📄 Lisans

Bu proje **okul projesi** olarak geliştirilmiştir. Eğitim amaçlı kullanılabilir.

## 🎓 Geliştirici

Modern Android geliştirme best practice'leri kullanılarak oluşturuldu:
- ✅ MVVM Architecture
- ✅ Jetpack Compose
- ✅ Kotlin Coroutines & Flow
- ✅ Material Design 3
- ✅ Clean Architecture
- ✅ ProGuard/R8 Optimizasyonu

---

**Başarılar!** 🚀

Detaylı dokümantasyon için [docs/](docs/) klasörünü inceleyin.
