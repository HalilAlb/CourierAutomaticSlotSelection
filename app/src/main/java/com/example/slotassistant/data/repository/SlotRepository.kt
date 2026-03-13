package com.example.slotassistant.data.repository

import com.example.slotassistant.data.api.MockSlotApiService
import com.example.slotassistant.data.api.SlotApiService
import com.example.slotassistant.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repository katmanı - Data source ile UI arasındaki köprü
 * Single Source of Truth prensibi
 */
class SlotRepository(
    private val apiService: SlotApiService = MockSlotApiService() // Gerçek uygulamada: RetrofitClient.slotApiService
) {
    
    /**
     * Belirli bir tarih için mevcut slotları getirir
     */
    suspend fun getAvailableSlots(date: String): Result<List<Slot>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAvailableSlots(date)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.Success(body.slots)
                } else {
                    Result.Error(body.message ?: "Bilinmeyen hata")
                }
            } else {
                Result.Error("API hatası: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Bağlantı hatası: ${e.localizedMessage}", e)
        }
    }
    
    /**
     * Slot rezerve eder
     */
    suspend fun reserveSlot(slotId: String, userId: String = "demo_user"): Result<ReservationResponse> = 
        withContext(Dispatchers.IO) {
            try {
                val request = ReservationRequest(slotId, userId)
                val response = apiService.reserveSlot(request)
                
                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!)
                } else {
                    Result.Error("Rezervasyon başarısız: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error("Rezervasyon hatası: ${e.localizedMessage}", e)
            }
        }
    
    /**
     * Kullanıcının tercih ettiği saat aralığında uygun slot olup olmadığını kontrol eder
     */
    suspend fun findMatchingSlot(
        date: String,
        preferredStartTime: String,
        preferredEndTime: String
    ): Result<Slot?> = withContext(Dispatchers.IO) {
        android.util.Log.d("SlotRepository", "━━━ SLOT ARAMA BAŞLADI ━━━")
        android.util.Log.d("SlotRepository", "Aranan Tarih: $date")
        android.util.Log.d("SlotRepository", "Aranan Başlangıç: '$preferredStartTime' (Uzunluk: ${preferredStartTime.length})")
        android.util.Log.d("SlotRepository", "Aranan Bitiş: '$preferredEndTime' (Uzunluk: ${preferredEndTime.length})")
        
        when (val slotsResult = getAvailableSlots(date)) {
            is Result.Success -> {
                val availableSlots = slotsResult.data
                android.util.Log.d("SlotRepository", "Toplam ${availableSlots.size} slot bulundu")
                
                availableSlots.forEachIndexed { index, slot ->
                    android.util.Log.d("SlotRepository", "Slot $index: Başlangıç='${slot.startTime}' (${slot.startTime.length}), Bitiş='${slot.endTime}' (${slot.endTime.length}), Müsait=${slot.isAvailable}")
                    
                    // Detaylı karşılaştırma
                    val startMatches = slot.startTime == preferredStartTime
                    val endMatches = slot.endTime == preferredEndTime
                    android.util.Log.d("SlotRepository", "  → Başlangıç eşleşiyor: $startMatches, Bitiş eşleşiyor: $endMatches, Müsait: ${slot.isAvailable}")
                    
                    if (!startMatches && slot.startTime.trim() == preferredStartTime.trim()) {
                        android.util.Log.w("SlotRepository", "  ⚠️ UYARI: Trim sonrası eşleşiyor! Boşluk karakteri sorunu olabilir")
                    }
                }
                
                val matchingSlot = availableSlots.find { slot ->
                    slot.isAvailable && 
                    slot.startTime == preferredStartTime && 
                    slot.endTime == preferredEndTime
                }
                
                if (matchingSlot != null) {
                    android.util.Log.d("SlotRepository", "✅ SLOT BULUNDU: ${matchingSlot.id} (${matchingSlot.startTime} - ${matchingSlot.endTime})")
                } else {
                    android.util.Log.w("SlotRepository", "❌ EŞLEŞEN SLOT BULUNAMADI!")
                    android.util.Log.w("SlotRepository", "Aranan: $preferredStartTime - $preferredEndTime")
                }
                android.util.Log.d("SlotRepository", "━━━ SLOT ARAMA BİTTİ ━━━")
                
                Result.Success(matchingSlot)
            }
            is Result.Error -> {
                android.util.Log.e("SlotRepository", "API Hatası: ${slotsResult.message}")
                slotsResult
            }
            else -> Result.Error("Beklenmeyen durum")
        }
    }
    
    /**
     * Bir sonraki Çarşamba gününün tarihini döndürür
     */
    fun getNextWednesday(): String {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        
        // Çarşamba = Calendar.WEDNESDAY (4)
        val daysUntilWednesday = when {
            today < Calendar.WEDNESDAY -> Calendar.WEDNESDAY - today
            today == Calendar.WEDNESDAY -> 7 // Bugün Çarşamba ise bir sonraki Çarşamba
            else -> 7 - today + Calendar.WEDNESDAY
        }
        
        calendar.add(Calendar.DAY_OF_MONTH, daysUntilWednesday)
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}
