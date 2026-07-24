package com.example.standardweather

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.work.WorkManager
import com.example.standardweather.ui.navigation.WeatherNavHost
import com.example.standardweather.ui.theme.StandardWeatherTheme
import com.example.standardweather.work.WorkManagerScheduler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* ignored – app works without notifications */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Schedule background weather sync
        WorkManagerScheduler.schedulePeriodicSync(WorkManager.getInstance(this))

        setContent {
            StandardWeatherTheme {
                WeatherNavHost()
            }
        }
    }
}
