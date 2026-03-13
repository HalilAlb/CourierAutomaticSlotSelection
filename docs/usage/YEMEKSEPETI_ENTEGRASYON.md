# 🍕 Yemeksepeti Entegrasyonu Rehberi

## ⚠️ ÖNEMLİ: Bu Uygulama Standalone Bir Android Uygulaması

Bu proje **bağımsız bir Android uygulaması** olarak geliştirildi. Yemeksepeti'nin kendi uygulamasına eklemek için iki seçeneğiniz var:

---

## 🎯 Seçenek 1: API Entegrasyonu (ÖNERİLEN)

### Yemeksepeti'nin Gerçek API'sini Kullanma

#### 1️⃣ API Endpoint'lerini Değiştir

Dosya: `app/src/main/java/com/example/slotassistant/data/api/RetrofitClient.kt`

```kotlin
private const val BASE_URL = "https://yemeksepeti.com/" // ← Yemeksepeti'nin gerçek API URL'si
```

#### 2️⃣ API Modellerini Güncelle

Yemeksepeti'nin API response formatına göre değiştir:

Dosya: `app/src/main/java/com/example/slotassistant/data/model/Models.kt`

```kotlin
// Yemeksepeti'nin JSON formatına uygun şekilde değiştir
data class Slot(
    @SerializedName("slot_id") val id: String,  // Yemeksepeti'nin field name'i
    @SerializedName("baslangic") val startTime: String,
    @SerializedName("bitis") val endTime: String,
    // ... diğer Yemeksepeti field'ları
)
```

#### 3️⃣ API Endpoint'lerini Değiştir

Dosya: `app/src/main/java/com/example/slotassistant/data/api/SlotApiService.kt`

```kotlin
interface SlotApiService {
    
    // Yemeksepeti'nin gerçek endpoint'i
    @GET("api/v2/delivery/timeslots")  // ← Gerçek endpoint
    suspend fun getAvailableSlots(
        @Query("date") date: String,
        @Header("Authorization") token: String  // Auth token gerekebilir
    ): Response<SlotsResponse>
    
    @POST("api/v2/delivery/reservation")  // ← Gerçek endpoint
    suspend fun reserveSlot(
        @Body request: ReservationRequest,
        @Header("Authorization") token: String
    ): Response<ReservationResponse>
}
```

#### 4️⃣ Authentication Ekle

Yemeksepeti API'si muhtemelen authentication gerektiriyor:

```kotlin
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer YOUR_TOKEN")
            .addHeader("X-User-Id", "YOUR_USER_ID")
            .build()
        return chain.proceed(request)
    }
}
```

---

## 🎯 Seçenek 2: Webview Kullanımı (Kolay Ama Sınırlı)

Eğer Yemeksepeti'nin API'sine erişim yoksa:

1. Yemeksepeti web sitesini WebView ile aç
2. JavaScript injection ile slot bilgilerini çek
3. Otomatik rezervasyon yap

**DİKKAT:** Bu yöntem:
- ❌ Yemeksepeti kullanım koşullarını ihlal edebilir
- ❌ Web sitesi değişince bozulabilir
- ❌ Resmi API kadar güvenilir değil

---

## 🎯 Seçenek 3: Accessibility Service (Root Erişimi Yok)

Android Accessibility Service kullanarak Yemeksepeti uygulamasının ekranını okuma:

```kotlin
class YemeksepetiAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Yemeksepeti uygulamasındaki slot bilgilerini oku
        if (event.packageName == "com.logistics.rider.yemeksepeti") {
            // Ekrandan veri çek
        }
    }
}
```

**DİKKAT:** 
- ❌ Bu da kullanım koşullarını ihlal edebilir
- ❌ Kullanıcı her seferinde Accessibility iznini vermeli

---

## ✅ EN İYİ YÖNTEM: Resmi API Kullanımı

### Adımlar:

1. **Yemeksepeti Developer Portal'a Kayıt Ol**
   - API key/token al
   - Endpoint dökümanlarını incele

2. **Bu Projedeki Dosyaları Güncelle:**
   ```
   app/src/main/java/com/example/slotassistant/
   ├── data/api/
   │   ├── RetrofitClient.kt      ← BASE_URL değiştir
   │   └── SlotApiService.kt      ← Endpoint'leri değiştir
   ├── data/model/
   │   └── Models.kt              ← JSON modellerini uyarla
   ```

3. **API Token'ı Güvenli Şekilde Sakla:**
   ```kotlin
   // local.properties dosyasında
   yemeksepeti.api.key=YOUR_API_KEY
   
   // build.gradle.kts'de
   buildConfigField("String", "API_KEY", getLocalProperty("yemeksepeti.api.key"))
   ```

4. **Test Et**

---

## 🔧 Günlere Özel Slot Özelliği

**SORU:** Her gün için farklı slotlar mı istiyorsunuz?
- **Pazartesi:** 16:00-19:00
- **Salı:** 11:00-14:00
- **Çarşamba:** 18:00-21:00

Şu anda **tüm seçilen günler için aynı slotlar** geçerli. Günlere özel slot sistemi istiyorsan, UI ve veri yapısında büyük değişiklik gerekir.

**Eklememi ister misin?** (Yaklaşık 1-2 saat refactoring gerekir)

---

## 📱 Package Name Değiştirme

Eğer bu uygulamayı kendi projen olarak publish edeceksen:

```bash
# Package name'i değiştir
1. app/build.gradle.kts içinde:
   namespace = "com.yourcompany.slotassistant"
   applicationId = "com.yourcompany.slotassistant"

2. Tüm dosyalarda package name'i değiştir (Refactor > Rename Package)
```

---

## ❓ Sık Sorulan Sorular

### Yemeksepeti bu uygulamanın çalıştığını nasıl anlıyor?

**CEVAP:** Anlamıyor! Bu uygulama:
- Yemeksepeti'nin **API'sini kullanarak** slot bilgilerini çeker
- Package name'e bakılmaz, **API authentication** önemli
- Her Android uygulaması API'yi kullanabilir (token varsa)

### Yemeksepeti bunu engelleyebilir mi?

**EVET!** Eğer:
- Resmi API'yi kullanmıyorsan (scraping yapıyorsan)
- Rate limiting aşıldıysa
- Kullanım koşullarına uygun değilse

### Güvenli mi?

**Resmi API kullanımı güvenli.** Ama:
- ✅ HTTPS kullan
- ✅ API token'ı şifrele
- ✅ Kullanıcı izni al
- ❌ Kullanıcı şifresini saklama

---

## 🚀 Hızlı Başlangıç

1. Yemeksepeti API dökümanlarını edinin
2. `RetrofitClient.kt` içindeki BASE_URL'i değiştirin
3. API modellerini (`Models.kt`) Yemeksepeti formatına uyarlayın
4. Test endpoint ile deneyin
5. Production'a geçin

**Yardım ister misin?** Yemeksepeti'nin API dökümanını paylaşırsan, otomatik entegrasyon kodu yazabilirim.
