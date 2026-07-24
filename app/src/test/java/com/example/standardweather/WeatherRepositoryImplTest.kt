package com.example.standardweather

import com.example.standardweather.data.local.entity.WeatherCacheEntity
import com.example.standardweather.data.local.dao.WeatherCacheDao
import com.example.standardweather.data.local.dao.SearchHistoryDao
import com.example.standardweather.data.remote.WeatherApiService
import com.example.standardweather.data.remote.model.*
import com.example.standardweather.data.repository.WeatherRepositoryImpl
import com.example.standardweather.domain.model.CitySearchResult
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import app.cash.turbine.test

class WeatherRepositoryImplTest {

    private val api: WeatherApiService = mockk()
    private val weatherCacheDao: WeatherCacheDao = mockk()
    private val searchHistoryDao: SearchHistoryDao = mockk()
    private lateinit var repository: WeatherRepositoryImpl

    private val fakeCity = CitySearchResult("27.18_78.02", "Agra", "India", "Uttar Pradesh", 27.18, 78.02)

    private val fakeCacheEntity = WeatherCacheEntity(
        cityId = "27.18_78.02", cityName = "Agra", country = "India",
        lat = 27.18, lon = 78.02, timezone = "Asia/Kolkata",
        currentDt = 1_784_856_600L, currentTemp = 28.9, currentFeelsLike = 32.9,
        currentHumidity = 72, currentWindSpeed = 4.5, currentUvi = 1.3,
        currentVisibility = 10000, currentWeatherId = 1003,
        currentWeatherMain = "Partly Cloudy",
        currentWeatherDescription = "partly cloudy",
        currentWeatherIcon = "https://cdn.weatherapi.com/weather/64x64/day/116.png",
        hourlyJson = "[]", dailyJson = "[]", alertsJson = null,
        fetchedAt = System.currentTimeMillis()
    )

    private val fakeCondition = WaConditionDto("Partly Cloudy", "//cdn.weatherapi.com/weather/64x64/day/116.png", 1003)

    private val fakeForecastResponse = WeatherApiForecastResponse(
        location = WaLocationDto("Agra", "Uttar Pradesh", "India", 27.18, 78.02, "Asia/Kolkata", 1_784_857_782L, "2026-07-24 07:19"),
        current  = WaCurrentDto(1_784_856_600L, 31.0, 35.0, 68, 18.0, 10.0, 2.0, 1, fakeCondition, 10, 0),
        forecast = WaForecastDto(forecastday = listOf(
            WaForecastDayDto(
                dateEpoch = 1_784_851_200L,
                day  = WaDayDto(35.0, 25.0, 30.0, 20.0, 70, 10.0, 3.0, 20, 0, fakeCondition),
                hour = emptyList()
            )
        )),
        alerts = null
    )

    @Before
    fun setUp() {
        repository = WeatherRepositoryImpl(api, weatherCacheDao, searchHistoryDao)
    }

    @Test
    fun `getWeather emits cached data without calling api when cache is fresh`() = runTest {
        coEvery { weatherCacheDao.getWeather(fakeCity.cityId) } returns fakeCacheEntity
        every { weatherCacheDao.observeWeather(fakeCity.cityId) } returns MutableStateFlow(fakeCacheEntity)

        repository.getWeather(fakeCity.cityId, fakeCity.lat, fakeCity.lon, fakeCity.name, fakeCity.country)
            .test {
                val result = awaitItem()
                assertTrue(result.isSuccess)
                assertEquals("Agra", result.getOrNull()?.cityName)
                cancelAndIgnoreRemainingEvents()
            }

        coVerify(exactly = 0) { api.getForecast(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `getWeather calls api when cache is stale`() = runTest {
        val stale = fakeCacheEntity.copy(fetchedAt = System.currentTimeMillis() - 40 * 60 * 1000L)
        coEvery { weatherCacheDao.getWeather(fakeCity.cityId) } returns stale
        coEvery { weatherCacheDao.upsert(any()) } just Runs
        every { weatherCacheDao.observeWeather(fakeCity.cityId) } returns MutableStateFlow(stale)
        coEvery { api.getForecast(any(), any(), any(), any(), any()) } returns fakeForecastResponse

        repository.getWeather(fakeCity.cityId, fakeCity.lat, fakeCity.lon, fakeCity.name, fakeCity.country)
            .test {
                val cached = awaitItem(); assertTrue(cached.isSuccess)
                val fresh  = awaitItem(); assertTrue(fresh.isSuccess)
                assertEquals(31.0, fresh.getOrNull()?.current?.temp)
                cancelAndIgnoreRemainingEvents()
            }

        coVerify(exactly = 1) { api.getForecast(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `getWeather emits failure when no cache and api throws`() = runTest {
        coEvery { weatherCacheDao.getWeather(fakeCity.cityId) } returns null
        coEvery { api.getForecast(any(), any(), any(), any(), any()) } throws RuntimeException("timeout")
        every { weatherCacheDao.observeWeather(fakeCity.cityId) } returns MutableStateFlow(null)

        repository.getWeather(fakeCity.cityId, fakeCity.lat, fakeCity.lon, fakeCity.name, fakeCity.country)
            .test {
                assertTrue(awaitItem().isFailure)
                cancelAndIgnoreRemainingEvents()
            }
    }

    @Test
    fun `searchCity returns mapped results on success`() = runTest {
        coEvery { api.searchCity(any(), any()) } returns listOf(
            WaSearchResultDto(1, "Agra", "Uttar Pradesh", "India", 27.18, 78.02)
        )
        val result = repository.searchCity("Agra")
        assertTrue(result.isSuccess)
        assertEquals("Agra", result.getOrNull()?.first()?.name)
    }

    @Test
    fun `searchCity returns failure on network exception`() = runTest {
        coEvery { api.searchCity(any(), any()) } throws RuntimeException("timeout")
        assertTrue(repository.searchCity("xyz").isFailure)
    }

    @Test
    fun `refreshWeather calls api and caches result`() = runTest {
        coEvery { weatherCacheDao.upsert(any()) } just Runs
        coEvery { api.getForecast(any(), any(), any(), any(), any()) } returns fakeForecastResponse

        val result = repository.refreshWeather(fakeCity.cityId, fakeCity.lat, fakeCity.lon, fakeCity.name, fakeCity.country)

        assertTrue(result.isSuccess)
        assertEquals(31.0, result.getOrNull()?.current?.temp)
        coVerify(exactly = 1) { weatherCacheDao.upsert(any()) }
        coVerify(exactly = 1) { api.getForecast(any(), any(), any(), any(), any()) }
    }
}
