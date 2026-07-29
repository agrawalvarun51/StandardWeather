package com.example.standardweather.data.local

import androidx.room.TypeConverter
import com.example.standardweather.data.local.model.CachedDailyForecast
import com.example.standardweather.data.local.model.CachedHourlyForecast
import com.example.standardweather.data.local.model.CachedWeatherAlert
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WeatherTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromHourlyForecasts(value: List<CachedHourlyForecast>): String = gson.toJson(value)

    @TypeConverter
    fun toHourlyForecasts(value: String): List<CachedHourlyForecast> {
        val type = object : TypeToken<List<CachedHourlyForecast>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromDailyForecasts(value: List<CachedDailyForecast>): String = gson.toJson(value)

    @TypeConverter
    fun toDailyForecasts(value: String): List<CachedDailyForecast> {
        val type = object : TypeToken<List<CachedDailyForecast>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromWeatherAlerts(value: List<CachedWeatherAlert>?): String? = value?.let(gson::toJson)

    @TypeConverter
    fun toWeatherAlerts(value: String?): List<CachedWeatherAlert>? {
        if (value == null) return null
        val type = object : TypeToken<List<CachedWeatherAlert>>() {}.type
        return gson.fromJson(value, type)
    }
}
