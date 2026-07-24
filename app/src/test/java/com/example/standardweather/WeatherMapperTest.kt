package com.example.standardweather

import com.example.standardweather.data.mapper.toCitySearchResult
import com.example.standardweather.data.mapper.toDomain
import com.example.standardweather.data.mapper.toEntity
import com.example.standardweather.data.remote.model.*
import org.junit.Assert.*
import org.junit.Test

class WeatherMapperTest {

    private val fakeCondition = WaConditionDto(text = "Partly Cloudy", icon = "//cdn.weatherapi.com/weather/64x64/day/116.png", code = 1003)

    private val fakeHour = WaHourDto(
        timeEpoch = 1_784_858_400L, tempC = 29.0, feelslikeC = 33.0,
        humidity = 70, windKph = 16.0, visKm = 10.0, uv = 1.5,
        isDay = 1, chanceOfRain = 15, condition = fakeCondition
    )

    private val fakeDay = WaDayDto(
        maxtempC = 34.0, mintempC = 26.0, avgtempC = 30.0,
        maxwindKph = 20.0, avghumidity = 72, avgvisKm = 10.0,
        uv = 3.0, chanceOfRain = 20, chanceOfSnow = 0,
        condition = fakeCondition
    )

    private val fakeForecastDay = WaForecastDayDto(
        dateEpoch = 1_784_851_200L,
        day = fakeDay,
        hour = listOf(fakeHour)
    )

    private val fakeAlert = WaAlertDto(
        headline = "Heat Advisory", event = "Heat Advisory",
        effective = null, expires = null,
        desc = "High temperatures expected.", instruction = null,
        severity = "Moderate", areas = "Agra, UP"
    )

    private val fakeResponse = WeatherApiForecastResponse(
        location = WaLocationDto(
            name = "Agra", region = "Uttar Pradesh", country = "India",
            lat = 27.18, lon = 78.02,
            tzId = "Asia/Kolkata",
            localtimeEpoch = 1_784_857_782L,
            localtime = "2026-07-24 07:19"
        ),
        current = WaCurrentDto(
            lastUpdatedEpoch = 1_784_856_600L,
            tempC = 28.9, feelslikeC = 32.9, humidity = 72,
            windKph = 16.2, visKm = 10.0, uv = 1.3, isDay = 1,
            condition = fakeCondition, chanceOfRain = 11, chanceOfSnow = 0
        ),
        forecast = WaForecastDto(forecastday = listOf(fakeForecastDay)),
        alerts = WaAlertsWrapper(alert = listOf(fakeAlert))
    )

    @Test
    fun `toEntity correctly maps location and current weather fields`() {
        val entity = fakeResponse.toEntity(fetchedAt = 1_784_856_600L)

        assertEquals("27.18_78.02", entity.cityId)
        assertEquals("Agra", entity.cityName)
        assertEquals("India", entity.country)
        assertEquals(28.9, entity.currentTemp, 0.001)
        assertEquals(72, entity.currentHumidity)
        assertEquals(1003, entity.currentWeatherId)
        assertEquals("partly cloudy", entity.currentWeatherDescription)
        assertEquals(1.3, entity.currentUvi, 0.001)
        assertEquals("Asia/Kolkata", entity.timezone)
    }

    @Test
    fun `toEntity converts wind kph to m-s correctly`() {
        val entity = fakeResponse.toEntity()
        assertEquals(16.2 / 3.6, entity.currentWindSpeed, 0.01)
    }

    @Test
    fun `toEntity maps hourly from forecast hours`() {
        val domain = fakeResponse.toEntity().toDomain()
        assertEquals(1, domain.hourly.size)
        assertEquals(29.0, domain.hourly.first().temp, 0.001)
        assertEquals(0.15, domain.hourly.first().pop, 0.01)
    }

    @Test
    fun `toEntity maps daily from forecastday`() {
        val domain = fakeResponse.toEntity().toDomain()
        assertEquals(1, domain.daily.size)
        assertEquals(34.0, domain.daily.first().tempMax, 0.001)
        assertEquals(26.0, domain.daily.first().tempMin, 0.001)
    }

    @Test
    fun `toEntity maps alerts correctly`() {
        val domain = fakeResponse.toEntity().toDomain()
        assertEquals(1, domain.alerts.size)
        assertEquals("Heat Advisory", domain.alerts.first().event)
        assertEquals("Agra, UP", domain.alerts.first().senderName)
    }

    @Test
    fun `entity with no alerts maps to empty list in domain`() {
        val domain = fakeResponse.copy(alerts = null).toEntity().toDomain()
        assertTrue(domain.alerts.isEmpty())
    }

    @Test
    fun `toCitySearchResult maps WaSearchResultDto correctly`() {
        val dto = WaSearchResultDto(id = 1, name = "Agra", region = "Uttar Pradesh", country = "India", lat = 27.18, lon = 78.02)
        val result = dto.toCitySearchResult()
        assertEquals("27.18_78.02", result.cityId)
        assertEquals("Agra", result.name)
        assertEquals("Uttar Pradesh", result.state)
    }
}
