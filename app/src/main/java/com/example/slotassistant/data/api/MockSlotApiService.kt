package com.example.slotassistant.data.api

import com.example.slotassistant.data.model.*
import kotlinx.coroutines.delay
import retrofit2.Response

/**
 * API'yi simüle eden mock servis.
 * Gerçek uygulamada RetrofitClient.slotApiService kullanılacak.
 * Test ve geliştirme aşamasında backend hazır olmadan çalışabilmek için.
 */
class MockSlotApiService : SlotApiService {
    
    // Simüle edilmiş slot veritabanı
    private val mockSlots = mutableListOf(
        Slot("1", "09:00", "12:00", true, "2026-03-12"),
        Slot("2", "12:00", "15:00", true, "2026-03-12"),
        Slot("3", "15:00", "18:00", false, "2026-03-12"),
        Slot("4", "16:15", "19:15", true, "2026-03-12"), // Kullanıcının istediği slot
        Slot("5", "18:00", "21:00", true, "2026-03-12")
    )
    
    override suspend fun getAvailableSlots(date: String): Response<SlotsResponse> {
        // Network gecikmesini simüle et
        delay(1000)
        
        return Response.success(
            SlotsResponse(
                slots = mockSlots.filter { it.date == date },
                success = true,
                message = "Slots retrieved successfully"
            )
        )
    }
    
    override suspend fun reserveSlot(request: ReservationRequest): Response<ReservationResponse> {
        // Network gecikmesini simüle et
        delay(1500)
        
        val slot = mockSlots.find { it.id == request.slotId }
        
        return if (slot != null && slot.isAvailable) {
            // Slotu rezerve edilmiş olarak işaretle
            val index = mockSlots.indexOf(slot)
            mockSlots[index] = slot.copy(isAvailable = false)
            
            Response.success(
                ReservationResponse(
                    success = true,
                    message = "Slot başarıyla rezerve edildi!",
                    reservationId = "RES-${System.currentTimeMillis()}"
                )
            )
        } else {
            Response.success(
                ReservationResponse(
                    success = false,
                    message = "Slot müsait değil veya bulunamadı."
                )
            )
        }
    }
}
