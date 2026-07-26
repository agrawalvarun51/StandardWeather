package com.example.standardweather.work

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.standardweather.WEATHER_ALERT_CHANNEL_ID
import com.example.standardweather.data.local.dao.WeatherCacheDao
import com.example.standardweather.domain.repository.WeatherRepository
import com.example.standardweather.MainActivity
import com.example.standardweather.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException

private const val TAG = "WeatherSyncWorker"

// Wind speed threshold for extreme weather alert (m/s)
private const val EXTREME_WIND_MPS = 20.0
// Temperature threshold (°C)
private const val EXTREME_TEMP_HIGH = 40.0
private const val EXTREME_TEMP_LOW = -10.0

@HiltWorker
class WeatherSyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val params: WorkerParameters,
    private val repository: WeatherRepository,
    private val weatherCacheDao: WeatherCacheDao
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting periodic weather sync")
        return try {
            // Refresh all cached cities (single-shot query — not a Flow)
            val cachedList = weatherCacheDao.getAllCached()
            cachedList.forEach { entity ->
                val refreshResult = repository.refreshWeather(
                    cityId = entity.cityId,
                    lat = entity.lat,
                    lon = entity.lon,
                    cityName = entity.cityName,
                    country = entity.country
                )
                refreshResult.onSuccess { weather ->
                    checkExtremeWeather(weather.cityName, weather)
                }
            }
            Log.d(TAG, "Weather sync completed successfully")
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Weather sync failed", e)
            Result.retry()
        }
    }

    private fun checkExtremeWeather(
        cityName: String,
        weather: com.example.standardweather.domain.model.WeatherData
    ) {
        val current = weather.current
        val alerts = weather.alerts

        // Check API-provided alerts first
        alerts.forEach { alert ->
            sendNotification(
                id = alert.event.hashCode(),
                title = "⚠️ ${alert.event} — $cityName",
                body = alert.description.take(200)
            )
        }

        // Check thresholds
        if (current.windSpeed >= EXTREME_WIND_MPS) {
            sendNotification(
                id = "wind_${cityName}".hashCode(),
                title = "💨 Strong Winds — $cityName",
                body = "Wind speed: ${"%.1f".format(current.windSpeed)} m/s. Take precautions."
            )
        }
        if (current.temp >= EXTREME_TEMP_HIGH) {
            sendNotification(
                id = "hot_${cityName}".hashCode(),
                title = "🌡️ Extreme Heat — $cityName",
                body = "Current temperature: ${"%.1f".format(current.temp)}°C. Stay hydrated!"
            )
        }
        if (current.temp <= EXTREME_TEMP_LOW) {
            sendNotification(
                id = "cold_${cityName}".hashCode(),
                title = "🥶 Extreme Cold — $cityName",
                body = "Current temperature: ${"%.1f".format(current.temp)}°C. Dress warmly!"
            )
        }
    }

    private fun sendNotification(id: Int, title: String, body: String) {
        val nm = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val tapIntent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext, id, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(appContext, WEATHER_ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        nm.notify(id, notification)
    }
}
