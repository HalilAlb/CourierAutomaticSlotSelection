#!/bin/bash

# Android Platform Tools PATH Kurulumu
# Bu script adb komutunu kalıcı olarak kullanabilmenizi sağlar

echo "🔧 Android Platform Tools PATH Kurulumu"
echo ""

# Shell tipini tespit et
if [ -n "$ZSH_VERSION" ]; then
    SHELL_CONFIG="$HOME/.zshrc"
    SHELL_NAME="Zsh"
elif [ -n "$BASH_VERSION" ]; then
    SHELL_CONFIG="$HOME/.bash_profile"
    SHELL_NAME="Bash"
else
    SHELL_CONFIG="$HOME/.profile"
    SHELL_NAME="Shell"
fi

echo "Shell: $SHELL_NAME"
echo "Config dosyası: $SHELL_CONFIG"
echo ""

# Homebrew platform-tools konumunu bul
if [ -d "/opt/homebrew/bin" ]; then
    PLATFORM_TOOLS_PATH="/opt/homebrew/bin"
elif [ -d "/usr/local/bin" ]; then
    PLATFORM_TOOLS_PATH="/usr/local/bin"
else
    echo "❌ Homebrew bulunamadı!"
    echo "Lütfen şunu çalıştırın:"
    echo "  brew install --cask android-platform-tools"
    exit 1
fi

# PATH zaten varsa ekleme
if grep -q "$PLATFORM_TOOLS_PATH" "$SHELL_CONFIG" 2>/dev/null; then
    echo "✅ PATH zaten ayarlı!"
else
    echo "📝 PATH ayarlanıyor..."
    echo "" >> "$SHELL_CONFIG"
    echo "# Android Platform Tools" >> "$SHELL_CONFIG"
    echo "export PATH=\"$PLATFORM_TOOLS_PATH:\$PATH\"" >> "$SHELL_CONFIG"
    echo "✅ PATH $SHELL_CONFIG dosyasına eklendi!"
fi

echo ""
echo "🎉 Kurulum tamamlandı!"
echo ""
echo "Şimdi şunlardan birini yapın:"
echo "  1) Terminal'i kapatıp yeniden açın"
echo "  2) Veya şunu çalıştırın: source $SHELL_CONFIG"
echo ""
echo "Test etmek için:"
echo "  adb version"
