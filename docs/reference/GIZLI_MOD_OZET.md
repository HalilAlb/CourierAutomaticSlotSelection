# 🔒 Gizli Mod - Hızlı Başlangıç

## ✅ Yapılan Değişiklikler

### 1. Launcher'dan Gizleme ✅
- Uygulama listesinde **görünmez**
- Ana ekran simgesi **yok**

### 2. Açma Yöntemi
```bash
# ADB ile aç
adb shell am start -n com.example.slotassistant/.ui.MainActivity
```

### 3. İnsan Benzeri Davranış ✅
- 🎲 Rastgele gecikmeler (800-2500ms)
- ⌨️ Karakter karakter yazma
- 👤 Bot tespitini zorlaştırır

### 4. Sessiz Mod ✅
- 🔕 Bildirimler kapalı
- 📳 Titreşim yok
- 💡 LED yok

### 5. Debug Kapalı ✅
- 📝 Minimum log
- 🔒 Daha gizli

### 6. Uygulama Adı
- "Sistem" (sade isim)

---

## ⚠️ ÖNEMLI UYARILAR

1. **Accessibility bildirimi gizlenemez** (Android güvenliği)
2. **Ayarlar → Erişilebilirlik'te görünür** (zorunlu)
3. **Tamamen gizli OLAMAZ** (sistem kısıtlaması)

---

## 🚀 Kullanım

### 1. Kur
```bash
./install.sh
```

### 2. Accessibility Aç
Ayarlar → Erişilebilirlik → Sistem → **AÇ**

### 3. Uygulamayı Aç
```bash
adb shell am start -n com.example.slotassistant/.ui.MainActivity
```

### 4. Ayarları Yap
- Kurye bilgileri
- Slot tercihleri
- Otomatik kontrolü başlat

### 5. Kapat
Arka planda otomatik çalışır, sessizce ✅

---

## 📚 Detaylı Rehber

[GIZLI_MOD_REHBERI.md](GIZLI_MOD_REHBERI.md) dosyasına bakın.

---

## 💡 Alternatif Açma Yöntemleri

### Widget ile
1. Nova Launcher kullan
2. Aktivite widget ekle
3. "Sistem → MainActivity" seç

### Tasker ile
```
Send Intent:
- Package: com.example.slotassistant
- Class: .ui.MainActivity
```

---

## ⚖️ Yasal Uyarı

**ETİK KURALLARA UYGUN KULLANIN!**

Sadece kişisel kullanım içindir. Kötüye kullanımdan sorumlu değiliz.

---

Build başarılı ✅ APK hazır!
