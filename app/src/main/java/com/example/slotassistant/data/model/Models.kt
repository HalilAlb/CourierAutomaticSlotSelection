package com.example.slotassistant.data.model

import com.google.gson.annotations.SerializedName

/**
 * Kullanıcının belirlediği tercih edilen zaman aralığı
 */
data class TimePreference(
    val id: String = System.currentTimeMillis().toString(),
    val startTime: String, // "16:15" formatında
    val endTime: String     // "19:15" formatında
)

/**
 * Günlere özel slot tercihleri
 */
data class DaySlotPreference(
    val day: WorkDay,
    val timePreferences: List<TimePreference> = emptyList()
)

/**
 * Kurye giriş bilgileri
 */
data class CourierCredentials(
    val courierId: String = "",
    val password: String = "",
    val level: CourierLevel = CourierLevel.LEVEL_1
)

/**
 * Kurye seviyeleri ve slot seçim günleri
 */
enum class CourierLevel(
    val displayName: String, 
    val priority: Int, 
    val allowedDays: List<WorkDay>,
    val slotSelectionHour: Int,    // Slot açılma saati
    val slotSelectionMinute: Int   // Slot açılma dakikası
) {
    LEVEL_1("Seviye 1", 1, listOf(WorkDay.MONDAY, WorkDay.TUESDAY), 11, 0),           // 11:00
    LEVEL_2("Seviye 2", 2, listOf(WorkDay.TUESDAY, WorkDay.WEDNESDAY), 11, 15),       // 11:15
    LEVEL_3("Seviye 3", 3, listOf(WorkDay.WEDNESDAY, WorkDay.THURSDAY), 11, 30),      // 11:30
    LEVEL_4("Seviye 4", 4, listOf(WorkDay.THURSDAY, WorkDay.FRIDAY), 11, 45),         // 11:45
    LEVEL_5("Seviye 5", 5, listOf(WorkDay.WEDNESDAY, WorkDay.THURSDAY, WorkDay.FRIDAY), 11, 0); // 11:00 (öncelikli)
    
    fun getSlotSelectionTime(): String {
        return "${slotSelectionHour.toString().padStart(2, '0')}:${slotSelectionMinute.toString().padStart(2, '0')}"
    }
    
    companion object {
        fun fromPriority(priority: Int): CourierLevel {
            return values().find { it.priority == priority } ?: LEVEL_1
        }
        
        fun fromName(name: String): CourierLevel {
            return try {
                valueOf(name)
            } catch (e: IllegalArgumentException) {
                LEVEL_1
            }
        }
    }
}

/**
 * Çalışma günleri enum
 */
enum class WorkDay(val displayName: String, val calendarDay: Int) {
    MONDAY("Pazartesi", 2),
    TUESDAY("Salı", 3),
    WEDNESDAY("Çarşamba", 4),
    THURSDAY("Perşembe", 5),
    FRIDAY("Cuma", 6),
    SATURDAY("Cumartesi", 7),
    SUNDAY("Pazar", 1)
}

/**
 * API'den gelen slot bilgisi
 */
data class Slot(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("start_time")
    val startTime: String,
    
    @SerializedName("end_time")
    val endTime: String,
    
    @SerializedName("is_available")
    val isAvailable: Boolean,
    
    @SerializedName("date")
    val date: String // "2026-03-12" formatında
)

/**
 * Slot listesi response
 */
data class SlotsResponse(
    @SerializedName("slots")
    val slots: List<Slot>,
    
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String? = null
)

/**
 * Rezervasyon isteği
 */
data class ReservationRequest(
    @SerializedName("slot_id")
    val slotId: String,
    
    @SerializedName("user_id")
    val userId: String = "demo_user"
)

/**
 * Rezervasyon response
 */
data class ReservationResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("reservation_id")
    val reservationId: String? = null
)

/**
 * API çağrıları için genel Result wrapper
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Exception? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
