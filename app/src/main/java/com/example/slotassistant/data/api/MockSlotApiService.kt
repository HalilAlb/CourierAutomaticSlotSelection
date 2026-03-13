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
    // Bugünden itibaren 21 gün için dinamik olarak oluşturulur
    private val mockSlots: MutableList<Slot> = buildDynamicSlots()

    companion object {
        /** Bugünden itibaren 21 gün boyunca, her gün için 3 saatlik aralıklarda
         *  kapsamlı slotlar üretir. Kullanıcının girdiği herhangi bir saat
         *  bu listede ya tam eşleşme ya da yakın değer olarak bulunabilir. */
        fun buildDynamicSlots(): MutableList<Slot> {
            val list = mutableListOf<Slot>()
            val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val cal = java.util.Calendar.getInstance()

            // Her güne ait slotlar — yaygın kurye saatlerini kapsamlı şekilde listeliyor
            val dailySlots = listOf(
                "09:00" to "12:00",
                "10:00" to "13:00",
                "11:00" to "14:00",
                "12:00" to "15:00",
                "13:00" to "16:00",
                "14:00" to "17:00",
                "15:00" to "18:00",
                "16:00" to "19:00",
                "16:15" to "19:15",
                "17:00" to "20:00",
                "17:30" to "19:45",  // ← Yaygın kurye vardiya saati
                "17:30" to "20:30",
                "18:00" to "21:00",
                "19:00" to "22:00",
                "20:00" to "22:00",
                "20:00" to "23:00",
                "21:00" to "23:45",
                "22:00" to "01:00",
                "23:00" to "02:00",
                "23:45" to "02:45"
            )

            var idCounter = 1
            repeat(21) { dayOffset ->
                val dateCal = cal.clone() as java.util.Calendar
                dateCal.add(java.util.Calendar.DAY_OF_MONTH, dayOffset)
                val dateStr = fmt.format(dateCal.time)

                dailySlots.forEach { (start, end) ->
                    list.add(Slot(idCounter.toString(), start, end, true, dateStr))
                    idCounter++
                }
            }

            android.util.Log.d("MockSlotApi", "Dinamik mock: ${list.size} slot oluşturuldu (21 gün)")
            return list
        }
    }


    override suspend fun getAvailableSlots(date: String): Response<SlotsResponse> {
        // Network gecikmesini simüle et
        delay(1000)
        
        val filteredSlots = mockSlots.filter { it.date == date }
        android.util.Log.d("MockSlotApi", "Tarih: $date için ${filteredSlots.size} slot bulundu")
        filteredSlots.forEach { 
            android.util.Log.d("MockSlotApi", "  - ${it.startTime}-${it.endTime} (ID: ${it.id}, Müsait: ${it.isAvailable})")
        }
        
        return Response.success(
            SlotsResponse(
                slots = filteredSlots,
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
