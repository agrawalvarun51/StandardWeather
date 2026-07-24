package com.example.standardweather.domain.model

/**
 * Domain model for the full weather snapshot of a city.
 */
data class WeatherData(
    val cityId: String,
    val cityName: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val timezone: String,
    val current: CurrentWeather,
    val hourly: List<HourlyWeather>,
    val daily: List<DailyWeather>,
    val alerts: List<WeatherAlert>,
    val fetchedAt: Long
)

data class CurrentWeather(
    val dt: Long,
    val temp: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val uvi: Double,
    val visibility: Int,
    val weatherId: Int,
    val weatherMain: String,
    val weatherDescription: String,
    val weatherIcon: String
)

data class HourlyWeather(
    val dt: Long,
    val temp: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val pop: Double,
    val weatherId: Int,
    val weatherMain: String,
    val weatherDescription: String,
    val weatherIcon: String
)

data class DailyWeather(
    val dt: Long,
    val tempDay: Double,
    val tempMin: Double,
    val tempMax: Double,
    val tempNight: Double,
    val humidity: Int,
    val windSpeed: Double,
    val pop: Double,
    val summary: String,
    val weatherId: Int,
    val weatherMain: String,
    val weatherDescription: String,
    val weatherIcon: String
)

data class WeatherAlert(
    val senderName: String,
    val event: String,
    val start: Long,
    val end: Long,
    val description: String
)

data class CitySearchResult(
    val cityId: String,
    val name: String,
    val country: String,
    val state: String?,
    val lat: Double,
    val lon: Double
)
