package com.example.standardweather

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

const val WEATHER_ALERT_CHANNEL_ID = "weather_alerts"
const val WEATHER_SYNC_CHANNEL_ID = "weather_sync"

@HiltAndroidApp
class WeatherApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)

            nm.createNotificationChannel(
                NotificationChannel(
                    WEATHER_ALERT_CHANNEL_ID,
                    "Weather Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Severe weather warnings" }
            )

            nm.createNotificationChannel(
                NotificationChannel(
                    WEATHER_SYNC_CHANNEL_ID,
                    "Background Sync",
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = "Periodic weather data refresh" }
            )
        }
    }
}
