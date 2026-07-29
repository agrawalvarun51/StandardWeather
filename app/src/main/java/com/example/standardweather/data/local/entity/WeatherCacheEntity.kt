package com.example.standardweather.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.standardweather.data.local.model.CachedDailyForecast
import com.example.standardweather.data.local.model.CachedHourlyForecast
import com.example.standardweather.data.local.model.CachedWeatherAlert

/**
 * Represents the cached weather snapshot for a given city.
 * [fetchedAt] is a Unix timestamp (ms) used for TTL calculation.
 */
@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val cityId: String,           // "lat_lon" composite key
    val cityName: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val timezone: String,
    // Current
    val currentDt: Long,
    val currentTemp: Double,
    val currentFeelsLike: Double,
    val currentHumidity: Int,
    val currentWindSpeed: Double,
    val currentUvi: Double,
    val currentVisibility: Int,
    val currentWeatherId: Int,
    val currentWeatherMain: String,
    val currentWeatherDescription: String,
    val currentWeatherIcon: String,
    val hourly: List<CachedHourlyForecast>,
    val daily: List<CachedDailyForecast>,
    val alerts: List<CachedWeatherAlert>?,
    // Cache metadata
    val fetchedAt: Long = System.currentTimeMillis()
)
