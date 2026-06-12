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
import com.example.tmdb.domain.model.UserMediaActivity
import com.example.tmdb.domain.model.WatchlistStatus
import com.example.tmdb.domain.model.appErrorOrNull
import com.example.tmdb.domain.usecase.AddMovieToWatchlistUseCase
import com.example.tmdb.domain.usecase.GetExternalRatingsUseCase
import com.example.tmdb.domain.usecase.GetMediaVideosUseCase
import com.example.tmdb.domain.usecase.GetTvSeasonUseCase
import com.example.tmdb.domain.usecase.ObserveMovieDetailUseCase
import com.example.tmdb.domain.usecase.ObserveWatchlistIdsUseCase
import com.example.tmdb.domain.usecase.ObserveWatchlistItemsUseCase
import com.example.tmdb.domain.usecase.RefreshMovieDetailUseCase
import com.example.tmdb.domain.usecase.RemoveMovieFromWatchlistUseCase
import com.example.tmdb.domain.usecase.UpdateUserActivityUseCase
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.collections.immutable.toImmutableList

internal class MovieDetailViewModel(
    savedStateHandle: SavedStateHandle,
    observeMovieDetail: ObserveMovieDetailUseCase,
    private val refreshMovieDetail: RefreshMovieDetailUseCase,
    private val getExternalRatings: GetExternalRatingsUseCase,
    private val getMediaVideos: GetMediaVideosUseCase,
    private val getTvSeason: GetTvSeasonUseCase,
    observeWatchlistItems: ObserveWatchlistItemsUseCase,
    private val observeWatchlistIds: ObserveWatchlistIdsUseCase,
    private val addMovieToWatchlist: AddMovieToWatchlistUseCase,
    private val removeMovieFromWatchlist: RemoveMovieFromWatchlistUseCase,
    private val updateUserActivity: UpdateUserActivityUseCase,
) : ViewModel() {

    private val movieId = MovieId(savedStateHandle.toRoute<MovieDetailRoute>().movieId)
    private val mediaType = runCatching {
        MediaType.valueOf(savedStateHandle.toRoute<MovieDetailRoute>().mediaType)
    }.getOrDefault(MediaType.MOVIE)
    private val detailFlow = observeMovieDetail(movieId, mediaType)
    private val watchlistIds = observeWatchlistIds()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptySet(),
        )
    private val userActivity = observeWatchlistItems()
        .map { items -> items.firstOrNull { it.movie.id == movieId }?.toActivityUi() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    private val isRefreshing = MutableStateFlow(true)
    private val refreshError = MutableStateFlow<AppError?>(null)
    private val externalRatings = MutableStateFlow(ExternalRatings())
    private val trailerUrl = MutableStateFlow<String?>(null)
    private val seasonEpisodes = MutableStateFlow<List<TvEpisodeUi>>(emptyList())
    private var latestDetail: MovieDetail? = null
    private var loadedSeasonNumber: Int? = null

    private val detailState =
        combine(detailFlow, isRefreshing, refreshError, externalRatings, watchlistIds) { detail, refreshing, error, ratings, savedIds ->
            DetailState(
                detail = detail,
                refreshing = refreshing,
                error = error,
                ratings = ratings,
                savedIds = savedIds,
            )
        }

    val uiState: StateFlow<MovieDetailUiState> =
        combine(detailState, trailerUrl, seasonEpisodes, userActivity) { detailState, trailer, episodes, activity ->
            val detail = detailState.detail
            latestDetail = detail
            MovieDetailUiState(
                isRefreshing = detailState.refreshing,
                content = when {
                    detail != null -> MovieDetailContent.Detail(
                        detail.toUi(
                            externalRatings = detailState.ratings,
                            isWatchlisted = detail.id in detailState.savedIds,
                        ).copy(
                            trailerUrl = trailer,
                            episodes = episodes.toImmutableList(),
                            userActivity = activity,
                        ),
                    )
                    detailState.refreshing -> MovieDetailContent.Loading
                    detailState.error != null -> MovieDetailContent.Error(detailState.error)
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
        observeSeasonEpisodes()
        loadVideos()
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

    fun onActivityStatusSelected(status: WatchlistStatus) {
        updateActivity { it.copy(status = status) }
    }

    fun onFavoriteToggle() {
        updateActivity { it.copy(favorite = !it.favorite) }
    }

    fun onUserRatingSelected(rating: Double?) {
        updateActivity { it.copy(userRating = rating) }
    }

    fun onNotesChanged(notes: String) {
        updateActivity { it.copy(notes = notes) }
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

    private fun loadVideos() {
        viewModelScope.launch {
            trailerUrl.value = getMediaVideos(movieId, mediaType)
                .getOrDefault(emptyList())
                .primaryTrailerUrl()
        }
    }

    private fun observeSeasonEpisodes() {
        viewModelScope.launch {
            detailFlow
                .filterNotNull()
                .map { detail -> detail.seasons.firstOrNull()?.seasonNumber }
                .distinctUntilChanged()
                .collect { seasonNumber ->
                    if (mediaType != MediaType.TV || seasonNumber == null || loadedSeasonNumber == seasonNumber) return@collect
                    loadedSeasonNumber = seasonNumber
                    seasonEpisodes.value = getTvSeason(movieId, seasonNumber)
                        .getOrNull()
                        ?.episodes
                        ?.map { it.toUi() }
                        .orEmpty()
                }
        }
    }

    private fun updateActivity(transform: (UserActivityUi) -> UserActivityUi) {
        viewModelScope.launch {
            val detail = latestDetail ?: return@launch
            if (detail.id !in watchlistIds.value) {
                addMovieToWatchlist(detail)
            }
            val next = transform(userActivity.value ?: UserActivityUi())
            updateUserActivity(
                UserMediaActivity(
                    mediaId = detail.id,
                    status = next.status,
                    favorite = next.favorite,
                    userRating = next.userRating,
                    notes = next.notes,
                ),
            )
        }
    }

    private data class DetailState(
        val detail: MovieDetail?,
        val refreshing: Boolean,
        val error: AppError?,
        val ratings: ExternalRatings,
        val savedIds: Set<MovieId>,
    )
}
