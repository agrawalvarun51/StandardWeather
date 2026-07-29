package com.example.standardweather.domain.repository

import com.example.standardweather.domain.model.CitySearchResult
import com.example.standardweather.domain.model.WeatherData
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

    fun getWeather(
        cityId: String,
        lat: Double,
        lon: Double,
        cityName: String,
        country: String,
        forceRefresh: Boolean = false
    ): Flow<Result<WeatherData>>

    suspend fun searchCity(query: String): Result<List<CitySearchResult>>

    fun getSearchHistory(): Flow<List<CitySearchResult>>

    suspend fun getCityForWeather(cityId: String): CitySearchResult?

    suspend fun saveSearchHistory(city: CitySearchResult)

    suspend fun refreshWeather(cityId: String, lat: Double, lon: Double, cityName: String, country: String): Result<WeatherData>
}
