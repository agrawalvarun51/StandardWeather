package com.example.standardweather.ui.viewmodel

internal sealed interface WeatherRequest {
    data object Initial : WeatherRequest
    data object Refresh : WeatherRequest
}
