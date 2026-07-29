package com.example.standardweather.data.mapper

import com.example.standardweather.data.local.entity.SearchHistoryEntity
import com.example.standardweather.data.local.entity.WeatherCacheEntity
import com.example.standardweather.data.local.model.CachedDailyForecast
import com.example.standardweather.data.local.model.CachedDailyTemperature
import com.example.standardweather.data.local.model.CachedHourlyForecast
import com.example.standardweather.data.local.model.CachedWeatherAlert
import com.example.standardweather.data.local.model.CachedWeatherCondition
import com.example.standardweather.data.remote.model.WaAlertDto
import com.example.standardweather.data.remote.model.WaSearchResultDto
import com.example.standardweather.data.remote.model.WeatherApiForecastResponse
import com.example.standardweather.domain.model.CitySearchResult
import com.example.standardweather.domain.model.CurrentWeather
import com.example.standardweather.domain.model.DailyWeather
import com.example.standardweather.domain.model.HourlyWeather
import com.example.standardweather.domain.model.WeatherAlert
import com.example.standardweather.domain.model.WeatherData

fun WeatherApiForecastResponse.toEntity(
    fetchedAt: Long = System.currentTimeMillis()
): WeatherCacheEntity {
    val cityId = "${location.lat}_${location.lon}"

    // Map hourly from all forecastdays → flat list, capped at 24
    val hourlyDtos: List<CachedHourlyForecast> = forecast.forecastday
        .flatMap { it.hour }
        .take(24)
        .map { h ->
            CachedHourlyForecast(
                dt = h.timeEpoch,
                temp = h.tempC,
                feelsLike = h.feelslikeC,
                humidity = h.humidity,
                windSpeed = h.windKph / 3.6,          // kph → m/s
                pop = h.chanceOfRain / 100.0,
                weather = listOf(
                    CachedWeatherCondition(
                        id   = h.condition.code,
                        main = h.condition.text,
                        description = h.condition.text.lowercase(),
                        icon = "https:${h.condition.icon}"
                    )
                )
            )
        }

    // Map daily — one entry per forecastday
    val dailyDtos: List<CachedDailyForecast> = forecast.forecastday.map { fd ->
        CachedDailyForecast(
            dt = fd.dateEpoch,
            temp = CachedDailyTemperature(
                day   = fd.day.avgtempC,
                min   = fd.day.mintempC,
                max   = fd.day.maxtempC,
                night = fd.day.mintempC
            ),
            humidity  = fd.day.avghumidity,
            windSpeed = fd.day.maxwindKph / 3.6,
            pop       = fd.day.chanceOfRain / 100.0,
            summary   = null,
            weather   = listOf(
                CachedWeatherCondition(
                    id   = fd.day.condition.code,
                    main = fd.day.condition.text,
                    description = fd.day.condition.text.lowercase(),
                    icon = "https:${fd.day.condition.icon}"
                )
            )
        )
    }

    // Map alerts
    val alertDtos: List<CachedWeatherAlert> = alerts?.alert.orEmpty().map { it.toAlertDto() }

    return WeatherCacheEntity(
        cityId      = cityId,
        cityName    = location.name,
        country     = location.country,
        lat         = location.lat,
        lon         = location.lon,
        timezone    = location.tzId,
        currentDt   = current.lastUpdatedEpoch,
        currentTemp = current.tempC,
        currentFeelsLike  = current.feelslikeC,
        currentHumidity   = current.humidity,
        currentWindSpeed  = current.windKph / 3.6,
        currentUvi        = current.uv,
        currentVisibility = (current.visKm * 1000).toInt(),
        currentWeatherId          = current.condition.code,
        currentWeatherMain        = current.condition.text,
        currentWeatherDescription = current.condition.text.lowercase(),
        currentWeatherIcon        = "https:${current.condition.icon}",
        hourly      = hourlyDtos,
        daily       = dailyDtos,
        alerts      = alertDtos.ifEmpty { null },
        fetchedAt   = fetchedAt
    )
}

private fun WaAlertDto.toAlertDto() = CachedWeatherAlert(
    senderName  = areas ?: "Weather Service",
    event       = event,
    start       = 0L,
    end         = 0L,
    description = desc
)

// Room entity → domain model

fun WeatherCacheEntity.toDomain(): WeatherData {
    return WeatherData(
        cityId   = cityId,
        cityName = cityName,
        country  = country,
        lat      = lat,
        lon      = lon,
        timezone = timezone,
        current  = CurrentWeather(
            dt               = currentDt,
            temp             = currentTemp,
            feelsLike        = currentFeelsLike,
            humidity         = currentHumidity,
            windSpeed        = currentWindSpeed,
            uvi              = currentUvi,
            visibility       = currentVisibility,
            weatherId        = currentWeatherId,
            weatherMain      = currentWeatherMain,
            weatherDescription = currentWeatherDescription,
            weatherIcon      = currentWeatherIcon
        ),
        hourly = hourly.map { h ->
            HourlyWeather(
                dt               = h.dt,
                temp             = h.temp,
                feelsLike        = h.feelsLike,
                humidity         = h.humidity,
                windSpeed        = h.windSpeed,
                pop              = h.pop,
                weatherId        = h.weather.firstOrNull()?.id ?: 800,
                weatherMain      = h.weather.firstOrNull()?.main ?: "Clear",
                weatherDescription = h.weather.firstOrNull()?.description ?: "",
                weatherIcon      = h.weather.firstOrNull()?.icon ?: ""
            )
        },
        daily = daily.map { d ->
            DailyWeather(
                dt          = d.dt,
                tempDay     = d.temp.day,
                tempMin     = d.temp.min,
                tempMax     = d.temp.max,
                tempNight   = d.temp.night,
                humidity    = d.humidity,
                windSpeed   = d.windSpeed,
                pop         = d.pop,
                summary     = d.summary ?: "",
                weatherId   = d.weather.firstOrNull()?.id ?: 800,
                weatherMain = d.weather.firstOrNull()?.main ?: "Clear",
                weatherDescription = d.weather.firstOrNull()?.description ?: "",
                weatherIcon = d.weather.firstOrNull()?.icon ?: ""
            )
        },
        alerts = alerts?.map { a ->
            WeatherAlert(
                senderName  = a.senderName,
                event       = a.event,
                start       = a.start,
                end         = a.end,
                description = a.description
            )
        } ?: emptyList(),
        fetchedAt = fetchedAt
    )
}

// Search result mappings

fun WaSearchResultDto.toCitySearchResult() = CitySearchResult(
    cityId  = "${lat}_${lon}",
    name    = name,
    country = country,
    state   = region.ifBlank { null },
    lat     = lat,
    lon     = lon
)

fun WaSearchResultDto.toSearchHistoryEntity() = SearchHistoryEntity(
    cityId   = "${lat}_${lon}",
    cityName = name,
    country  = country,
    state    = region.ifBlank { null },
    lat      = lat,
    lon      = lon
)

fun SearchHistoryEntity.toCitySearchResult() = CitySearchResult(
    cityId  = cityId,
    name    = cityName,
    country = country,
    state   = state,
    lat     = lat,
    lon     = lon
)
