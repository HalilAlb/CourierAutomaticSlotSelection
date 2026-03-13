#!/bin/bash

# Yemeksepeti Express uygulamasının paket adını bulma scripti

echo "🔍 Yemeksepeti Express Paket Adı Bulma"
echo "======================================="
echo ""

# 1. Yüklu tüm yemeksepeti uygulamalarını listele
echo "📦 Yükl\u00fc Yemeksepeti uygulamaları:"
adb shell pm list packages | grep -i yemek

echo ""
echo "🚚 Kurye/Courier ile ilgili uygulamalar:"
adb shell pm list packages | grep -iE "courier|kurye|express"

echo ""
echo "================================"
echo "📱 Şu anda açık olan uygulama:"
adb shell dumpsys window | grep mCurrentFocus

echo ""
echo "================================"
echo "💡 Kullanım:"
echo "1. Yemeksepeti Express uygulamasını telefonunuzda açın"
echo "2. Bu scripti tekrar çalıştırın: ./scripts/find-courier-app.sh"
echo "3. 'mCurrentFocus' satırında gösterilen paket adını kopyalayın"
echo "4. CourierAccessibilityService.kt dosyasında COURIER_APP_PACKAGE değişkenine yapıştırın"
echo ""
echo "Örnek çıktı:"
echo "mCurrentFocus=Window{abc123 u0 com.yemeksepeti.courierexpress/..."
echo "                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
echo "                                 Bu kısım paket adıdır"
