package com.example.standardweather.data.local.model

data class CachedHourlyForecast(
    val dt: Long,
    val temp: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val pop: Double,
    val weather: List<CachedWeatherCondition>
)

data class CachedDailyForecast(
    val dt: Long,
    val temp: CachedDailyTemperature,
    val humidity: Int,
    val windSpeed: Double,
    val pop: Double,
    val summary: String?,
    val weather: List<CachedWeatherCondition>
)

data class CachedDailyTemperature(
    val day: Double,
    val min: Double,
    val max: Double,
    val night: Double
)

data class CachedWeatherCondition(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class CachedWeatherAlert(
    val senderName: String,
    val event: String,
    val start: Long,
    val end: Long,
    val description: String
)
