package com.example.slotassistant.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.slotassistant.data.model.Result
import com.example.slotassistant.data.repository.SlotRepository
import com.example.slotassistant.notification.NotificationHelper
import com.example.slotassistant.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Slot kontrolü ve rezervasyon yapan Worker
 * Her Çarşamba günü belirlenen saatte çalışır
 */
class SlotCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        const val TAG = "SlotCheckWorker"
        const val WORK_NAME = "slot_check_work"
    }
    
    private val repository = SlotRepository()
    private val notificationHelper = NotificationHelper(applicationContext)
    private val preferencesManager = PreferencesManager(applicationContext)
    
    override suspend fun doWork(): Result {
        Log.d(TAG, "SlotCheckWorker başlatıldı")
        
        return withContext(Dispatchers.IO) {
            try {
                // Kullanıcının tercihlerini al
                val timePreferences = preferencesManager.getTimePreferences()
                val selectedDays = preferencesManager.getSelectedDays()
                
                if (timePreferences.isEmpty()) {
                    Log.w(TAG, "Saat aralığı tercihi ayarlanmamış")
                    notificationHelper.showInfoNotification(
                        "Uyarı",
                        "Lütfen tercih ettiğiniz saat aralıklarını ayarlayın"
                    )
                    return@withContext Result.success()
                }
                
                // Bugünün hangi gün olduğunu kontrol et
                val today = Calendar.getInstance()
                val currentDayOfWeek = today.get(Calendar.DAY_OF_WEEK)
                val currentDayName = when (currentDayOfWeek) {
                    Calendar.MONDAY -> "MONDAY"
                    Calendar.TUESDAY -> "TUESDAY"
                    Calendar.WEDNESDAY -> "WEDNESDAY"
                    Calendar.THURSDAY -> "THURSDAY"
                    Calendar.FRIDAY -> "FRIDAY"
                    Calendar.SATURDAY -> "SATURDAY"
                    Calendar.SUNDAY -> "SUNDAY"
                    else -> "UNKNOWN"
                }
                
                // Bugün seçilen günlerden biri mi kontrol et
                if (!selectedDays.contains(currentDayName)) {
                    Log.d(TAG, "Bugün ($currentDayName) kontrol günü değil")
                    return@withContext Result.success()
                }
                
                Log.d(TAG, "Bugün kontrol günü: $currentDayName")
                
                // Bir sonraki kontrolü yapacağımız tarihi al (bugün veya yarın)
                val targetDate = repository.getNextWednesday() // Bu fonksiyonu genel hale getirmeliyiz
                Log.d(TAG, "Kontrol edilen tarih: $targetDate")
                
                var foundSlot = false
                var reservedSlot: com.example.slotassistant.data.model.Slot? = null
                
                // Tüm tercih edilen saat aralıklarını kontrol et
                for (preference in timePreferences) {
                    val startTime = preference.startTime
                    val endTime = preference.endTime
                    
                    Log.d(TAG, "Kontrol edilen saat: $startTime - $endTime")
                    
                    // Uygun slotu bul
                    val slotResult = repository.findMatchingSlot(targetDate, startTime, endTime)
                    
                    when (slotResult) {
                        is com.example.slotassistant.data.model.Result.Success -> {
                            val slot = slotResult.data
                            
                            if (slot != null) {
                                Log.d(TAG, "Uygun slot bulundu: ${slot.id} ($startTime - $endTime)")
                                
                                // Slotu rezerve et
                                val reservationResult = repository.reserveSlot(slot.id)
                                
                                when (reservationResult) {
                                    is com.example.slotassistant.data.model.Result.Success -> {
                                        val response = reservationResult.data
                                        if (response.success) {
                                            Log.d(TAG, "Rezervasyon başarılı")
                                            foundSlot = true
                                            reservedSlot = slot
                                            break // İlk bulduğumuz slotu aldık, diğerlerini kontrol etmeye gerek yok
                                        }
                                    }
                                    else -> {
                                        Log.e(TAG, "Rezervasyon hatası")
                                    }
                                }
                            }
                        }
                        else -> {
                            Log.d(TAG, "$startTime - $endTime aralığında slot bulunamadı")
                        }
                    }
                }
                
                // Sonuç bildirimi
                if (foundSlot && reservedSlot != null) {
                    notificationHelper.showSuccessNotification(
                        "Slot başarıyla rezerve edildi!",
                        "${reservedSlot.startTime} - ${reservedSlot.endTime}"
                    )
                } else {
                    val prefsText = timePreferences.joinToString("\n") { "• ${it.startTime} - ${it.endTime}" }
                    notificationHelper.showFailureNotification(
                        "Hiçbir tercih ettiğiniz saat aralığında slot bulunamadı:\n$prefsText"
                    )
                }
                
                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "Worker hatası", e)
                notificationHelper.showFailureNotification(
                    "Beklenmeyen bir hata oluştu: ${e.localizedMessage}"
                )
                Result.failure()
            }
        }
    }
}
