package com.example.slotassistant

import android.app.Application
import android.util.Log

/**
 * Application sınıfı - Uygulama başlatıldığında ilk çalışan sınıf
 */
class SlotAssistantApplication : Application() {
    
    companion object {
        private const val TAG = "SlotAssistantApp"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Uygulama başlatıldı")
        
        // Gerekli başlangıç işlemleri burada yapılabilir
        // Örneğin: WorkManager configuration, Logging setup, vb.
    }
}
