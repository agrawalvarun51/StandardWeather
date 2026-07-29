package com.example.standardweather.domain.usecase

import com.example.standardweather.domain.model.CitySearchResult
import com.example.standardweather.domain.model.WeatherData
import com.example.standardweather.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveWeatherUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    operator fun invoke(
        city: CitySearchResult,
        forceRefresh: Boolean
    ): Flow<Result<WeatherData>> = repository.getWeather(
        cityId = city.cityId,
        lat = city.lat,
        lon = city.lon,
        cityName = city.name,
        country = city.country,
        forceRefresh = forceRefresh
    )
}
