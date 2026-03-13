package com.example.slotassistant.worker

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import com.example.slotassistant.data.model.WorkDay
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * WorkManager işlemlerini planlar ve yönetir
 */
class WorkScheduler(private val context: Context) {
    
    companion object {
        private const val TAG = "WorkScheduler"
        const val SLOT_CHECK_WORK_TAG = "slot_check_work_tag"
    }
    
    /**
     * Seçilen günlerde belirlenen saatte çalışacak periodic work planlar
     */
    fun scheduleSlotCheckWork(hourOfDay: Int = 11, minute: Int = 0, workDays: List<WorkDay> = listOf(WorkDay.WEDNESDAY)) {
        val currentDate = Calendar.getInstance()
        
        // İlk çalışma gününü hesapla
        val targetDate = getNextWorkDay(currentDate, workDays, hourOfDay, minute)
        
        val delayInMillis = targetDate.timeInMillis - currentDate.timeInMillis
        val delayInMinutes = TimeUnit.MILLISECONDS.toMinutes(delayInMillis)
        
        Log.d(TAG, "İlk çalışma zamanı: ${targetDate.time}")
        Log.d(TAG, "Gecikme (dakika): $delayInMinutes")
        Log.d(TAG, "Seçilen günler: ${workDays.joinToString { it.displayName }}")
        
        // Constraints: Sadece internet bağlantısı olduğunda çalış
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        // Periodic work request (Her gün kontrol edecek, hangi gün olduğunu worker içinde kontrol edeceğiz)
        val workRequest = PeriodicWorkRequestBuilder<SlotCheckWorker>(
            1, TimeUnit.DAYS, // Her gün çalış
            15, TimeUnit.MINUTES // Flex interval
        )
            .setConstraints(constraints)
            .setInitialDelay(delayInMinutes, TimeUnit.MINUTES)
            .addTag(SLOT_CHECK_WORK_TAG)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                15,
                TimeUnit.MINUTES
            )
            .build()
        
        // Önceki işi iptal et ve yenisini planla
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SlotCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
        
        val daysText = workDays.joinToString(", ") { it.displayName }
        Log.d(TAG, "Slot kontrolü work planlandı ($daysText günleri $hourOfDay:${minute.toString().padStart(2, '0')})")
    }
    
    /**
     * Seçilen günlerden bir sonraki çalışma gününü hesaplar
     */
    private fun getNextWorkDay(currentDate: Calendar, workDays: List<WorkDay>, hourOfDay:Int, minute: Int): Calendar {
        val targetDate = currentDate.clone() as Calendar
        targetDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
        targetDate.set(Calendar.MINUTE, minute)
        targetDate.set(Calendar.SECOND, 0)
        targetDate.set(Calendar.MILLISECOND, 0)
        
        // Eğer bugün çalışma günlerinden biriyse ve saat geçmemişse bugünü kullan
        val currentDay = currentDate.get(Calendar.DAY_OF_WEEK)
        val isToday = workDays.any { it.calendarDay == currentDay }
        
        if (isToday && targetDate.after(currentDate)) {
            return targetDate
        }
        
        // Aksi halde en yakın çalışma gününü bul
        for (i in 1..7) {
            targetDate.add(Calendar.DAY_OF_MONTH, 1)
            val dayOfWeek = targetDate.get(Calendar.DAY_OF_WEEK)
            if (workDays.any { it.calendarDay == dayOfWeek }) {
                return targetDate
            }
        }
        
        return targetDate
    }
    
    /**
     * Test amaçlı: Hemen çalışan bir one-time work planlar
     */
    fun scheduleImmediateSlotCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        // Manuel test olduğunu işaretle
        val inputData = Data.Builder()
            .putBoolean("manual_test", true)
            .build()
        
        val workRequest = OneTimeWorkRequestBuilder<SlotCheckWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag(SLOT_CHECK_WORK_TAG)
            .build()
        
        WorkManager.getInstance(context).enqueue(workRequest)
        
        Log.d(TAG, "Anlık slot kontrolü başlatıldı (Manuel Test)")
    }
    
    /**
     * Planlanmış işi iptal eder
     */
    fun cancelSlotCheckWork() {
        WorkManager.getInstance(context).cancelUniqueWork(SlotCheckWorker.WORK_NAME)
        Log.d(TAG, "Slot kontrolü work iptal edildi")
    }
    
    /**
     * Work durumunu kontrol eder
     */
    fun getWorkStatus(): LiveData<List<WorkInfo>> {
        return WorkManager.getInstance(context)
            .getWorkInfosByTagLiveData(SLOT_CHECK_WORK_TAG)
    }
}
