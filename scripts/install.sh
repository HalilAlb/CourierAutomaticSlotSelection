#!/bin/bash

# Renkli çıktı
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}╔════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  🚀 Slot Assistant Güncelleme     ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════╝${NC}"
echo ""

# adb PATH kontrolü ve ekleme
if ! command -v adb &> /dev/null; then
    echo -e "${BLUE}📱 adb bulunamadı, PATH ekleniyor...${NC}"
    
    # Homebrew konumlarını kontrol et
    if [ -f "/opt/homebrew/bin/adb" ]; then
        export PATH="/opt/homebrew/bin:$PATH"
        echo -e "${GREEN}✅ adb bulundu: /opt/homebrew/bin/adb${NC}"
    elif [ -f "/usr/local/bin/adb" ]; then
        export PATH="/usr/local/bin:$PATH"
        echo -e "${GREEN}✅ adb bulundu: /usr/local/bin/adb${NC}"
    else
        echo -e "${RED}❌ Hata: adb bulunamadı!${NC}"
        echo "Android platform-tools kurulu değil. Lütfen şunu çalıştırın:"
        echo "  brew install --cask android-platform-tools"
        echo ""
        echo "Sonra terminal'i kapatıp tekrar açın veya şunu çalıştırın:"
        echo "  export PATH=\"/opt/homebrew/bin:\$PATH\""
        exit 1
    fi
fi

# Cihaz kontrolü
if ! adb devices | grep -q "device$"; then
    echo -e "${RED}❌ Hata: Cihaz bulunamadı!${NC}"
    echo "USB ile bağlı olduğundan ve USB debugging açık olduğundan emin olun."
    echo ""
    echo "Bağlı cihazlar:"
    adb devices
    exit 1
fi

echo -e "${GREEN}📱 Cihaz bulundu!${NC}"
echo ""

# Build tipi seçimi
echo -e "${BLUE}📦 Hangi versiyonu yüklemek istersiniz?${NC}"
echo "1) Debug (Geliştirme - daha büyük)"
echo "2) Release (Üretim - optimize edilmiş, daha küçük)"
read -p "Seçim (1/2): " -n 1 -r
echo ""

if [[ $REPLY =~ ^[2]$ ]]; then
    BUILD_TYPE="Release"
    BUILD_TASK="assembleRelease"
    APK_PATH="app/build/outputs/apk/release/app-release.apk"
else
    BUILD_TYPE="Debug"
    BUILD_TASK="assembleDebug"
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
fi

# Build
echo -e "${BLUE}📦 $BUILD_TYPE APK build ediliyor...${NC}"
./gradlew $BUILD_TASK --quiet

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Build başarısız!${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Build tamamlandı!${NC}"
echo ""

# Install
echo -e "${BLUE}📲 Cihaza yükleniyor...${NC}"
adb install -r $APK_PATH

if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}╔════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║  ✅ Güncelleme başarılı!          ║${NC}"
    echo -e "${GREEN}╚════════════════════════════════════╝${NC}"
    
    # Uygulamayı başlat
    read -p "Uygulamayı başlatmak ister misiniz? (e/h) " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[EeYy]$ ]]; then
        adb shell am start -n com.example.slotassistant/.ui.MainActivity
        echo -e "${GREEN}🚀 Uygulama başlatıldı!${NC}"
    fi
else
    echo -e "${RED}❌ Yükleme başarısız!${NC}"
    exit 1
fi
