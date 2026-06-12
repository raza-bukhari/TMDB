package com.example.tmdb.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.tmdb.domain.usecase.SearchMoviesUseCase
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private val DEBOUNCE = 300.milliseconds

internal class SearchViewModel(
    private val searchMovies: SearchMoviesUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")

    val uiState: StateFlow<SearchUiState> = query
        .map { SearchUiState(query = it, content = SearchContent.Idle) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchUiState(),
        )

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val pagingData: Flow<PagingData<SearchResultItem>> = query
        .map { it.trim() }
        .debounce(DEBOUNCE)
        .distinctUntilChanged()
        .flatMapLatest { trimmed ->
            if (trimmed.isBlank()) {
                flowOf(PagingData.empty())
            } else {
                searchMovies(trimmed).map { pagingData ->
                    pagingData.map { it.toSearchItem() }
                }
            }
        }.cachedIn(viewModelScope)

    fun onQueryChanged(newQuery: String) {
        query.value = newQuery
    }
}
