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
        when (val slotsResult = getAvailableSlots(date)) {
            is Result.Success -> {
                val matchingSlot = slotsResult.data.find { slot ->
                    slot.isAvailable && 
                    slot.startTime == preferredStartTime && 
                    slot.endTime == preferredEndTime
                }
                Result.Success(matchingSlot)
            }
            is Result.Error -> slotsResult
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
