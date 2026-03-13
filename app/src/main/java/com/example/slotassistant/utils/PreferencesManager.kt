package com.example.slotassistant.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.slotassistant.data.model.TimePreference
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/**
 * DataStore kullanarak kullanıcı tercihlerini yönetir
 */
class PreferencesManager(private val context: Context) {
    
    private val gson = Gson()
    
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
        
        private val TIME_PREFERENCES_LIST = stringPreferencesKey("time_preferences_list")
        private val SELECTED_DAYS = stringPreferencesKey("selected_days")
        private val SCHEDULE_HOUR = stringPreferencesKey("schedule_hour")
        private val SCHEDULE_MINUTE = stringPreferencesKey("schedule_minute")
        private val IS_WORK_SCHEDULED = stringPreferencesKey("is_work_scheduled")
        
        // Kurye bilgileri
        private val COURIER_ID = stringPreferencesKey("courier_id")
        private val COURIER_PASSWORD = stringPreferencesKey("courier_password")
        private val COURIER_LEVEL = stringPreferencesKey("courier_level")
        private val AUTO_DETECT_LEVEL = stringPreferencesKey("auto_detect_level")
        private val SLOT_SELECTION_DAY = stringPreferencesKey("slot_selection_day") // WEDNESDAY veya THURSDAY
        private val COURIER_APP_PACKAGE = stringPreferencesKey("courier_app_package") // Kurye uygulaması paket adı
        
        // Gün seçimi için
        private val PREFERRED_DAY_OF_WEEK = stringPreferencesKey("preferred_day_of_week") // "Cu", "Pe", "Ct", "Pz" vb.
        private val WEEKS_AHEAD = stringPreferencesKey("weeks_ahead") // 0 = bu hafta, 1 = gelecek hafta, 2 = 2 hafta sonra
    }
    
    /**
     * Tercih edilen saat aralıklarını listesini kaydeder
     */
    suspend fun setTimePreferences(preferences: List<TimePreference>) {
        val json = gson.toJson(preferences)
        context.dataStore.edit { prefs ->
            prefs[TIME_PREFERENCES_LIST] = json
        }
    }
    
    /**
     * Seçilen günleri kaydeder
     */
    suspend fun setSelectedDays(days: Set<String>) {
        val json = gson.toJson(days)
        context.dataStore.edit { prefs ->
            prefs[SELECTED_DAYS] = json
        }
    }
    
    /**
     * Zamanlanmış işin çalışma saatini kaydeder
     */
    suspend fun setScheduleTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[SCHEDULE_HOUR] = hour.toString()
            preferences[SCHEDULE_MINUTE] = minute.toString()
        }
    }
    
    /**
     * Work planlandı mı bilgisini kaydeder
     */
    suspend fun setWorkScheduled(isScheduled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_WORK_SCHEDULED] = isScheduled.toString()
        }
    }
    
    /**
     * Tercih edilen saat aralıklarını getirir (blocking)
     * Trim ile temizleyerek döndürüyor - format uyuşmazlıklarını önler
     */
    fun getTimePreferences(): List<TimePreference> = runBlocking {
        context.dataStore.data.map { prefs ->
            val json = prefs[TIME_PREFERENCES_LIST] ?: "[]"
            val type = object : TypeToken<List<TimePreference>>() {}.type
            val rawList = gson.fromJson<List<TimePreference>>(json, type) ?: emptyList()
            
            // Tüm string değerleri trim et
            rawList.map { pref ->
                pref.copy(
                    startTime = pref.startTime.trim(),
                    endTime = pref.endTime.trim()
                )
            }
        }.first()
    }
    
    /**
     * Seçilen günleri getirir (blocking)
     */
    fun getSelectedDays(): Set<String> = runBlocking {
        context.dataStore.data.map { prefs ->
            val json = prefs[SELECTED_DAYS] ?: "[]"
            val type = object : TypeToken<Set<String>>() {}.type
            gson.fromJson<Set<String>>(json, type) ?: setOf("WEDNESDAY")
        }.first()
    }
    
    /**
     * Tercih edilen saat aralıklarını Flow olarak getirir
     * Trim ile temizleyerek döndürüyor
     */
    fun getTimePreferencesFlow(): Flow<List<TimePreference>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[TIME_PREFERENCES_LIST] ?: "[]"
            val type = object : TypeToken<List<TimePreference>>() {}.type
            val rawList = gson.fromJson<List<TimePreference>>(json, type) ?: emptyList()
            
            // Tüm string değerleri trim et
            rawList.map { pref ->
                pref.copy(
                    startTime = pref.startTime.trim(),
                    endTime = pref.endTime.trim()
                )
            }
        }
    }
    
    /**
     * Seçilen günleri Flow olarak getirir
     */
    fun getSelectedDaysFlow(): Flow<Set<String>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[SELECTED_DAYS] ?: "[]"
            val type = object : TypeToken<Set<String>>() {}.type
            gson.fromJson<Set<String>>(json, type) ?: setOf("WEDNESDAY")
        }
    }
    
    /**
     * Zamanlanmış işin saatini getirir
     */
    fun getScheduleHour(): Int = runBlocking {
        context.dataStore.data.map { preferences ->
            preferences[SCHEDULE_HOUR]?.toIntOrNull() ?: 11
        }.first()
    }
    
    /**
     * Zamanlanmış işin dakikasını getirir
     */
    fun getScheduleMinute(): Int = runBlocking {
        context.dataStore.data.map { preferences ->
            preferences[SCHEDULE_MINUTE]?.toIntOrNull() ?: 0
        }.first()
    }
    
    /**
     * Work planlandı mı bilgisini getirir
     */
    fun isWorkScheduled(): Boolean = runBlocking {
        context.dataStore.data.map { preferences ->
            preferences[IS_WORK_SCHEDULED]?.toBoolean() ?: false
        }.first()
    }
    
    /**
     * Work planlandı mı bilgisini Flow olarak getirir
     */
    fun isWorkScheduledFlow(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[IS_WORK_SCHEDULED]?.toBoolean() ?: false
        }
    }
    
    // ==================== KURYE SİSTEMİ ====================
    
    /**
     * Kurye giriş bilgilerini kaydeder
     */
    suspend fun setCourierCredentials(id: String, password: String, level: String) {
        context.dataStore.edit { prefs ->
            prefs[COURIER_ID] = id
            prefs[COURIER_PASSWORD] = password
            prefs[COURIER_LEVEL] = level
        }
    }
    
    /**
     * Kurye ID'yi getirir
     */
    fun getCourierId(): String = runBlocking {
        context.dataStore.data.map { prefs ->
            prefs[COURIER_ID] ?: ""
        }.first()
    }
    
    /**
     * Kurye şifresini getirir
     */
    fun getCourierPassword(): String = runBlocking {
        context.dataStore.data.map { prefs ->
            prefs[COURIER_PASSWORD] ?: ""
        }.first()
    }
    
    /**
     * Kurye seviyesini getirir
     */
    fun getCourierLevel(): String = runBlocking {
        context.dataStore.data.map { prefs ->
            prefs[COURIER_LEVEL] ?: "LEVEL_1"
        }.first()
    }
    
    /**
     * Kurye seviyesi Flow
     */
    fun getCourierLevelFlow(): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[COURIER_LEVEL] ?: "LEVEL_1"
        }
    }
    
    /**
     * Otomatik seviye algılama ayarı
     */
    suspend fun setAutoDetectLevel(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AUTO_DETECT_LEVEL] = enabled.toString()
        }
    }
    
    fun isAutoDetectLevel(): Boolean = runBlocking {
        context.dataStore.data.map { prefs ->
            prefs[AUTO_DETECT_LEVEL]?.toBoolean() ?: true
        }.first()
    }
    
    /**
     * Slot seçim gününü ayarlar (WEDNESDAY veya THURSDAY)
     */
    suspend fun setSlotSelectionDay(day: String) {
        context.dataStore.edit { prefs ->
            prefs[SLOT_SELECTION_DAY] = day
        }
    }
    
    fun getSlotSelectionDay(): String = runBlocking {
        context.dataStore.data.map { prefs ->
            prefs[SLOT_SELECTION_DAY] ?: "WEDNESDAY"
        }.first()
    }
    
    fun getSlotSelectionDayFlow(): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[SLOT_SELECTION_DAY] ?: "WEDNESDAY"
        }
    }
    
    /**
     * Kurye uygulaması paket adını ayarlar
     */
    suspend fun setCourierAppPackage(packageName: String) {
        context.dataStore.edit { prefs ->
            prefs[COURIER_APP_PACKAGE] = packageName
        }
    }
    
    fun getCourierAppPackage(): String = runBlocking {
        context.dataStore.data.map { prefs ->
            prefs[COURIER_APP_PACKAGE] ?: "com.logistics.rider.yemeksepeti"
        }.first()
    }
    
    fun getCourierAppPackageFlow(): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[COURIER_APP_PACKAGE] ?: "com.logistics.rider.yemeksepeti"
        }
    }
    
    // ==================== GÜN SEÇİMİ ====================
    
    /**
     * Tercih edilen haftanın gününü ayarlar
     * @param dayOfWeek Gün kısaltması: "Cu" = Cuma, "Pe" = Perşembe, "Ct" = Cumartesi vb.
     */
    suspend fun setPreferredDayOfWeek(dayOfWeek: String) {
        context.dataStore.edit { prefs ->
            prefs[PREFERRED_DAY_OF_WEEK] = dayOfWeek
        }
    }
    
    /**
     * Tercih edilen haftanın gününü getirir
     * @return Gün kısaltması ("Cu", "Pe" vb.) veya boş string
     */
    fun getPreferredDayOfWeek(): String = runBlocking {
        context.dataStore.data.map { prefs ->
            prefs[PREFERRED_DAY_OF_WEEK] ?: "" // Boş string = bugünü kullan
        }.first()
    }
    
    /**
     * Kaç hafta sonrasını seç
     * @param weeks 0 = bu hafta, 1 = gelecek hafta, 2 = 2 hafta sonra
     */
    suspend fun setWeeksAhead(weeks: Int) {
        context.dataStore.edit { prefs ->
            prefs[WEEKS_AHEAD] = weeks.toString()
        }
    }
    
    /**
     * Kaç hafta sonrasını getirir
     * @return 0 = bu hafta, 1 = gelecek hafta vb.
     */
    fun getWeeksAhead(): Int = runBlocking {
        context.dataStore.data.map { prefs ->
            prefs[WEEKS_AHEAD]?.toIntOrNull() ?: 0
        }.first()
    }
}
