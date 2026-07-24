package com.example.standardweather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.standardweather.domain.model.CitySearchResult
import com.example.standardweather.domain.repository.WeatherRepository
import com.example.standardweather.ui.state.SearchUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")

    init {
        // Observe recent searches from DB
        repository.getSearchHistory()
            .onEach { history ->
                _uiState.update { it.copy(recentSearches = history) }
            }
            .launchIn(viewModelScope)

        // Debounced search
        _query
            .debounce(400)
            .distinctUntilChanged()
            .onEach { q ->
                if (q.isBlank()) {
                    _uiState.update { it.copy(results = emptyList(), isSearching = false) }
                } else {
                    performSearch(q)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
        _query.value = query
    }

    fun clearQuery() {
        _uiState.update { it.copy(query = "", results = emptyList()) }
        _query.value = ""
    }

    fun onCitySelected(city: CitySearchResult) {
        viewModelScope.launch {
            repository.saveSearchHistory(city)
        }
    }

    private suspend fun performSearch(query: String) {
        _uiState.update { it.copy(isSearching = true, error = null) }
        repository.searchCity(query)
            .onSuccess { results ->
                _uiState.update { it.copy(results = results, isSearching = false) }
            }
            .onFailure { err ->
                _uiState.update {
                    it.copy(isSearching = false, error = err.message ?: "Search failed")
                }
            }
    }
}
