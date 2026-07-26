package com.example.standardweather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.standardweather.domain.model.CitySearchResult
import com.example.standardweather.domain.repository.WeatherRepository
import com.example.standardweather.ui.state.WeatherUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {
    private val selectedCity = MutableStateFlow<CitySearchResult?>(null)

    private val refreshRequests = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val uiState: StateFlow<WeatherUiState> = selectedCity
        .flatMapLatest { city ->
            if (city == null) {
                flowOf(WeatherUiState.Loading)
            } else {
                merge(
                    flowOf(false),
                    refreshRequests.map { true }
                ).flatMapLatest { refresh ->
                    repository.getWeather(
                        cityId = city.cityId,
                        lat = city.lat,
                        lon = city.lon,
                        cityName = city.name,
                        country = city.country,
                        forceRefresh = refresh
                    )
                        .map { result ->
                            result.fold(
                                onSuccess = { data -> WeatherUiState.Success(data, isRefreshing = false) },
                                onFailure = { err ->
                                    WeatherUiState.Error(
                                        message = err.message ?: "Unknown error"
                                    )
                                }
                            )
                        }
                        .catch { err ->
                            emit(WeatherUiState.Error(message = err.message ?: "Unknown error"))
                        }
                        .let { weatherFlow ->
                            if (refresh) {
                                weatherFlow.onStart {
                                    val currentState = uiState.value
                                    if (currentState is WeatherUiState.Success) {
                                        emit(currentState.copy(isRefreshing = true))
                                    }
                                }
                            } else {
                                weatherFlow
                            }
                        }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WeatherUiState.Loading
        )

    fun loadWeather(city: CitySearchResult) {
        selectedCity.value = city
    }

    fun refresh() {
        if (selectedCity.value != null) {
            refreshRequests.tryEmit(Unit)
        }
    }
}
