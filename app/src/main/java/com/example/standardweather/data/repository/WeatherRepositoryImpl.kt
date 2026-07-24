package com.example.standardweather.data.repository

import com.example.standardweather.BuildConfig
import com.example.standardweather.data.local.dao.SearchHistoryDao
import com.example.standardweather.data.local.dao.WeatherCacheDao
import com.example.standardweather.data.local.entity.SearchHistoryEntity
import com.example.standardweather.data.mapper.toCitySearchResult
import com.example.standardweather.data.mapper.toDomain
import com.example.standardweather.data.mapper.toEntity
import com.example.standardweather.data.remote.WeatherApiService
import com.example.standardweather.domain.model.CitySearchResult
import com.example.standardweather.domain.model.WeatherData
import com.example.standardweather.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Cache TTL: 30 minutes */
private const val CACHE_TTL_MS = 30 * 60 * 1000L

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApiService,
    private val weatherCacheDao: WeatherCacheDao,
    private val searchHistoryDao: SearchHistoryDao
) : WeatherRepository {

    override fun getWeather(
        cityId: String,
        lat: Double,
        lon: Double,
        cityName: String,
        country: String,
        forceRefresh: Boolean
    ): Flow<Result<WeatherData>> = flow {
        // 1. Emit cached data immediately (offline-first)
        val cached = weatherCacheDao.getWeather(cityId)
        if (cached != null) {
            emit(Result.success(cached.toDomain()))
        }

        // 2. Refresh if stale or forced
        val fetchedAt = cached?.fetchedAt ?: 0L
        val stale = (System.currentTimeMillis() - fetchedAt) > CACHE_TTL_MS
        if (forceRefresh || stale) {
            runCatching { fetchAndCache(lat, lon) }
                .onSuccess { fresh -> emit(Result.success(fresh)) }
                .onFailure { err ->
                    if (cached == null) emit(Result.failure(err))
                }
        }

        // Observe DB so WorkManager-triggered refreshes propagate to the UI
        emitAll(
            weatherCacheDao.observeWeather(cityId)
                .filterNotNull()
                .map { entity -> Result.success(entity.toDomain()) }
        )
    }

    private suspend fun fetchAndCache(lat: Double, lon: Double): WeatherData {
        val response = api.getForecast(
            apiKey = BuildConfig.WEATHER_API_KEY,
            query  = "$lat,$lon"
        )
        val entity = response.toEntity()
        weatherCacheDao.upsert(entity)
        return entity.toDomain()
    }

    override suspend fun searchCity(query: String): Result<List<CitySearchResult>> =
        runCatching {
            api.searchCity(
                apiKey = BuildConfig.WEATHER_API_KEY,
                query  = query
            ).map { it.toCitySearchResult() }
        }

    override fun getSearchHistory(): Flow<List<CitySearchResult>> =
        searchHistoryDao.observeRecent().map { list -> list.map { it.toCitySearchResult() } }

    override suspend fun saveSearchHistory(city: CitySearchResult) {
        searchHistoryDao.upsert(
            SearchHistoryEntity(
                cityId   = city.cityId,
                cityName = city.name,
                country  = city.country,
                state    = city.state,
                lat      = city.lat,
                lon      = city.lon
            )
        )
    }

    override suspend fun refreshWeather(
        cityId: String,
        lat: Double,
        lon: Double,
        cityName: String,
        country: String
    ): Result<WeatherData> = runCatching { fetchAndCache(lat, lon) }
}
