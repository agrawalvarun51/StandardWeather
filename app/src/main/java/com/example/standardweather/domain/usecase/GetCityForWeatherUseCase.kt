package com.example.standardweather.domain.usecase

import com.example.standardweather.domain.model.CitySearchResult
import com.example.standardweather.domain.repository.WeatherRepository
import javax.inject.Inject

class GetCityForWeatherUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(cityId: String): CitySearchResult? = repository.getCityForWeather(cityId)
}
