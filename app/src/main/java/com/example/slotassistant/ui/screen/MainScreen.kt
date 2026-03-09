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
    
    // Güncelleme bilgisi
    val updateInfo by viewModel.updateInfo.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var showAddSlotDialog by remember { mutableStateOf(false) }
    var showScheduleTimePicker by remember { mutableStateOf(false) }
    var showCourierSettingsDialog by remember { mutableStateOf(false) }
    
    var newStartHour by remember { mutableStateOf(16) }
    var newStartMinute by remember { mutableStateOf(15) }
    var newEndHour by remember { mutableStateOf(19) }
    var newEndMinute by remember { mutableStateOf(15) }
    
    var selectedScheduleHour by remember { mutableStateOf(scheduleHour) }
    var selectedScheduleMinute by remember { mutableStateOf(scheduleMinute) }
    
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
                    onRemoveClick = { viewModel.removeTimePreference(it) }
                )
            }
            
            // Gün seçimi
            item {
                DaySelectionSection(
                    selectedDays = selectedDays,
                    onDayToggle = { viewModel.toggleDay(it) }
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
                    selectedScheduleHour = selectedScheduleHour,
                    selectedScheduleMinute = selectedScheduleMinute
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
                            value = newStartHour.toString().padStart(2, '0'),
                            onValueChange = { newStartHour = it.toIntOrNull() ?: newStartHour },
                            label = { Text("Saat") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newStartMinute.toString().padStart(2, '0'),
                            onValueChange = { newStartMinute = it.toIntOrNull() ?: newStartMinute },
                            label = { Text("Dakika") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Text("Bitiş Saati")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newEndHour.toString().padStart(2, '0'),
                            onValueChange = { newEndHour = it.toIntOrNull() ?: newEndHour },
                            label = { Text("Saat") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newEndMinute.toString().padStart(2, '0'),
                            onValueChange = { newEndMinute = it.toIntOrNull() ?: newEndMinute },
                            label = { Text("Dakika") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val start = "${newStartHour.toString().padStart(2, '0')}:${newStartMinute.toString().padStart(2, '0')}"
                    val end = "${newEndHour.toString().padStart(2, '0')}:${newEndMinute.toString().padStart(2, '0')}"
                    viewModel.addTimePreference(start, end)
                    showAddSlotDialog = false
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
    
    // Zamanlama saati seçici
    if (showScheduleTimePicker) {
        AlertDialog(
            onDismissRequest = { showScheduleTimePicker = false },
            title = { Text("Kontrol Saati") },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = selectedScheduleHour.toString().padStart(2, '0'),
                        onValueChange = { selectedScheduleHour = it.toIntOrNull() ?: selectedScheduleHour },
                        label = { Text("Saat") },
                        modifier = Modifier.weight(1f)
                    )
                    Text(":", style = MaterialTheme.typography.headlineMedium)
                    OutlinedTextField(
                        value = selectedScheduleMinute.toString().padStart(2, '0'),
                        onValueChange = { selectedScheduleMinute = it.toIntOrNull() ?: selectedScheduleMinute },
                        label = { Text("Dakika") },
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
        
        // Slot seçim saati - seviye seçilince varsayılanı göster
        val defaultTime = remember(inputLevel) {
            try {
                val level = com.example.slotassistant.data.model.CourierLevel.valueOf(inputLevel)
                level.slotSelectionHour to level.slotSelectionMinute
            } catch (e: Exception) {
                11 to 0
            }
        }
        var inputHour by remember(defaultTime) { mutableStateOf(defaultTime.first.toString()) }
        var inputMinute by remember(defaultTime) { mutableStateOf(defaultTime.second.toString()) }
        
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
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val hour = inputHour.toIntOrNull() ?: 11
                    val minute = inputMinute.toIntOrNull() ?: 0
                    viewModel.saveCourierCredentials(inputId, inputPassword, inputLevel, hour, minute)
                    viewModel.setSlotSelectionDay(inputSelectionDay)
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
                            IconButton(onClick = { onRemoveClick(pref) }) {
                                Icon(Icons.Default.Delete, "Sil", tint = MaterialTheme.colorScheme.error)
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
                Button(onClick = { onSchedule(selectedScheduleHour, selectedScheduleMinute) }, modifier = Modifier.fillMaxWidth()) {
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
                    onRemoveClick = {}
                )
            }
        }
    }
}
