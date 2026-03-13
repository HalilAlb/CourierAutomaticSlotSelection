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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
                
                Log.d(TAG, "Tercih sayısı: ${timePreferences.size}")
                Log.d(TAG, "Seçilen günler: $selectedDays")
                
                // Tüm tercihleri detaylı logla
                timePreferences.forEachIndexed { index, pref ->
                    Log.d(TAG, "Tercih $index: ID=${pref.id}, Başlangıç='${pref.startTime}' (${pref.startTime.length} kar), Bitiş='${pref.endTime}' (${pref.endTime.length} kar)")
                    // Byte array olarak göster (gizli karakterleri görmek için)
                    Log.d(TAG, "  Başlangıç bytes: ${pref.startTime.toByteArray().joinToString { it.toString() }}")
                    Log.d(TAG, "  Bitiş bytes: ${pref.endTime.toByteArray().joinToString { it.toString() }}")
                }
                
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
                
                Log.d(TAG, "Bugün: $currentDayName")
                
                // TEST: Manuel test için gün kontrolünü atla
                // Otomatik zamanlı çalışma için gün kontrolü yap
                val isManualTest = inputData.getBoolean("manual_test", false)
                
                if (!isManualTest && !selectedDays.contains(currentDayName)) {
                    Log.d(TAG, "Bugün ($currentDayName) kontrol günü değil, seçilen günler: $selectedDays")
                    notificationHelper.showInfoNotification(
                        "Gün Uyuşmuyor",
                        "Bugün ($currentDayName) seçili günlerden değil. Seçili günler: ${selectedDays.joinToString()}"
                    )
                    return@withContext Result.success()
                }
                
                Log.d(TAG, "Kontrol yapılıyor... (Manuel: $isManualTest)")
                
                // Kaç hafta sonrasının slotu aranacak (0=bu hafta, 1=gelecek hafta)
                val weeksAhead = preferencesManager.getWeeksAhead()
                
                // Hedef tarihi hesapla: weeksAhead ve ilk seçili güne göre
                // Örnek: selectedDay=WEDNESDAY, weeksAhead=1 → gelecek haftanın Çarşambası
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val targetDate = calculateTargetDateForApi(selectedDays, weeksAhead, dateFormat)
                
                Log.d(TAG, "Kontrol edilen tarih: $targetDate (weeksAhead=$weeksAhead)")

                
                var foundSlot = false
                var reservedSlot: com.example.slotassistant.data.model.Slot? = null
                
                // Tüm tercih edilen saat aralıklarını kontrol et
                for (preference in timePreferences) {
                    val startTime = preference.startTime
                    val endTime = preference.endTime
                    
                    Log.d(TAG, "Kontrol edilen slot: $startTime - $endTime (Tarih: $targetDate)")
                    
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
                    Log.d(TAG, "✅ Slot rezerve edildi: ${reservedSlot.startTime} - ${reservedSlot.endTime}")
                    notificationHelper.showSuccessNotification(
                        "Slot başarıyla rezerve edildi!",
                        "${reservedSlot.startTime} - ${reservedSlot.endTime}"
                    )
                } else {
                    val prefsText = timePreferences.joinToString("\n") { "• ${it.startTime} - ${it.endTime}" }
                    Log.w(TAG, "❌ Slot bulunamadı. Tercihler:\n$prefsText")
                    Log.w(TAG, "Kontrol edilen tarih: $targetDate")
                    val weekLabel = when (weeksAhead) {
                        0 -> "bu hafta"
                        1 -> "gelecek hafta"
                        else -> "$weeksAhead hafta sonra"
                    }
                    notificationHelper.showFailureNotification(
                        "Hiçbir tercih ettiğiniz saat aralığında slot bulunamadı " +
                        "($weekLabel - $targetDate):\n$prefsText"
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

    /**
     * weeksAhead ve seçili günlere göre API'ye gönderilecek hedef tarihi hesaplar.
     *
     * Mantık (ISO haftası — Pazartesi haftanın başı):
     *   1. Bu haftanın Pazartesisine git
     *   2. weeksAhead kadar hafta ilerle
     *   3. İlk seçili güne (MONDAY, WEDNESDAY…) ayarla
     *
     * Örnek: Bugün Cuma 13/03, WEDNESDAY seçili, weeksAhead=1
     *   → Pzt 09/03 → +1 hafta → Pzt 16/03 → Çarşamba → 18/03 ✅
     */
    private fun calculateTargetDateForApi(
        selectedDays: Set<String>,
        weeksAhead: Int,
        dateFormat: SimpleDateFormat
    ): String {
        val dayOfWeekMap = mapOf(
            "MONDAY"    to Calendar.MONDAY,
            "TUESDAY"   to Calendar.TUESDAY,
            "WEDNESDAY" to Calendar.WEDNESDAY,
            "THURSDAY"  to Calendar.THURSDAY,
            "FRIDAY"    to Calendar.FRIDAY,
            "SATURDAY"  to Calendar.SATURDAY,
            "SUNDAY"    to Calendar.SUNDAY
        )

        // İlk seçili günü al; yoksa bugünü kullan
        val firstDay = selectedDays.firstOrNull()
        val targetDayOfWeek = dayOfWeekMap[firstDay]
            ?: return dateFormat.format(Calendar.getInstance().time)

        val calendar = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY          // ISO: hafta Pazartesi başlar
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) // Bu haftanın Pazartesisine git
            add(Calendar.WEEK_OF_YEAR, weeksAhead)     // weeksAhead hafta ileri
            set(Calendar.DAY_OF_WEEK, targetDayOfWeek) // Hedef güne ayarla
        }

        val result = dateFormat.format(calendar.time)
        Log.d(TAG, "Hedef tarih hesaplandı: $firstDay + $weeksAhead hafta = $result")
        return result
    }
}

