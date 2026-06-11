package com.example.tmdb.feature.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.appErrorOrNull
import com.example.tmdb.domain.usecase.ObservePopularMoviesUseCase
import com.example.tmdb.domain.usecase.RefreshPopularMoviesUseCase
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface MoviesEffect {
    data class NavigateToDetail(val movieId: Long) : MoviesEffect
}

internal class MoviesViewModel(
    observePopularMovies: ObservePopularMoviesUseCase,
    private val refreshPopularMovies: RefreshPopularMoviesUseCase,
) : ViewModel() {

    private val isRefreshing = MutableStateFlow(true)
    private val refreshError = MutableStateFlow<AppError?>(null)

    private val _effects = Channel<MoviesEffect>(Channel.BUFFERED)
    val effects: Flow<MoviesEffect> = _effects.receiveAsFlow()

    val uiState: StateFlow<MoviesUiState> =
        combine(observePopularMovies(), isRefreshing, refreshError) { movies, refreshing, error ->
            MoviesUiState(
                isRefreshing = refreshing,
                content = when {
                    movies.isNotEmpty() -> MoviesContent.Movies(
                        movies.map { it.toListItem() }.toImmutableList(),
                    )
                    refreshing -> MoviesContent.Loading
                    error != null -> MoviesContent.Error(error)
                    else -> MoviesContent.Empty
                },
            )
        }.stateIn(
            scope = viewModelScope,
            // Survives rotation without re-subscribing the DB; stops when nobody looks for 5s.
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MoviesUiState(),
        )

    init {
        refresh()
    }

    fun onRetryClicked() = refresh()

    fun onMovieClicked(movieId: Long) {
        viewModelScope.launch { _effects.send(MoviesEffect.NavigateToDetail(movieId)) }
    }

    private fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            refreshError.value = null
            refreshError.value = refreshPopularMovies().appErrorOrNull()
            isRefreshing.value = false
        }
    }
}
