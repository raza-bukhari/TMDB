package com.example.tmdb.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.tmdb.core.navigation.MovieDetailRoute
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.ExternalRatings
import com.example.tmdb.domain.model.MediaKey
import com.example.tmdb.domain.model.MediaType
import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.UserMediaActivity
import com.example.tmdb.domain.model.WatchProviderRegion
import com.example.tmdb.domain.model.WatchlistStatus
import com.example.tmdb.domain.model.appErrorOrNull
import com.example.tmdb.domain.usecase.AddMovieToWatchlistUseCase
import com.example.tmdb.domain.usecase.GetExternalRatingsUseCase
import com.example.tmdb.domain.usecase.GetMediaVideosUseCase
import com.example.tmdb.domain.usecase.GetTvEpisodeUseCase
import com.example.tmdb.domain.usecase.GetTvSeasonUseCase
import com.example.tmdb.domain.usecase.GetWatchProvidersUseCase
import com.example.tmdb.domain.usecase.ObserveMovieDetailUseCase
import com.example.tmdb.domain.usecase.ObserveWatchlistKeysUseCase
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
    private val getTvEpisode: GetTvEpisodeUseCase,
    private val getTvSeason: GetTvSeasonUseCase,
    private val getWatchProviders: GetWatchProvidersUseCase,
    observeWatchlistItems: ObserveWatchlistItemsUseCase,
    private val observeWatchlistKeys: ObserveWatchlistKeysUseCase,
    private val addMovieToWatchlist: AddMovieToWatchlistUseCase,
    private val removeMovieFromWatchlist: RemoveMovieFromWatchlistUseCase,
    private val updateUserActivity: UpdateUserActivityUseCase,
) : ViewModel() {

    private val movieId = MovieId(savedStateHandle.toRoute<MovieDetailRoute>().movieId)
    private val mediaType = runCatching {
        MediaType.valueOf(savedStateHandle.toRoute<MovieDetailRoute>().mediaType)
    }.getOrDefault(MediaType.MOVIE)
    private val detailFlow = observeMovieDetail(movieId, mediaType)
    private val watchlistKeys = observeWatchlistKeys()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptySet(),
        )
    private val userActivity = observeWatchlistItems()
        .map { items -> items.firstOrNull { it.movie.mediaKey == MediaKey(movieId, mediaType) }?.toActivityUi() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    private val isRefreshing = MutableStateFlow(true)
    private val refreshError = MutableStateFlow<AppError?>(null)
    private val externalRatings = MutableStateFlow(ExternalRatings())
    private val videos = MutableStateFlow<List<VideoUi>>(emptyList())
    private val watchProviderRegion = MutableStateFlow<WatchProviderRegion?>(null)
    private val selectedSeasonNumber = MutableStateFlow<Int?>(null)
    private val seasonEpisodes = MutableStateFlow<List<TvEpisodeUi>>(emptyList())
    private val selectedEpisode = MutableStateFlow<TvEpisodeUi?>(null)
    private val isEpisodeLoading = MutableStateFlow(false)
    private var latestDetail: MovieDetail? = null
    private var loadedSeasonNumber: Int? = null

    private val detailState =
        combine(detailFlow, isRefreshing, refreshError, externalRatings, watchlistKeys) { detail, refreshing, error, ratings, savedKeys ->
            DetailState(
                detail = detail,
                refreshing = refreshing,
                error = error,
                ratings = ratings,
                savedKeys = savedKeys,
            )
        }

    private val episodeSheetState =
        combine(selectedEpisode, isEpisodeLoading) { episode, loading ->
            EpisodeSheetState(selectedEpisode = episode, isEpisodeLoading = loading)
        }

    private val renderInputs =
        combine(
            detailState,
            videos,
            seasonEpisodes,
            userActivity,
            watchProviderRegion,
        ) { detailState, videos, episodes, activity, providers ->
            RenderInputs(
                detailState = detailState,
                videos = videos,
                episodes = episodes,
                activity = activity,
                providers = providers,
            )
        }

    val uiState: StateFlow<MovieDetailUiState> =
        combine(renderInputs, selectedSeasonNumber, episodeSheetState) { inputs, seasonNumber, episodeSheet ->
            val detail = inputs.detailState.detail
            latestDetail = detail
            MovieDetailUiState(
                isRefreshing = inputs.detailState.refreshing,
                selectedEpisode = episodeSheet.selectedEpisode,
                isEpisodeLoading = episodeSheet.isEpisodeLoading,
                content = when {
                    detail != null -> MovieDetailContent.Detail(
                        detail.toUi(
                            externalRatings = inputs.detailState.ratings,
                            isWatchlisted = detail.mediaKey in inputs.detailState.savedKeys,
                            watchProvidersOverride = inputs.providers?.displayProviders,
                        ).copy(
                            videos = inputs.videos.toImmutableList(),
                            selectedSeasonNumber = seasonNumber ?: detail.seasons.firstOrNull()?.seasonNumber,
                            episodes = inputs.episodes.toImmutableList(),
                            userActivity = inputs.activity,
                        ),
                    )
                    inputs.detailState.refreshing -> MovieDetailContent.Loading
                    inputs.detailState.error != null -> MovieDetailContent.Error(inputs.detailState.error)
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
        observeInitialSeason()
        loadVideos()
        loadWatchProviders()
    }

    fun onRetryClicked() = refresh()

    fun onWatchlistToggle() {
        viewModelScope.launch {
            val detail = latestDetail ?: return@launch
            if (detail.mediaKey in watchlistKeys.value) {
                removeMovieFromWatchlist(detail.id, detail.mediaType)
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

    fun onSeasonSelected(seasonNumber: Int) {
        if (mediaType != MediaType.TV) return
        selectedSeasonNumber.value = seasonNumber
        loadSeasonEpisodes(seasonNumber)
    }

    fun onEpisodeSelected(episode: TvEpisodeUi) {
        if (mediaType != MediaType.TV) return
        selectedEpisode.value = episode
        viewModelScope.launch {
            isEpisodeLoading.value = true
            selectedEpisode.value = getTvEpisode(movieId, episode.seasonNumber, episode.episodeNumber)
                .getOrNull()
                ?.toUi()
                ?: episode
            isEpisodeLoading.value = false
        }
    }

    fun onEpisodeDismissed() {
        selectedEpisode.value = null
        isEpisodeLoading.value = false
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
            videos.value = getMediaVideos(movieId, mediaType)
                .getOrDefault(emptyList())
                .toVideoUiList()
        }
    }

    private fun loadWatchProviders() {
        viewModelScope.launch {
            watchProviderRegion.value = getWatchProviders(movieId, mediaType, region = "US").getOrNull()
        }
    }

    private fun observeInitialSeason() {
        viewModelScope.launch {
            detailFlow
                .filterNotNull()
                .map { detail ->
                    val selected = selectedSeasonNumber.value
                    if (selected != null && detail.seasons.any { it.seasonNumber == selected }) {
                        selected
                    } else {
                        detail.seasons.firstOrNull()?.seasonNumber
                    }
                }
                .distinctUntilChanged()
                .collect { seasonNumber ->
                    if (mediaType != MediaType.TV || seasonNumber == null) return@collect
                    selectedSeasonNumber.value = seasonNumber
                    loadSeasonEpisodes(seasonNumber)
                }
        }
    }

    private fun loadSeasonEpisodes(seasonNumber: Int) {
        if (loadedSeasonNumber == seasonNumber) return
        loadedSeasonNumber = seasonNumber
        viewModelScope.launch {
            seasonEpisodes.value = emptyList()
            seasonEpisodes.value = getTvSeason(movieId, seasonNumber)
                .getOrNull()
                ?.episodes
                ?.map { it.toUi() }
                .orEmpty()
        }
    }

    private fun updateActivity(transform: (UserActivityUi) -> UserActivityUi) {
        viewModelScope.launch {
            val detail = latestDetail ?: return@launch
            if (detail.mediaKey !in watchlistKeys.value) {
                addMovieToWatchlist(detail)
            }
            val next = transform(userActivity.value ?: UserActivityUi())
            updateUserActivity(
                UserMediaActivity(
                    mediaId = detail.id,
                    mediaType = detail.mediaType,
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
        val savedKeys: Set<MediaKey>,
    )

    private data class RenderInputs(
        val detailState: DetailState,
        val videos: List<VideoUi>,
        val episodes: List<TvEpisodeUi>,
        val activity: UserActivityUi?,
        val providers: WatchProviderRegion?,
    )

    private data class EpisodeSheetState(
        val selectedEpisode: TvEpisodeUi?,
        val isEpisodeLoading: Boolean,
    )
}

private val MovieDetail.mediaKey: MediaKey
    get() = MediaKey(id, mediaType)
