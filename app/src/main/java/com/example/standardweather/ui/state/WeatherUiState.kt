package com.example.standardweather.ui.state

import com.example.standardweather.domain.model.CitySearchResult
import com.example.standardweather.domain.model.WeatherData

sealed class WeatherUiState {
    data object Loading : WeatherUiState()
    data class Success(val data: WeatherData, val isRefreshing: Boolean = false) : WeatherUiState()
    data class Error(val message: String, val cachedData: WeatherData? = null) : WeatherUiState()
}

data class SearchUiState(
    val query: String = "",
    val results: List<CitySearchResult> = emptyList(),
    val recentSearches: List<CitySearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val error: String? = null
)
