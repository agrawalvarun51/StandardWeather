package com.example.standardweather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.standardweather.domain.model.CitySearchResult
import com.example.standardweather.domain.repository.WeatherRepository
import com.example.standardweather.ui.state.WeatherUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var currentCity: CitySearchResult? = null

    fun loadWeather(city: CitySearchResult, forceRefresh: Boolean = false) {
        currentCity = city
        _uiState.value = WeatherUiState.Loading

        repository.getWeather(
            cityId = city.cityId,
            lat = city.lat,
            lon = city.lon,
            cityName = city.name,
            country = city.country,
            forceRefresh = forceRefresh
        )
            .onEach { result ->
                result
                    .onSuccess { data ->
                        _uiState.update { current ->
                            val isRefreshing = current is WeatherUiState.Success
                            WeatherUiState.Success(data, isRefreshing = isRefreshing)
                        }
                    }
                    .onFailure { err ->
                        val cached = (_uiState.value as? WeatherUiState.Success)?.data
                        _uiState.value = WeatherUiState.Error(
                            message = err.message ?: "Unknown error",
                            cachedData = cached
                        )
                    }
            }
            .catch { err ->
                val cached = (_uiState.value as? WeatherUiState.Success)?.data
                _uiState.value = WeatherUiState.Error(
                    message = err.message ?: "Unknown error",
                    cachedData = cached
                )
            }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        currentCity?.let { loadWeather(it, forceRefresh = true) }
    }
}
