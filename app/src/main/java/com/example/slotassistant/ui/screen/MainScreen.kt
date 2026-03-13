package com.example.slotassistant.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.slotassistant.data.model.TimePreference
import com.example.slotassistant.data.model.WorkDay
import com.example.slotassistant.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val timePreferences by viewModel.timePreferences.collectAsState()
    val selectedDays by viewModel.selectedDays.collectAsState()
    val scheduleHour by viewModel.scheduleHour.collectAsState()
    val scheduleMinute by viewModel.scheduleMinute.collectAsState()
    val isWorkScheduled by viewModel.isWorkScheduled.collectAsState()
    
    // Kurye bilgileri
    val courierId by viewModel.courierId.collectAsState()
    val courierPassword by viewModel.courierPassword.collectAsState()
    val courierLevel by viewModel.courierLevel.collectAsState()
    val slotSelectionDay by viewModel.slotSelectionDay.collectAsState()
    val courierAppPackage by viewModel.courierAppPackage.collectAsState()
    
    // Haftalık seçim
    val weeksAhead by viewModel.weeksAhead.collectAsState()
    
    // Güncelleme bilgisi
    val updateInfo by viewModel.updateInfo.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Accessibility izin durumu
    var isAccessibilityEnabled by remember { 
        mutableStateOf(
            com.example.slotassistant.utils.PermissionUtils.isAccessibilityServiceEnabled(
                context, 
                com.example.slotassistant.accessibility.CourierAccessibilityService::class.java
            )
        )
    }
    
    var showAddSlotDialog by remember { mutableStateOf(false) }
    var showEditSlotDialog by remember { mutableStateOf<TimePreference?>(null) }
    var showScheduleTimePicker by remember { mutableStateOf(false) }
    var showCourierSettingsDialog by remember { mutableStateOf(false) }
    
    // String olarak tutuyoruz - silme işlemi çalışsın diye
    var newStartHour by remember { mutableStateOf("16") }
    var newStartMinute by remember { mutableStateOf("15") }
    var newEndHour by remember { mutableStateOf("19") }
    var newEndMinute by remember { mutableStateOf("15") }
    
    // Düzenleme için
    var editStartHour by remember { mutableStateOf("16") }
    var editStartMinute by remember { mutableStateOf("15") }
    var editEndHour by remember { mutableStateOf("19") }
    var editEndMinute by remember { mutableStateOf("15") }
    
    var selectedScheduleHour by remember { mutableStateOf(scheduleHour.toString()) }
    var selectedScheduleMinute by remember { mutableStateOf(scheduleMinute.toString()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Otomatik Slot Asistanı") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = {
            if (uiState is MainViewModel.UiState.Success || uiState is MainViewModel.UiState.Error) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.resetUiState() }) {
                            Text("KAPAT")
                        }
                    }
                ) {
                    Text(
                        when (uiState) {
                            is MainViewModel.UiState.Success -> (uiState as MainViewModel.UiState.Success).message
                            is MainViewModel.UiState.Error -> (uiState as MainViewModel.UiState.Error).message
                            else -> ""
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Accessibility İzin Uyarısı
            if (!isAccessibilityEnabled) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Erişilebilirlik İzni Gerekli",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            
                            Text(
                                "Otomatik slot seçimi için Erişilebilirlik Servisi'ni açmanız gerekiyor.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Button(
                                onClick = { 
                                    com.example.slotassistant.utils.PermissionUtils.openAccessibilitySettings(context)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ayarlara Git")
                            }
                            
                            OutlinedButton(
                                onClick = { 
                                    isAccessibilityEnabled = com.example.slotassistant.utils.PermissionUtils.isAccessibilityServiceEnabled(
                                        context, 
                                        com.example.slotassistant.accessibility.CourierAccessibilityService::class.java
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Durumu Yenile")
                            }
                        }
                    }
                }
            } else {
                // İzin verildi bildirimi
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "✅ Erişilebilirlik Servisi Aktif",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            // Kurye Ayarları
            item {
                CourierSettingsSection(
                    courierId = courierId,
                    courierLevel = courierLevel,
                    slotSelectionDay = slotSelectionDay,
                    scheduleHour = scheduleHour,
                    scheduleMinute = scheduleMinute,
                    onSettingsClick = { showCourierSettingsDialog = true }
                )
            }
            
            // Başlık
            item {
                Text(
                    text = "🎯 Slot Tercihleri ve Günler",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Tercih edilen slotlar listesi
            item {
                SlotsListSection(
                    timePreferences = timePreferences,
                    onAddClick = { showAddSlotDialog = true },
                    onEditClick = { pref ->
                        // Mevcut değerleri parse et
                        val startParts = pref.startTime.split(":")
                        val endParts = pref.endTime.split(":")
                        editStartHour = startParts.getOrNull(0) ?: "16"
                        editStartMinute = startParts.getOrNull(1) ?: "15"
                        editEndHour = endParts.getOrNull(0) ?: "19"
                        editEndMinute = endParts.getOrNull(1) ?: "15"
                        showEditSlotDialog = pref
                    },
                    onRemoveClick = { viewModel.removeTimePreference(it) }
                )
            }
            
            // Çalışma günleri ve hafta seçimi
            item {
                WorkScheduleSection(
                    selectedDays = selectedDays,
                    weeksAhead = weeksAhead,
                    onDayToggle = { viewModel.toggleDay(it) },
                    onWeeksAheadChange = { viewModel.setWeeksAhead(it) }
                )
            }
            
            item { Divider() }
            
            // Otomatik kontrol zamanı
            item {
                ScheduleSection(
                    scheduleHour = scheduleHour,
                    scheduleMinute = scheduleMinute,
                    isWorkScheduled = isWorkScheduled,
                    selectedDays = selectedDays,
                    onScheduleTimeClick = { showScheduleTimePicker = true },
                    onSchedule = { h, m -> viewModel.scheduleWork(h, m) },
                    onCancel = { viewModel.cancelWork() },
                    selectedScheduleHour = selectedScheduleHour.toIntOrNull() ?: scheduleHour,
                    selectedScheduleMinute = selectedScheduleMinute.toIntOrNull() ?: scheduleMinute
                )
            }
            
            item { Divider() }
            
            // Test butonu
            item {
                TestSection(
                    onTestClick = { viewModel.checkNow() },
                    isLoading = uiState is MainViewModel.UiState.Loading
                )
            }
            
            // Loading indicator
            if (uiState is MainViewModel.UiState.Loading) {
                item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
    
    // Yeni slot ekleme dialogu
    if (showAddSlotDialog) {
        AlertDialog(
            onDismissRequest = { showAddSlotDialog = false },
            title = { Text("Yeni Slot Ekle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Başlangıç Saati")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newStartHour,
                            onValueChange = { 
                                if (it.length <= 2 && (it.isEmpty() || it.all { c -> c.isDigit() })) {
                                    newStartHour = it
                                }
                            },
                            label = { Text("Saat") },
                            placeholder = { Text("00") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newStartMinute,
                            onValueChange = { 
                                if (it.length <= 2 && (it.isEmpty() || it.all { c -> c.isDigit() })) {
                                    newStartMinute = it
                                }
                            },
                            label = { Text("Dakika") },
                            placeholder = { Text("00") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    
                    Text("Bitiş Saati")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newEndHour,
                            onValueChange = { 
                                if (it.length <= 2 && (it.isEmpty() || it.all { c -> c.isDigit() })) {
                                    newEndHour = it
                                }
                            },
                            label = { Text("Saat") },
                            placeholder = { Text("00") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newEndMinute,
                            onValueChange = { 
                                if (it.length <= 2 && (it.isEmpty() || it.all { c -> c.isDigit() })) {
                                    newEndMinute = it
                                }
                            },
                            label = { Text("Dakika") },
                            placeholder = { Text("00") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // String'leri Int'e çevir, boşsa 0 kullan
                    val startH = newStartHour.toIntOrNull() ?: 0
                    val startM = newStartMinute.toIntOrNull() ?: 0
                    val endH = newEndHour.toIntOrNull() ?: 0
                    val endM = newEndMinute.toIntOrNull() ?: 0
                    
                    val start = "${startH.toString().padStart(2, '0')}:${startM.toString().padStart(2, '0')}"
                    val end = "${endH.toString().padStart(2, '0')}:${endM.toString().padStart(2, '0')}"
                    viewModel.addTimePreference(start, end)
                    showAddSlotDialog = false
                    
                    // Reset değerleri
                    newStartHour = "16"
                    newStartMinute = "15"
                    newEndHour = "19"
                    newEndMinute = "15"
                }) {
                    Text("EKLE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSlotDialog = false }) {
                    Text("İPTAL")
                }
            }
        )
    }
    
    // Düzenleme dialogu
    showEditSlotDialog?.let { pref ->
        AlertDialog(
            onDismissRequest = { showEditSlotDialog = null },
            title = { Text("Slot Düzenle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Başlangıç Saati")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editStartHour,
                            onValueChange = { 
                                if (it.length <= 2 && (it.isEmpty() || it.all { c -> c.isDigit() })) {
                                    editStartHour = it
                                }
                            },
                            label = { Text("Saat") },
                            placeholder = { Text("00") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editStartMinute,
                            onValueChange = { 
                                if (it.length <= 2 && (it.isEmpty() || it.all { c -> c.isDigit() })) {
                                    editStartMinute = it
                                }
                            },
                            label = { Text("Dakika") },
                            placeholder = { Text("00") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    
                    Text("Bitiş Saati")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editEndHour,
                            onValueChange = { 
                                if (it.length <= 2 && (it.isEmpty() || it.all { c -> c.isDigit() })) {
                                    editEndHour = it
                                }
                            },
                            label = { Text("Saat") },
                            placeholder = { Text("00") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editEndMinute,
                            onValueChange = { 
                                if (it.length <= 2 && (it.isEmpty() || it.all { c -> c.isDigit() })) {
                                    editEndMinute = it
                                }
                            },
                            label = { Text("Dakika") },
                            placeholder = { Text("00") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // String'leri Int'e çevir, boşsa 0 kullan
                    val startH = editStartHour.toIntOrNull() ?: 0
                    val startM = editStartMinute.toIntOrNull() ?: 0
                    val endH = editEndHour.toIntOrNull() ?: 0
                    val endM = editEndMinute.toIntOrNull() ?: 0
                    
                    val start = "${startH.toString().padStart(2, '0')}:${startM.toString().padStart(2, '0')}"
                    val end = "${endH.toString().padStart(2, '0')}:${endM.toString().padStart(2, '0')}"
                    viewModel.updateTimePreference(pref, start, end)
                    showEditSlotDialog = null
                }) {
                    Text("GÜNCELLE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditSlotDialog = null }) {
                    Text("İPTAL")
                }
            }
        )
    }
    
    // Zamanlama saati seçici
    if (showScheduleTimePicker) {
        AlertDialog(
            onDismissRequest = { showScheduleTimePicker = false },
            title = { Text("Kontrol Saati") },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = selectedScheduleHour,
                        onValueChange = { 
                            if (it.length <= 2 && (it.isEmpty() || it.all { c -> c.isDigit() })) {
                                selectedScheduleHour = it
                            }
                        },
                        label = { Text("Saat") },
                        placeholder = { Text("00") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Text(":", style = MaterialTheme.typography.headlineMedium)
                    OutlinedTextField(
                        value = selectedScheduleMinute,
                        onValueChange = { 
                            if (it.length <= 2 && (it.isEmpty() || it.all { c -> c.isDigit() })) {
                                selectedScheduleMinute = it
                            }
                        },
                        label = { Text("Dakika") },
                        placeholder = { Text("00") },
                        modifier = Modifier.weight(1f)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showScheduleTimePicker = false }) {
                    Text("TAMAM")
                }
            }
        )
    }
    
    // Kurye Ayarları Dialog
    if (showCourierSettingsDialog) {
        var inputId by remember { mutableStateOf(courierId) }
        var inputPassword by remember { mutableStateOf(courierPassword) }
        var inputLevel by remember { mutableStateOf(courierLevel) }
        var inputSelectionDay by remember { mutableStateOf(slotSelectionDay) }
        var inputPackageName by remember { mutableStateOf(courierAppPackage) }
        
        // Slot seçim saati - mevcut kayıtlı değerleri kullan
        var inputHour by remember { mutableStateOf(scheduleHour.toString()) }
        var inputMinute by remember { mutableStateOf(scheduleMinute.toString()) }
        
        // Seviye değiştiğinde varsayılan saati göster (opsiyonel)
        LaunchedEffect(inputLevel) {
            // Kullanıcı manuel değiştirmediyse, seviyeye göre varsayılanı doldur
            // Ancak kullanıcı zaten bir değer girdiyse, onu koruyalım
        }
        
        AlertDialog(
            onDismissRequest = { showCourierSettingsDialog = false },
            title = { Text("🚚 Kurye Ayarları") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = inputId,
                        onValueChange = { inputId = it },
                        label = { Text("Kurye ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = inputPassword,
                        onValueChange = { inputPassword = it },
                        label = { Text("Şifre") },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { Text("Kurye uygulaması şifresi") }
                    )
                    
                    Text("Seviye", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in 1..5) {
                            FilterChip(
                                selected = inputLevel == "LEVEL_$i",
                                onClick = { inputLevel = "LEVEL_$i" },
                                label = { Text("$i") }
                            )
                        }
                    }
                    
                    Text("Slot Seçim Günü", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = inputSelectionDay == "WEDNESDAY",
                            onClick = { inputSelectionDay = "WEDNESDAY" },
                            label = { Text("Çarşamba") }
                        )
                        FilterChip(
                            selected = inputSelectionDay == "THURSDAY",
                            onClick = { inputSelectionDay = "THURSDAY" },
                            label = { Text("Perşembe") }
                        )
                    }
                    
                    Text("⏰ Slot Seçim Saati", style = MaterialTheme.typography.labelMedium)
                    Text(
                        "Seviyenize göre varsayılan saat otomatik gelir, istediğiniz gibi değiştirebilirsiniz.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = inputHour,
                            onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) inputHour = it },
                            label = { Text("Saat") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Text(":", modifier = Modifier.align(Alignment.CenterVertically), style = MaterialTheme.typography.headlineSmall)
                        OutlinedTextField(
                            value = inputMinute,
                            onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) inputMinute = it },
                            label = { Text("Dakika") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text("📱 Kurye Uygulaması Paket Adı", style = MaterialTheme.typography.labelMedium)
                    Text(
                        "AccessibilityService'in doğru uygulamayı izlemesi için paket adını girin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = inputPackageName,
                        onValueChange = { inputPackageName = it },
                        label = { Text("Paket Adı (örn: com.yemeksepeti.express)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        supportingText = {
                            Text(
                                "💡 Paket adını bulmak için:\nadb shell dumpsys window | grep mCurrentFocus",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val hour = inputHour.toIntOrNull() ?: 11
                    val minute = inputMinute.toIntOrNull() ?: 0
                    viewModel.saveCourierCredentials(inputId, inputPassword, inputLevel, hour, minute)
                    viewModel.setSlotSelectionDay(inputSelectionDay)
                    if (inputPackageName.isNotBlank()) {
                        viewModel.setCourierAppPackage(inputPackageName)
                    }
                    showCourierSettingsDialog = false
                }) {
                    Text("KAYDET")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCourierSettingsDialog = false }) {
                    Text("İPTAL")
                }
            }
        )
    }
    
    // Güncelleme Dialog
    updateInfo?.let { update ->
        AlertDialog(
            onDismissRequest = { if (!update.forceUpdate) viewModel.dismissUpdateDialog() },
            title = { Text("🚀 Yeni Güncelleme Mevcut!") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Versiyon ${update.versionName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        update.changelog,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    com.example.slotassistant.utils.UpdateChecker.downloadUpdate(context, update.downloadUrl)
                    viewModel.dismissUpdateDialog()
                }) {
                    Text("GÜNCELLE")
                }
            },
            dismissButton = {
                if (!update.forceUpdate) {
                    TextButton(onClick = { viewModel.dismissUpdateDialog() }) {
                        Text("DAHA SONRA")
                    }
                }
            }
        )
    }
}

@Composable
fun SlotsListSection(
    timePreferences: List<TimePreference>,
    onAddClick: () -> Unit,
    onEditClick: (TimePreference) -> Unit,
    onRemoveClick: (TimePreference) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tercih Edilen Slotlar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            if (timePreferences.isEmpty()) {
                Text("Henüz slot eklenmedi. Ekle butonuna tıklayın.", style = MaterialTheme.typography.bodyMedium)
            } else {
                timePreferences.forEach { pref ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${pref.startTime} - ${pref.endTime}", style = MaterialTheme.typography.bodyLarge)
                            Row {
                                IconButton(onClick = { onEditClick(pref) }) {
                                    Icon(Icons.Default.Edit, "Düzenle", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { onRemoveClick(pref) }) {
                                    Icon(Icons.Default.Delete, "Sil", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
            
            Button(onClick = onAddClick, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Yeni Slot Ekle")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkScheduleSection(
    selectedDays: Set<WorkDay>,
    weeksAhead: Int,
    onDayToggle: (WorkDay) -> Unit,
    onWeeksAheadChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("📅 Çalışma Planı", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            // Hafta seçimi
            Text("Hangi Hafta:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            
            val weeks = listOf(
                0 to "Bu hafta",
                1 to "Gelecek hafta"
            )
            
            var weekExpanded by remember { mutableStateOf(false) }
            
            ExposedDropdownMenuBox(
                expanded = weekExpanded,
                onExpandedChange = { weekExpanded = !weekExpanded }
            ) {
                OutlinedTextField(
                    value = weeks.find { it.first == weeksAhead }?.second ?: "Bu hafta",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = weekExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                
                ExposedDropdownMenu(
                    expanded = weekExpanded,
                    onDismissRequest = { weekExpanded = false }
                ) {
                    weeks.forEach { (weekNum, weekName) ->
                        DropdownMenuItem(
                            text = { Text(weekName) },
                            onClick = {
                                onWeeksAheadChange(weekNum)
                                weekExpanded = false
                            }
                        )
                    }
                }
            }
            
            Divider()
            
            // Gün seçimi
            Text("Çalışma Günleri:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text("Seçtiğiniz günlerde slot ayırtılacak", style = MaterialTheme.typography.bodySmall)
            
            WorkDay.values().forEach { day ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = selectedDays.contains(day),
                        onCheckedChange = { onDayToggle(day) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        day.displayName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            // Seçim özeti
            if (selectedDays.isNotEmpty()) {
                Divider()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "✅ Seçiminiz:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val weekText = weeks.find { it.first == weeksAhead }?.second ?: "Bu hafta"
                        val daysText = selectedDays.joinToString(", ") { it.displayName }
                        Text(
                            "$weekText $daysText için slot aranacak",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImprovedDaySelectionSection(
    selectedDays: Set<WorkDay>,
    onDayToggle: (WorkDay) -> Unit
) {
    // Bugün hangi gün?
    val calendar = java.util.Calendar.getInstance()
    val today = calendar.get(java.util.Calendar.DAY_OF_WEEK)
    val isMonday = today == java.util.Calendar.MONDAY
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("📅 Çalışma Günleri", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            // Yemeksepeti kuralları açıklaması
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMonday) 
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    else 
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "ℹ️ Yemeksepeti Kurye Slot Kuralları:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (isMonday) {
                        Text(
                            "✅ Bugün PAZARTESİ - Bu haftanın günlerini seçebilirsiniz",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            "📌 Bugün Pazartesi değil - Gelecek haftanın günleri aranacak",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        "• Pazartesi: Bu hafta slotları",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• Çarşamba/Perşembe: Gelecek hafta slotları",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Text("Çalışmak istediğiniz günleri seçin:", style = MaterialTheme.typography.bodyMedium)
            
            WorkDay.values().forEach { day ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = selectedDays.contains(day),
                        onCheckedChange = { onDayToggle(day) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        day.displayName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            // Seçilen günlerin özeti
            if (selectedDays.isNotEmpty()) {
                Divider()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "✅ Seçiminiz:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val weekText = if (isMonday) "Bu hafta" else "Gelecek hafta"
                        val daysText = selectedDays.joinToString(", ") { it.displayName }
                        Text(
                            "$weekText $daysText günleri için slot aranacak",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DaySelectionSection(
    selectedDays: Set<WorkDay>,
    onDayToggle: (WorkDay) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Çalışma Günleri", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            Text("Otomatik kontrol yapılacak günleri seçin:", style = MaterialTheme.typography.bodyMedium)
            
            WorkDay.values().forEach { day ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = selectedDays.contains(day),
                        onCheckedChange = { onDayToggle(day) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(day.displayName)
                }
            }
        }
    }
}

@Composable
fun ScheduleSection(
    scheduleHour: Int,
    scheduleMinute: Int,
    isWorkScheduled: Boolean,
    selectedDays: Set<WorkDay>,
    onScheduleTimeClick: () -> Unit,
    onSchedule: (Int, Int) -> Unit,
    onCancel: () -> Unit,
    selectedScheduleHour: Int,
    selectedScheduleMinute: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isWorkScheduled) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isWorkScheduled) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Otomatik Kontrol", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        if (isWorkScheduled) "Aktif ✅" else "Pasif",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            val daysText = selectedDays.joinToString(", ") { it.displayName }
            Text(
                "Seçilen günler ($daysText) saat ${selectedScheduleHour.toString().padStart(2, '0')}:${selectedScheduleMinute.toString().padStart(2, '0')}'da otomatik kontrol yapılacak",
                style = MaterialTheme.typography.bodyMedium
            )
            
            OutlinedButton(onClick = onScheduleTimeClick, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.AccessTime, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kontrol Saatini Ayarla: ${selectedScheduleHour.toString().padStart(2, '0')}:${selectedScheduleMinute.toString().padStart(2, '0')}")
            }
            
            if (isWorkScheduled) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Otomatik Kontrolü İptal Et")
                }
            } else {
                Button(onClick = { 
                    onSchedule(selectedScheduleHour, selectedScheduleMinute)
                }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Otomatik Kontrolü Başlat")
                }
            }
        }
    }
}

@Composable
fun TestSection(onTestClick: () -> Unit, isLoading: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Science, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Test ve Anlık Kontrol", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            Text("Otomatik kontrolü beklemeden hemen slot kontrolü yapabilirsiniz.", style = MaterialTheme.typography.bodyMedium)
            
            Button(onClick = onTestClick, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.PlayCircle, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isLoading) "Kontrol Ediliyor..." else "Şimdi Kontrol Et")
            }
        }
    }
}

@Composable
fun CourierSettingsSection(
    courierId: String,
    courierLevel: String,
    slotSelectionDay: String,
    scheduleHour: Int,
    scheduleMinute: Int,
    onSettingsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🚚 Kurye Bilgileri", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, "Ayarlar", tint = MaterialTheme.colorScheme.tertiary)
                }
            }
            
            if (courierId.isEmpty()) {
                Text(
                    "⚠️ Kurye bilgileri girilmedi. Ayarlar butonuna tıklayarak bilgilerinizi girin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Kurye ID:", style = MaterialTheme.typography.bodyMedium)
                    Text(courierId, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Seviye:", style = MaterialTheme.typography.bodyMedium)
                    Text(courierLevel.replace("LEVEL_", "Seviye "), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Slot Açılma Günü:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        if (slotSelectionDay == "WEDNESDAY") "Çarşamba" else "Perşembe",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Kullanıcının belirlediği slot seçim saati
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Slot Seçim Saati:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "⏰ ${scheduleHour.toString().padStart(2, '0')}:${scheduleMinute.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    MaterialTheme {
        // Preview için mock ViewModel
        Surface {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CourierSettingsSection(
                    courierId = "KRY12345",
                    courierLevel = "LEVEL_3",
                    slotSelectionDay = "WEDNESDAY",
                    scheduleHour = 11,
                    scheduleMinute = 30,
                    onSettingsClick = {}
                )
                
                SlotsListSection(
                    timePreferences = listOf(
                        TimePreference(startTime = "16:00", endTime = "19:00"),
                        TimePreference(startTime = "20:00", endTime = "22:00")
                    ),
                    onAddClick = {},
                    onEditClick = {},
                    onRemoveClick = {}
                )
            }
        }
    }
}
