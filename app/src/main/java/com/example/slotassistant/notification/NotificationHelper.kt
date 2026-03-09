package com.example.slotassistant.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.slotassistant.R
import com.example.slotassistant.ui.MainActivity

/**
 * Bildirim yöneticisi
 * Sistem bildirimlerini oluşturur ve gönderir
 */
class NotificationHelper(private val context: Context) {
    
    companion object {
        private const val CHANNEL_ID = "slot_assistant_channel"
        private const val CHANNEL_NAME = "Slot Bildirimleri"
        private const val CHANNEL_DESCRIPTION = "Slot rezervasyon durum bildirimleri"
        const val NOTIFICATION_ID_SUCCESS = 1001
        const val NOTIFICATION_ID_FAILURE = 1002
        const val NOTIFICATION_ID_INFO = 1003
        
        // Sessiz mod - bildirimleri minimize eder
        var SILENT_MODE = true
    }
    
    private val notificationManager = 
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Bildirim kanalı oluşturur (Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = if (SILENT_MODE) {
                NotificationManager.IMPORTANCE_LOW // Sessiz mod
            } else {
                NotificationManager.IMPORTANCE_HIGH
            }
            
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                
                if (!SILENT_MODE) {
                    enableVibration(true)
                    enableLights(true)
                } else {
                    enableVibration(false)
                    enableLights(false)
                    setShowBadge(false)
                }
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Başarılı rezervasyon bildirimi
     */
    fun showSuccessNotification(message: String, slotInfo: String) {
        if (SILENT_MODE) return // Sessiz modda bildirim gösterme
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("✅ Slot Başarıyla Seçildi!")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$message\n\nSlot: $slotInfo"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_SUCCESS, notification)
    }
    
    /**
     * Başarısız rezervasyon bildirimi
     */
    fun showFailureNotification(message: String) {
        if (SILENT_MODE) return // Sessiz modda bildirim gösterme
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("❌ Slot Bulunamadı")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300))
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_FAILURE, notification)
    }
    
    /**
     * Bilgilendirme bildirimi
     */
    fun showInfoNotification(title: String, message: String) {
        if (SILENT_MODE) return // Sessiz modda bildirim gösterme
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_INFO, notification)
    }
}
