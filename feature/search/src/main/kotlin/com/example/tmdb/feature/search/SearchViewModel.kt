package com.example.tmdb.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.AppException
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.usecase.SearchMoviesUseCase
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val DEBOUNCE = 300.milliseconds

internal class SearchViewModel(
    private val searchMovies: SearchMoviesUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val content = MutableStateFlow<SearchContent>(SearchContent.Idle)

    // Page-append bookkeeping; only touched from viewModelScope (Main) — no races.
    private var accumulated = listOf<Movie>()
    private var currentPage = 0

    val uiState: StateFlow<SearchUiState> =
        combine(query, content) { q, c -> SearchUiState(query = q, content = c) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SearchUiState(),
            )

    init {
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            query
                .map { it.trim() }
                .debounce(DEBOUNCE) // wait out typing bursts
                .distinctUntilChanged()
                .collectLatest { trimmed -> // latest-wins: a new query cancels the in-flight search
                    if (trimmed.isBlank()) {
                        accumulated = emptyList()
                        currentPage = 0
                        content.value = SearchContent.Idle
                    } else {
                        content.value = SearchContent.Loading
                        loadFirstPage(trimmed)
                    }
                }
        }
    }

    fun onQueryChanged(newQuery: String) {
        query.value = newQuery
    }

    fun onRetryClicked() {
        val trimmed = query.value.trim()
        if (trimmed.isBlank()) return
        content.value = SearchContent.Loading
        viewModelScope.launch { loadFirstPage(trimmed) }
    }

    fun onLoadMoreRequested() {
        val current = content.value as? SearchContent.Results ?: return
        if (!current.canLoadMore || current.isAppending) return
        content.value = current.copy(isAppending = true)
        viewModelScope.launch {
            searchMovies(query.value.trim(), page = currentPage + 1).fold(
                onSuccess = { page ->
                    accumulated = accumulated + page.movies
                    currentPage = page.page
                    content.value = SearchContent.Results(
                        movies = accumulated.map { it.toSearchItem() }.toImmutableList(),
                        isAppending = false,
                        canLoadMore = page.canLoadMore,
                    )
                },
                // Keep what we have; the user can scroll again to retry the append.
                onFailure = { content.value = current.copy(isAppending = false) },
            )
        }
    }

    private suspend fun loadFirstPage(trimmed: String) {
        searchMovies(trimmed, page = 1).fold(
            onSuccess = { page ->
                accumulated = page.movies
                currentPage = page.page
                content.value = when {
                    page.movies.isEmpty() -> SearchContent.NoResults
                    else -> SearchContent.Results(
                        movies = accumulated.map { it.toSearchItem() }.toImmutableList(),
                        isAppending = false,
                        canLoadMore = page.canLoadMore,
                    )
                }
            },
            onFailure = { t ->
                content.value = SearchContent.Error((t as? AppException)?.error ?: AppError.Unknown(t))
            },
        )
    }
}
