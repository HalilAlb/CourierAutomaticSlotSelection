# 🚀 Hızlı Başlangıç Kılavuzu

## Projeyi İlk Kez Açıyorsanız

### 1. Android Studio'da Açın
```bash
# Terminalde:
cd /Users/halilalbayrak/Desktop/new-project

# Android Studio'da:
File > Open > new-project klasörünü seçin
```

### 2. Gradle Wrapper Oluşturun
Android Studio terminal'inde şu komutu çalıştırın:
```bash
gradle wrapper --gradle-version 8.2
```

Veya doğrudan gradle-wrapper.jar ekleyin.

### 3. Gradle Senkronizasyonu
- Android Studio otomatik olarak "Sync Now" soracaktır
- Veya: File > Sync Project with Gradle Files

### 4. Emulator veya Cihaz Hazırlayın
- **Emulator**: Tools > Device Manager > Create Device
  - Önerilen: Pixel 6 Pro, API 34 (Android 14)
- **Gerçek Cihaz**: USB Debugging aktif edin

### 5. Uygulamayı Çalıştırın
- Run > Run 'app' (Shift+F10)
- İlk build 2-5 dakika sürebilir

## ⚡ Hızlı Test Senaryosu

### Test Adımları:

1. **Uygulama Açılışı**
   - Bildirim izni iste → İzin Ver

2. **Tercih Ayarlama**
   - Başlangıç: 16:15
   - Bitiş: 19:15
   - "Tercihleri Kaydet" butonuna bas
   - ✅ "Tercihler kaydedildi" mesajı görmeli

3. **Otomatik Kontrol Ayarlama**
   - "Kontrol Saatini Ayarla" → 11:00
   - "Otomatik Kontrolü Başlat" butonuna bas
   - ✅ "Aktif" etiketi görünmeli

4. **Anlık Test**
   - "Şimdi Kontrol Et" butonuna bas
   - ⏳ 2-3 saniye bekle
   - ✅ Bildirim gelmeli: "Slot başarıyla seçildi!" veya "Slot bulunamadı"

## 🎯 Demo Senaryosu

Mock API şu anki tarih için bu slotları sunuyor:
- 09:00 - 12:00 (Müsait)
- 12:00 - 15:00 (Müsait)
- 15:00 - 18:00 (Dolu)
- **16:15 - 19:15 (Müsait)** ← Bizim örnek tercihmiz
- 18:00 - 21:00 (Müsait)

Eğer tercihinizi 16:15-19:15 olarak ayarlarsanız:
1. "Şimdi Kontrol Et" → Slot bulunur
2. Otomatik rezerve edilir
3. ✅ Başarı bildirimi gelir

## 🔧 Sorun Giderme

### "SDK not found" hatası
```bash
# local.properties dosyası oluşturun:
sdk.dir=/Users/kullaniciadi/Library/Android/sdk
```

### "Cannot resolve symbol" hataları
1. File > Invalidate Caches > Invalidate and Restart
2. Build > Clean Project
3. Build > Rebuild Project

### Gradle build çok yavaş
```bash
# gradle.properties dosyasına ekleyin:
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.configureondemand=true
```

### Emulator açılmıyor
- AVD Manager'da cihazı silin ve yeniden oluşturun
- Hardware acceleration (HAXM/Hypervisor) kurulu olmalı

## 📝 Önemli Dosyalar

```
📁 new-project/
├── 📄 README.md                    ← Detaylı dokümantasyon
├── 📄 QUICK_START.md              ← Bu dosya
├── 📄 build.gradle.kts            ← Root build dosyası
├── 📄 settings.gradle.kts         ← Proje ayarları
├── 📁 app/
│   ├── 📄 build.gradle.kts        ← App modül build dosyası
│   ├── 📄 proguard-rules.pro      ← ProGuard kuralları
│   └── 📁 src/main/
│       ├── 📄 AndroidManifest.xml ← İzinler ve konfigürasyon
│       ├── 📁 java/com/example/slotassistant/
│       │   ├── 📁 data/           ← API, Model, Repository
│       │   ├── 📁 ui/             ← Compose ekranlar
│       │   ├── 📁 viewmodel/      ← ViewModel
│       │   ├── 📁 worker/         ← WorkManager
│       │   ├── 📁 notification/   ← Bildirimler
│       │   └── 📁 utils/          ← Yardımcı sınıflar
│       └── 📁 res/                ← Resource dosyalar
```

## 🎨 Kod Değişikliği Yapmak

### API URL Değiştirme
```kotlin
// RetrofitClient.kt dosyasında:
private const val BASE_URL = "https://yourapi.com/"
```

### Mock API'den Gerçek API'ye Geçiş
```kotlin
// SlotRepository.kt dosyasında:
class SlotRepository(
    private val apiService: SlotApiService = RetrofitClient.slotApiService
    // MockSlotApiService() yerine yukarıdakini kullanın
)
```

### Zamanlama Saatini Değiştirme
```kotlin
// WorkScheduler.kt dosyasında:
fun scheduleSlotCheckWork(hourOfDay: Int = 13, minute: Int = 0)
// 11:00 yerine 13:00 için
```

### Test Verilerini Değiştirme
```kotlin
// MockSlotApiService.kt dosyasında mockSlots listesini düzenleyin
```

## 📱 APK Oluşturma

### Debug APK
```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### Release APK (İmzasız)
```bash
./gradlew assembleRelease
```

### APK'yı Cihaza Yükleme
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 🧪 Logları İzleme

Android Studio'da Logcat filtrelerini kullanın:
```
Tag: SlotCheckWorker        → WorkManager işlemleri
Tag: SlotRepository         → API istekleri
Tag: MainViewModel          → ViewModel state değişiklikleri
Tag: NotificationHelper     → Bildirimler
```

## 💡 İpuçları

1. **Hızlı Test**: "Şimdi Kontrol Et" butonu ile Çarşamba'yı beklemeyin
2. **Bildirimler**: Cihaz ses açık olmalı, DND modu kapalı
3. **Pil**: Test ederken pil optimizasyonunu kapatın
4. **Network**: İlk testlerde Mock API kullanın (gerçek backend gerekmez)
5. **Tarih**: Sistem her zaman bir sonraki Çarşamba'yı hedefler

## 📚 Öğrenme Kaynakları

- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Retrofit](https://square.github.io/retrofit/)
- [Material Design 3](https://m3.material.io/)

## ✅ Checklist

Projeyi ilk açtığınızda kontrol edin:

- [ ] Android Studio güncel (2023.1.1+)
- [ ] JDK 17 kurulu
- [ ] Android SDK kurulu
- [ ] Gradle Wrapper oluşturuldu
- [ ] Gradle Sync başarılı
- [ ] Build başarılı
- [ ] Emulator/Cihaz hazır
- [ ] Uygulama çalıştı
- [ ] Bildirim izni verildi
- [ ] Test bildirimi geldi

Hepsi ✅ ise hazırsınız! 🎉

## 🆘 Yardım

Sorun yaşıyorsanız:
1. README.md'deki "Yaygın Sorunlar" bölümüne bakın
2. Build > Clean Project deneyin
3. File > Invalidate Caches > Restart
4. Android Studio'yu yeniden başlatın
5. Projeyi kapatıp tekrar açın

---

🎯 **Başarılar!** Kolay gelsin...
