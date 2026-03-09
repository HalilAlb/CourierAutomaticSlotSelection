package com.example.slotassistant.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

/**
 * Uygulama güncelleme kontrolü yapan yardımcı sınıf
 * 
 * Kullanım:
 * 1. GitHub Releases veya kendi sunucunuzda bir version.json dosyası oluşturun
 * 2. UpdateChecker.checkForUpdates(context) ile kontrol edin
 */
object UpdateChecker {
    
    // Güncelleme bilgilerinin bulunduğu URL
    // GitHub releases için: https://raw.githubusercontent.com/USERNAME/REPO/main/version.json
    private const val UPDATE_URL = "https://raw.githubusercontent.com/YOUR_USERNAME/slot-assistant/main/version.json"
    
    data class UpdateInfo(
        val versionCode: Int,
        val versionName: String,
        val downloadUrl: String,
        val changelog: String,
        val forceUpdate: Boolean
    )
    
    /**
     * Güncelleme kontrolü yapar
     * 
     * version.json formatı:
     * {
     *   "versionCode": 2,
     *   "versionName": "1.1.0",
     *   "updateUrl": "https://github.com/USERNAME/REPO/releases/download/v1.1.0/app-release.apk",
     *   "changelog": "- Yeni özellik\n- Hata düzeltmeleri",
     *   "forceUpdate": false
     * }
     */
    suspend fun checkForUpdates(context: Context): UpdateInfo? {
        return try {
            val updateData = withContext(Dispatchers.IO) {
                val json = URL(UPDATE_URL).readText()
                JSONObject(json)
            }
            
            val currentVersionCode = context.packageManager
                .getPackageInfo(context.packageName, 0).versionCode
            
            val latestVersionCode = updateData.getInt("versionCode")
            
            if (latestVersionCode > currentVersionCode) {
                UpdateInfo(
                    versionCode = latestVersionCode,
                    versionName = updateData.getString("versionName"),
                    downloadUrl = updateData.getString("updateUrl"),
                    changelog = updateData.optString("changelog", "Yeni güncellemeler mevcut"),
                    forceUpdate = updateData.optBoolean("forceUpdate", false)
                )
            } else {
                null // Güncelleme yok
            }
        } catch (e: Exception) {
            // Güncelleme kontrolü başarısız - sessizce devam et
            e.printStackTrace()
            null
        }
    }
    
    /**
     * APK indirme sayfasını açar
     */
    fun downloadUpdate(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
