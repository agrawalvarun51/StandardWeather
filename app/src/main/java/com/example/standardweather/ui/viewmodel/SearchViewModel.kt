package com.example.standardweather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.standardweather.domain.model.CitySearchResult
import com.example.standardweather.domain.repository.WeatherRepository
import com.example.standardweather.ui.state.SearchUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")

    private data class SearchResult(
        val results: List<CitySearchResult> = emptyList(),
        val isSearching: Boolean = false,
        val error: String? = null
    )

    private val searchResult: StateFlow<SearchResult> = _query
        .debounce(400)
        .distinctUntilChanged()
        .flatMapLatest { q ->
            if (q.isBlank()) {
                flowOf(SearchResult())
            } else {
                flowOf(Unit)
                    .map {
                        val result = repository.searchCity(q)
                        result.fold(
                            onSuccess = { SearchResult(results = it, isSearching = false) },
                            onFailure = { SearchResult(error = it.message ?: "Search failed") }
                        )
                    }
                    .onStart { emit(SearchResult(isSearching = true)) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchResult()
        )

    val uiState: StateFlow<SearchUiState> = combine(
        _query,
        searchResult,
        repository.getSearchHistory()
    ) { query, search, history ->
        SearchUiState(
            query = query,
            results = search.results,
            recentSearches = history,
            isSearching = search.isSearching,
            error = search.error
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchUiState()
        )

    fun onQueryChanged(query: String) {
        _query.value = query
    }

    fun onCitySelected(city: CitySearchResult) {
        viewModelScope.launch {
            repository.saveSearchHistory(city)
        }
    }
}
