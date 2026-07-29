package com.example.standardweather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.standardweather.domain.model.CitySearchResult
import com.example.standardweather.domain.usecase.GetCityForWeatherUseCase
import com.example.standardweather.domain.usecase.ObserveWeatherUseCase
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
    private val observeWeatherUseCase: ObserveWeatherUseCase,
    private val getCityForWeatherUseCase: GetCityForWeatherUseCase
) : ViewModel() {
    private val selectedCity = MutableStateFlow<CitySearchResult?>(null)

    private val refreshRequests = MutableSharedFlow<WeatherRequest>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val uiState: StateFlow<WeatherUiState> = selectedCity
        .flatMapLatest { city ->
            if (city == null) {
                flowOf(WeatherUiState.Loading)
            } else {
                merge(
                    flowOf(WeatherRequest.Initial),
                    refreshRequests
                ).flatMapLatest { request ->
                    observeWeatherUseCase(city, forceRefresh = request is WeatherRequest.Refresh)
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
                            if (request is WeatherRequest.Refresh) {
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

    suspend fun loadWeather(cityId: String) {
        selectedCity.value = getCityForWeatherUseCase(cityId)
    }

    fun refresh() {
        if (selectedCity.value != null) {
            refreshRequests.tryEmit(WeatherRequest.Refresh)
        }
    }
}
