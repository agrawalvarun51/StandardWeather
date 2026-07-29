package com.example.standardweather

import app.cash.turbine.test
import com.example.standardweather.domain.model.CitySearchResult
import com.example.standardweather.domain.model.CurrentWeather
import com.example.standardweather.domain.model.WeatherData
import com.example.standardweather.domain.repository.WeatherRepository
import com.example.standardweather.domain.usecase.GetCityForWeatherUseCase
import com.example.standardweather.domain.usecase.ObserveWeatherUseCase
import com.example.standardweather.ui.state.WeatherUiState
import com.example.standardweather.ui.viewmodel.WeatherViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository: WeatherRepository = mockk()
    private lateinit var viewModel: WeatherViewModel

    private val fakeCity = CitySearchResult("10.0_20.0", "TestCity", "TC", null, 10.0, 20.0)

    private val fakeWeatherData = WeatherData(
        cityId = "10.0_20.0",
        cityName = "TestCity",
        country = "TC",
        lat = 10.0,
        lon = 20.0,
        timezone = "UTC",
        current = CurrentWeather(
            dt = 1_700_000_000L,
            temp = 22.0,
            feelsLike = 20.0,
            humidity = 60,
            windSpeed = 5.0,
            uvi = 3.0,
            visibility = 10000,
            weatherId = 800,
            weatherMain = "Clear",
            weatherDescription = "clear sky",
            weatherIcon = "01d"
        ),
        hourly = emptyList(),
        daily = emptyList(),
        alerts = emptyList(),
        fetchedAt = System.currentTimeMillis()
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getCityForWeather(fakeCity.cityId) } returns fakeCity
        viewModel = WeatherViewModel(
            ObserveWeatherUseCase(repository),
            GetCityForWeatherUseCase(repository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadWeather transitions Loading to Success`() = runTest {
        every {
            repository.getWeather(any(), any(), any(), any(), any(), any())
        } returns flowOf(Result.success(fakeWeatherData))

        viewModel.uiState.test {
            // initial value before any city is selected
            assertEquals(WeatherUiState.Loading, awaitItem())
            viewModel.loadWeather(fakeCity.cityId)
            val success = awaitItem()
            assertTrue(success is WeatherUiState.Success)
            assertEquals("TestCity", (success as WeatherUiState.Success).data.cityName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadWeather transitions Loading to Error when flow emits failure`() = runTest {
        every {
            repository.getWeather(any(), any(), any(), any(), any(), any())
        } returns flowOf(Result.failure(RuntimeException("No network")))

        viewModel.uiState.test {
            // initial value before any city is selected
            assertEquals(WeatherUiState.Loading, awaitItem())
            viewModel.loadWeather(fakeCity.cityId)
            val error = awaitItem()
            assertTrue(error is WeatherUiState.Error)
            assertEquals("No network", (error as WeatherUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test
    fun `refresh requests forced weather even when cache emits immediately`() = runTest {
        every {
            repository.getWeather(
                fakeCity.cityId,
                fakeCity.lat,
                fakeCity.lon,
                fakeCity.name,
                fakeCity.country,
                false
            )
        } returns flowOf(Result.success(fakeWeatherData))

        every {
            repository.getWeather(
                fakeCity.cityId,
                fakeCity.lat,
                fakeCity.lon,
                fakeCity.name,
                fakeCity.country,
                true
            )
        } returns flowOf(Result.success(fakeWeatherData))

        viewModel.uiState.test {
            assertEquals(WeatherUiState.Loading, awaitItem())
            viewModel.loadWeather(fakeCity.cityId)
            assertTrue(awaitItem() is WeatherUiState.Success)

            viewModel.refresh()
            advanceUntilIdle()

            verify {
                repository.getWeather(
                    fakeCity.cityId,
                    fakeCity.lat,
                    fakeCity.lon,
                    fakeCity.name,
                    fakeCity.country,
                    true
                )
            }
            cancelAndIgnoreRemainingEvents()
        }
    }
}
