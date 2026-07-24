package com.example.standardweather.data.remote.model

import com.google.gson.annotations.SerializedName

data class OneCallResponse(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("current") val current: CurrentWeatherDto,
    @SerializedName("hourly") val hourly: List<HourlyWeatherDto>,
    @SerializedName("daily") val daily: List<DailyWeatherDto>,
    @SerializedName("alerts") val alerts: List<WeatherAlertDto>?
)

data class CurrentWeatherDto(
    @SerializedName("dt") val dt: Long,
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("wind_speed") val windSpeed: Double,
    @SerializedName("uvi") val uvi: Double,
    @SerializedName("visibility") val visibility: Int,
    @SerializedName("weather") val weather: List<WeatherConditionDto>
)

data class HourlyWeatherDto(
    @SerializedName("dt") val dt: Long,
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("wind_speed") val windSpeed: Double,
    @SerializedName("pop") val pop: Double,          // probability of precipitation
    @SerializedName("weather") val weather: List<WeatherConditionDto>
)

data class DailyWeatherDto(
    @SerializedName("dt") val dt: Long,
    @SerializedName("temp") val temp: DailyTempDto,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("wind_speed") val windSpeed: Double,
    @SerializedName("pop") val pop: Double,
    @SerializedName("summary") val summary: String?,
    @SerializedName("weather") val weather: List<WeatherConditionDto>
)

data class DailyTempDto(
    @SerializedName("day") val day: Double,
    @SerializedName("min") val min: Double,
    @SerializedName("max") val max: Double,
    @SerializedName("night") val night: Double
)

data class WeatherConditionDto(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class WeatherAlertDto(
    @SerializedName("sender_name") val senderName: String,
    @SerializedName("event") val event: String,
    @SerializedName("start") val start: Long,
    @SerializedName("end") val end: Long,
    @SerializedName("description") val description: String
)

data class GeocodingDto(
    @SerializedName("name") val name: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("country") val country: String,
    @SerializedName("state") val state: String?
)
