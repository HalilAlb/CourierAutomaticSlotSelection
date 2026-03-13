package com.example.slotassistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.example.slotassistant.data.model.Result
import com.example.slotassistant.data.model.Slot
import com.example.slotassistant.data.model.TimePreference
import com.example.slotassistant.data.model.WorkDay
import com.example.slotassistant.data.repository.SlotRepository
import com.example.slotassistant.utils.PreferencesManager
import com.example.slotassistant.worker.WorkScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Ana ViewModel - UI state ve business logic yönetimi
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = SlotRepository()
    private val preferencesManager = PreferencesManager(application)
    private val workScheduler = WorkScheduler(application)
    
    // UI State
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    // Tercih edilen saat aralıkları listesi
    private val _timePreferences = MutableStateFlow<List<TimePreference>>(emptyList())
    val timePreferences: StateFlow<List<TimePreference>> = _timePreferences.asStateFlow()
    
    // Seçilen günler
    private val _selectedDays = MutableStateFlow<Set<WorkDay>>(setOf(WorkDay.WEDNESDAY))
    val selectedDays: StateFlow<Set<WorkDay>> = _selectedDays.asStateFlow()
    
    // Zamanlama ayarları
    private val _scheduleHour = MutableStateFlow(11)
    val scheduleHour: StateFlow<Int> = _scheduleHour.asStateFlow()
    
    private val _scheduleMinute = MutableStateFlow(0)
    val scheduleMinute: StateFlow<Int> = _scheduleMinute.asStateFlow()
    
    // Work durumu
    private val _isWorkScheduled = MutableStateFlow(false)
    val isWorkScheduled: StateFlow<Boolean> = _isWorkScheduled.asStateFlow()
    
    // Slot listesi
    private val _availableSlots = MutableStateFlow<List<Slot>>(emptyList())
    val availableSlots: StateFlow<List<Slot>> = _availableSlots.asStateFlow()
    
    // Kurye bilgileri
    private val _courierId = MutableStateFlow("")
    val courierId: StateFlow<String> = _courierId.asStateFlow()
    
    private val _courierPassword = MutableStateFlow("")
    val courierPassword: StateFlow<String> = _courierPassword.asStateFlow()
    
    private val _courierLevel = MutableStateFlow("LEVEL_1")
    val courierLevel: StateFlow<String> = _courierLevel.asStateFlow()
    
    private val _autoDetectLevel = MutableStateFlow(true)
    val autoDetectLevel: StateFlow<Boolean> = _autoDetectLevel.asStateFlow()
    
    private val _slotSelectionDay = MutableStateFlow("WEDNESDAY")
    val slotSelectionDay: StateFlow<String> = _slotSelectionDay.asStateFlow()
    
    private val _courierAppPackage = MutableStateFlow("com.logistics.rider.yemeksepeti")
    val courierAppPackage: StateFlow<String> = _courierAppPackage.asStateFlow()
    
    // Haftalık çalışma günü seçimi
    private val _preferredDayOfWeek = MutableStateFlow("")
    val preferredDayOfWeek: StateFlow<String> = _preferredDayOfWeek.asStateFlow()
    
    private val _weeksAhead = MutableStateFlow(0)
    val weeksAhead: StateFlow<Int> = _weeksAhead.asStateFlow()
    
    // Güncelleme bilgisi
    private val _updateInfo = MutableStateFlow<com.example.slotassistant.utils.UpdateChecker.UpdateInfo?>(null)
    val updateInfo: StateFlow<com.example.slotassistant.utils.UpdateChecker.UpdateInfo?> = _updateInfo.asStateFlow()
    
    init {
        loadPreferences()
        observeWorkStatus()
    }
    
    /**
     * Kaydedilmiş tercihleri yükler
     */
    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesManager.getTimePreferencesFlow().collect { prefs ->
                _timePreferences.value = prefs
            }
        }
        
        viewModelScope.launch {
            preferencesManager.getSelectedDaysFlow().collect { days ->
                _selectedDays.value = days.mapNotNull { dayName ->
                    try {
                        WorkDay.valueOf(dayName)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }.toSet()
            }
        }
        
        viewModelScope.launch {
            preferencesManager.isWorkScheduledFlow().collect { isScheduled ->
                _isWorkScheduled.value = isScheduled
            }
        }
        
        viewModelScope.launch {
            preferencesManager.getCourierLevelFlow().collect { level ->
                _courierLevel.value = level
            }
        }
        
        viewModelScope.launch {
            preferencesManager.getSlotSelectionDayFlow().collect { day ->
                _slotSelectionDay.value = day
            }
        }
        
        viewModelScope.launch {
            preferencesManager.getCourierAppPackageFlow().collect { pkg ->
                _courierAppPackage.value = pkg
            }
        }
        
        _scheduleHour.value = preferencesManager.getScheduleHour()
        _scheduleMinute.value = preferencesManager.getScheduleMinute()
        _courierId.value = preferencesManager.getCourierId()
        _courierPassword.value = preferencesManager.getCourierPassword()
        _autoDetectLevel.value = preferencesManager.isAutoDetectLevel()
        _preferredDayOfWeek.value = preferencesManager.getPreferredDayOfWeek()
        _weeksAhead.value = preferencesManager.getWeeksAhead()
    }
    
    /**
     * Work durumunu gözlemler
     */
    private fun observeWorkStatus() {
        viewModelScope.launch {
            workScheduler.getWorkStatus().observeForever { workInfoList ->
                val isScheduled = workInfoList.any { workInfo ->
                    workInfo.state == WorkInfo.State.ENQUEUED || 
                    workInfo.state == WorkInfo.State.RUNNING
                }
                _isWorkScheduled.value = isScheduled
            }
        }
    }
    
    /**
     * Yeni saat aralığı ekler
     */
    fun addTimePreference(startTime: String, endTime: String) {
        viewModelScope.launch {
            // Format kontrolü ve temizleme
            val cleanStartTime = startTime.trim()
            val cleanEndTime = endTime.trim()
            
            android.util.Log.d("MainViewModel", "Slot ekleniyor: '$cleanStartTime' - '$cleanEndTime'")
            android.util.Log.d("MainViewModel", "Başlangıç uzunluk: ${cleanStartTime.length}, Bitiş uzunluk: ${cleanEndTime.length}")
            
            val newPref = TimePreference(startTime = cleanStartTime, endTime = cleanEndTime)
            val updatedList = _timePreferences.value + newPref
            preferencesManager.setTimePreferences(updatedList)
            _timePreferences.value = updatedList
            _uiState.value = UiState.Success("Slot eklendi: $cleanStartTime - $cleanEndTime")
        }
    }
    
    /**
     * Saat aralığını siler
     */
    fun removeTimePreference(preference: TimePreference) {
        viewModelScope.launch {
            val updatedList = _timePreferences.value.filter { it.id != preference.id }
            preferencesManager.setTimePreferences(updatedList)
            _timePreferences.value = updatedList
            _uiState.value = UiState.Success("Slot silindi")
        }
    }
    
    /**
     * Saat aralığını günceller
     */
    fun updateTimePreference(preference: TimePreference, newStartTime: String, newEndTime: String) {
        viewModelScope.launch {
            // Format kontrolü ve temizleme
            val cleanStartTime = newStartTime.trim()
            val cleanEndTime = newEndTime.trim()
            
            android.util.Log.d("MainViewModel", "Slot güncelleniyor: ID=${preference.id}, Eski: ${preference.startTime}-${preference.endTime}, Yeni: $cleanStartTime-$cleanEndTime")
            
            val updatedList = _timePreferences.value.map { 
                if (it.id == preference.id) {
                    it.copy(startTime = cleanStartTime, endTime = cleanEndTime)
                } else {
                    it
                }
            }
            preferencesManager.setTimePreferences(updatedList)
            _timePreferences.value = updatedList
            _uiState.value = UiState.Success("Slot güncellendi: $cleanStartTime - $cleanEndTime")
        }
    }
    
    /**
     * Gün seçimini günceller
     */
    fun toggleDay(day: WorkDay) {
        viewModelScope.launch {
            val currentDays = _selectedDays.value.toMutableSet()
            if (currentDays.contains(day)) {
                currentDays.remove(day)
            } else {
                currentDays.add(day)
            }
            
            preferencesManager.setSelectedDays(currentDays.map { it.name }.toSet())
            _selectedDays.value = currentDays
        }
    }
    
    /**
     * Zamanlanmış işi planlar
     * Seviye bilgisi varsa, seviyeye göre saat otomatik belirlenir
     */
    fun scheduleWork(hour: Int = -1, minute: Int = -1) {
        viewModelScope.launch {
            try {
                if (_timePreferences.value.isEmpty()) {
                    _uiState.value = UiState.Error("Önce en az bir saat aralığı ekleyin")
                    return@launch
                }
                
                if (_selectedDays.value.isEmpty()) {
                    _uiState.value = UiState.Error("En az bir gün seçmelisiniz")
                    return@launch
                }
                
                // Seviyeye göre saat belirle
                val levelEnum = try {
                    com.example.slotassistant.data.model.CourierLevel.valueOf(_courierLevel.value)
                } catch (e: Exception) {
                    com.example.slotassistant.data.model.CourierLevel.LEVEL_1
                }
                
                val finalHour = if (hour == -1) levelEnum.slotSelectionHour else hour
                val finalMinute = if (minute == -1) levelEnum.slotSelectionMinute else minute
                
                preferencesManager.setScheduleTime(finalHour, finalMinute)
                preferencesManager.setWorkScheduled(true)
                
                workScheduler.scheduleSlotCheckWork(finalHour, finalMinute, _selectedDays.value.toList())
                
                _scheduleHour.value = finalHour
                _scheduleMinute.value = finalMinute
                _isWorkScheduled.value = true
                
                val daysText = _selectedDays.value.joinToString(", ") { it.displayName }
                _uiState.value = UiState.Success(
                    "Otomatik kontrol planlandı:\n$daysText günleri saat ${finalHour.toString().padStart(2, '0')}:${finalMinute.toString().padStart(2, '0')}\n(Seviye ${levelEnum.priority} - ${levelEnum.getSlotSelectionTime()})"
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Planlama başarısız: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Zamanlanmış işi iptal eder
     */
    fun cancelWork() {
        viewModelScope.launch {
            try {
                workScheduler.cancelSlotCheckWork()
                preferencesManager.setWorkScheduled(false)
                
                _isWorkScheduled.value = false
                _uiState.value = UiState.Success("Otomatik kontrol iptal edildi")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("İptal başarısız: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Test amaçlı hemen slot kontrolü yapar
     */
    fun checkNow() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            try {
                workScheduler.scheduleImmediateSlotCheck()
                _uiState.value = UiState.Success("Slot kontrolü başlatıldı, sonuç bildirimle gelecek")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Kontrol başlatılamadı: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Mevcut slotları getirir (manuel kontrol için)
     */
    fun fetchAvailableSlots(date: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            when (val result = repository.getAvailableSlots(date)) {
                is Result.Success -> {
                    _availableSlots.value = result.data
                    _uiState.value = UiState.Success("${result.data.size} slot bulundu")
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(result.message)
                }
                else -> {}
            }
        }
    }
    
    /**
     * UI durumunu sıfırlar
     */
    fun resetUiState() {
        _uiState.value = UiState.Idle
    }
    
    /**
     * Haftalık çalışma günü ayarlar (örn: "Cu" = Cuma)
     */
    fun setPreferredDayOfWeek(dayOfWeek: String) {
        viewModelScope.launch {
            preferencesManager.setPreferredDayOfWeek(dayOfWeek)
            _preferredDayOfWeek.value = dayOfWeek
            
            val dayName = when(dayOfWeek) {
                "Pt" -> "Pazartesi"
                "Sa" -> "Salı"
                "Ça" -> "Çarşamba"
                "Pe" -> "Perşembe"
                "Cu" -> "Cuma"
                "Ct" -> "Cumartesi"
                "Pz" -> "Pazar"
                "" -> "Bugün"
                else -> dayOfWeek
            }
            
            _uiState.value = UiState.Success("Çalışma günü: $dayName")
        }
    }
    
    /**
     * Kaç hafta sonrası için slot ayırtılacak (0=bu hafta, 1=gelecek hafta)
     */
    fun setWeeksAhead(weeks: Int) {
        viewModelScope.launch {
            preferencesManager.setWeeksAhead(weeks)
            _weeksAhead.value = weeks
            
            val weekText = when(weeks) {
                0 -> "Bu hafta"
                1 -> "Gelecek hafta"
                2 -> "2 hafta sonra"
                else -> "$weeks hafta sonra"
            }
            
            _uiState.value = UiState.Success("Hafta seçimi: $weekText")
        }
    }
    
    // ==================== KURYE SİSTEMİ ====================
    
    /**
     * Kurye giriş bilgilerini kaydeder
     */
    fun saveCourierCredentials(id: String, password: String, level: String, hour: Int, minute: Int) {
        viewModelScope.launch {
            preferencesManager.setCourierCredentials(id, password, level)
            _courierId.value = id
            _courierPassword.value = password
            _courierLevel.value = level
            
            // Kullanıcının belirlediği slot seçim saatini ayarla
            _scheduleHour.value = hour
            _scheduleMinute.value = minute
            preferencesManager.setScheduleTime(hour, minute)
            
            val timeText = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
            _uiState.value = UiState.Success("Kurye bilgileri kaydedildi. Slot seçim saati: $timeText")
        }
    }
    
    /**
     * Otomatik seviye algılama ayarını değiştirir
     */
    fun toggleAutoDetectLevel(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAutoDetectLevel(enabled)
            _autoDetectLevel.value = enabled
        }
    }
    
    /**
     * Slot seçim gününü ayarlar
     */
    fun setSlotSelectionDay(day: String) {
        viewModelScope.launch {
            preferencesManager.setSlotSelectionDay(day)
            _slotSelectionDay.value = day
            
            _uiState.value = UiState.Success("Slot seçim günü kaydedildi: $day")
        }
    }
    
    /**
     * Kurye uygulaması paket adını ayarlar
     */
    fun setCourierAppPackage(packageName: String) {
        viewModelScope.launch {
            preferencesManager.setCourierAppPackage(packageName)
            _courierAppPackage.value = packageName
            
            _uiState.value = UiState.Success("Paket adı kaydedildi: $packageName\n\nAccessibility Service'i yeniden başlatmanız gerekebilir.")
        }
    }
    
    /**
     * Güncelleme bilgisini ayarlar
     */
    fun setUpdateInfo(info: com.example.slotassistant.utils.UpdateChecker.UpdateInfo?) {
        _updateInfo.value = info
    }
    
    /**
     * Güncelleme dialogunu kapatır
     */
    fun dismissUpdateDialog() {
        _updateInfo.value = null
    }
    
    /**
     * UI State sealed class
     */
    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }
}
