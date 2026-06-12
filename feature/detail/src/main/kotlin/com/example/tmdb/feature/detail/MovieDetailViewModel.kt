package com.example.tmdb.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.tmdb.core.navigation.MovieDetailRoute
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.ExternalRatings
import com.example.tmdb.domain.model.MediaType
import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.appErrorOrNull
import com.example.tmdb.domain.usecase.AddMovieToWatchlistUseCase
import com.example.tmdb.domain.usecase.GetExternalRatingsUseCase
import com.example.tmdb.domain.usecase.ObserveMovieDetailUseCase
import com.example.tmdb.domain.usecase.ObserveWatchlistIdsUseCase
import com.example.tmdb.domain.usecase.RefreshMovieDetailUseCase
import com.example.tmdb.domain.usecase.RemoveMovieFromWatchlistUseCase
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
    private val getExternalRatings: GetExternalRatingsUseCase,
    private val observeWatchlistIds: ObserveWatchlistIdsUseCase,
    private val addMovieToWatchlist: AddMovieToWatchlistUseCase,
    private val removeMovieFromWatchlist: RemoveMovieFromWatchlistUseCase,
) : ViewModel() {

    private val movieId = MovieId(savedStateHandle.toRoute<MovieDetailRoute>().movieId)
    private val mediaType = runCatching {
        MediaType.valueOf(savedStateHandle.toRoute<MovieDetailRoute>().mediaType)
    }.getOrDefault(MediaType.MOVIE)
    private val detailFlow = observeMovieDetail(movieId)
    private val watchlistIds = observeWatchlistIds()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptySet(),
        )

    private val isRefreshing = MutableStateFlow(true)
    private val refreshError = MutableStateFlow<AppError?>(null)
    private val externalRatings = MutableStateFlow(ExternalRatings())
    private var latestDetail: MovieDetail? = null

    val uiState: StateFlow<MovieDetailUiState> =
        combine(detailFlow, isRefreshing, refreshError, externalRatings, watchlistIds) { detail, refreshing, error, ratings, savedIds ->
            latestDetail = detail
            MovieDetailUiState(
                isRefreshing = refreshing,
                content = when {
                    detail != null -> MovieDetailContent.Detail(
                        detail.toUi(
                            externalRatings = ratings,
                            isWatchlisted = detail.id in savedIds,
                        ),
                    )
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
        observeExternalRatings()
    }

    fun onRetryClicked() = refresh()

    fun onWatchlistToggle() {
        viewModelScope.launch {
            val detail = latestDetail ?: return@launch
            if (detail.id in watchlistIds.value) {
                removeMovieFromWatchlist(detail.id)
            } else {
                addMovieToWatchlist(detail)
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            refreshError.value = null
            refreshError.value = refreshMovieDetail(movieId, mediaType).appErrorOrNull()
            isRefreshing.value = false
        }
    }

    private fun observeExternalRatings() {
        viewModelScope.launch {
            detailFlow
                .map { detail -> detail?.imdbId }
                .distinctUntilChanged()
                .collect { imdbId ->
                    externalRatings.value = if (imdbId.isNullOrBlank()) {
                        ExternalRatings()
                    } else {
                        getExternalRatings(imdbId).getOrDefault(ExternalRatings())
                    }
                }
        }
    }
}
