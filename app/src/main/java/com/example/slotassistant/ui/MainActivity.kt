package com.example.slotassistant.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.slotassistant.ui.screen.MainScreen
import com.example.slotassistant.ui.theme.SlotAssistantTheme
import com.example.slotassistant.utils.PermissionUtils
import com.example.slotassistant.utils.UpdateChecker
import com.example.slotassistant.viewmodel.MainViewModel
import kotlinx.coroutines.launch

/**
 * Ana Activity - Jetpack Compose ile UI render eder
 */
class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Bildirim iznini kontrol et ve gerekirse iste
        if (!PermissionUtils.hasNotificationPermission(this)) {
            PermissionUtils.requestNotificationPermission(this)
        }
        
        setContent {
            SlotAssistantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
        
        // Uygulama açılışında güncelleme kontrolü yap
        lifecycleScope.launch {
            val updateInfo = UpdateChecker.checkForUpdates(this@MainActivity)
            if (updateInfo != null) {
                viewModel.setUpdateInfo(updateInfo)
            }
        }
    }
}
