package com.example.slotassistant.data.api

import com.example.slotassistant.data.model.ReservationRequest
import com.example.slotassistant.data.model.ReservationResponse
import com.example.slotassistant.data.model.SlotsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Slot API Interface
 * Bu örnekte simüle edilmiş endpoint'ler kullanılıyor.
 * Gerçek uygulamada kendi backend URL'inizi kullanın.
 */
interface SlotApiService {
    
    /**
     * Belirli bir tarih için mevcut slotları getirir
     * @param date Format: "2026-03-12"
     */
    @GET("api/slots")
    suspend fun getAvailableSlots(
        @Query("date") date: String
    ): Response<SlotsResponse>
    
    /**
     * Slot rezerve eder
     */
    @POST("api/reserve")
    suspend fun reserveSlot(
        @Body request: ReservationRequest
    ): Response<ReservationResponse>
}
