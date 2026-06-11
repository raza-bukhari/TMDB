package com.example.tmdb.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.tmdb.core.navigation.MovieDetailRoute
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.appErrorOrNull
import com.example.tmdb.domain.usecase.ObserveMovieDetailUseCase
import com.example.tmdb.domain.usecase.RefreshMovieDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class MovieDetailViewModel(
    savedStateHandle: SavedStateHandle,
    observeMovieDetail: ObserveMovieDetailUseCase,
    private val refreshMovieDetail: RefreshMovieDetailUseCase,
) : ViewModel() {

    private val movieId = MovieId(savedStateHandle.toRoute<MovieDetailRoute>().movieId)

    private val isRefreshing = MutableStateFlow(true)
    private val refreshError = MutableStateFlow<AppError?>(null)

    val uiState: StateFlow<MovieDetailUiState> =
        combine(observeMovieDetail(movieId), isRefreshing, refreshError) { detail, refreshing, error ->
            MovieDetailUiState(
                isRefreshing = refreshing,
                content = when {
                    detail != null -> MovieDetailContent.Detail(detail.toUi())
                    refreshing -> MovieDetailContent.Loading
                    error != null -> MovieDetailContent.Error(error)
                    else -> MovieDetailContent.Loading
                },
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            // A refresh is always in flight at construction (see init).
            initialValue = MovieDetailUiState(isRefreshing = true),
        )

    init {
        refresh()
    }

    fun onRetryClicked() = refresh()

    private fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            refreshError.value = null
            refreshError.value = refreshMovieDetail(movieId).appErrorOrNull()
            isRefreshing.value = false
        }
    }
}
