package com.example.standardweather.data.remote

import com.example.standardweather.data.remote.model.WaSearchResultDto
import com.example.standardweather.data.remote.model.WeatherApiForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    // Current weather + 7-day forecast + alerts in a single call
    @GET("forecast.json")
    suspend fun getForecast(
        @Query("key")    apiKey: String,
        @Query("q")      query:  String,
        @Query("days")   days:   Int = 7,
        @Query("alerts") alerts: String = "yes",
        @Query("aqi")    aqi:    String = "no"
    ): WeatherApiForecastResponse

    // City search / autocomplete
    @GET("search.json")
    suspend fun searchCity(
        @Query("key") apiKey: String,
        @Query("q")   query:  String
    ): List<WaSearchResultDto>
}
