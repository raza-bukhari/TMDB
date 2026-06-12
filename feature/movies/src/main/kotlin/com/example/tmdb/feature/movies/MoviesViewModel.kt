package com.example.tmdb.feature.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.HomeList
import com.example.tmdb.domain.model.MediaType
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.asAppError
import com.example.tmdb.domain.usecase.AddMovieToWatchlistUseCase
import com.example.tmdb.domain.usecase.DiscoverMoviesUseCase
import com.example.tmdb.domain.usecase.GetHomeListUseCase
import com.example.tmdb.domain.usecase.ObserveWatchlistKeysUseCase
import com.example.tmdb.domain.usecase.ObserveWatchlistItemsUseCase
import com.example.tmdb.domain.usecase.ObserveWatchlistUseCase
import com.example.tmdb.domain.usecase.RemoveMovieFromWatchlistUseCase
import com.example.tmdb.domain.usecase.SearchMoviesUseCase
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val SEARCH_DEBOUNCE = 300.milliseconds

sealed interface MoviesEffect {
    data class NavigateToDetail(val movieId: Long, val mediaType: MediaType) : MoviesEffect
}

internal class MoviesViewModel(
    private val getHomeList: GetHomeListUseCase,
    private val searchMovies: SearchMoviesUseCase,
    private val discoverMovies: DiscoverMoviesUseCase,
    private val observeWatchlist: ObserveWatchlistUseCase,
    private val observeWatchlistItems: ObserveWatchlistItemsUseCase,
    private val observeWatchlistKeys: ObserveWatchlistKeysUseCase,
    private val addMovieToWatchlist: AddMovieToWatchlistUseCase,
    private val removeMovieFromWatchlist: RemoveMovieFromWatchlistUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoviesUiState())
    val uiState: StateFlow<MoviesUiState> = _uiState

    private val _effects = Channel<MoviesEffect>(Channel.BUFFERED)
    val effects: Flow<MoviesEffect> = _effects.receiveAsFlow()
    private var refreshJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val searchResults: Flow<PagingData<MovieListItem>> = _uiState
        .map { it.searchQuery.trim() }
        .debounce(SEARCH_DEBOUNCE)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(PagingData.empty())
            } else {
                searchMovies(query).map { pagingData ->
                    pagingData.map { movie -> movie.toListItem() }
                }
            }
        }
        .cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val discoverResults: Flow<PagingData<MovieListItem>> = _uiState
        .map { DiscoverRequest(query = it.discoverQuery.trim(), filters = it.discoverFilters) }
        .debounce(SEARCH_DEBOUNCE)
        .distinctUntilChanged()
        .flatMapLatest { request ->
            val movies = if (request.query.isBlank()) {
                discoverMovies(request.filters.toDiscoverMovieFilters())
            } else {
                searchMovies(request.query)
            }
            movies.map { pagingData -> pagingData.map { movie -> movie.toListItem() } }
        }
        .cachedIn(viewModelScope)

    init {
        observeWatchlistState()
        refreshHome()
    }

    fun onTrendingWindowSelected(window: TrendingWindow) {
        if (window == _uiState.value.trendingWindow) return
        refreshHome(window)
    }

    fun onRefresh() {
        refreshHome()
    }

    fun onRetryClicked() {
        refreshHome()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onDiscoverQueryChanged(query: String) {
        _uiState.update { it.copy(discoverQuery = query) }
    }

    fun onDiscoverFiltersChanged(filters: MovieFilters) {
        _uiState.update { it.copy(discoverFilters = filters) }
    }

    fun onDiscoverFiltersReset() {
        _uiState.update { it.copy(discoverFilters = MovieFilters()) }
    }

    fun onTabSelected(tab: MoviesTab) {
        _uiState.update { it.copy(selectedTab = tab, searchQuery = "") }
    }

    fun onWatchlistFilterSelected(filter: WatchlistFilter) {
        _uiState.update { it.copy(selectedWatchlistFilter = filter) }
    }

    fun onWatchlistToggle(movie: MovieListItem) {
        viewModelScope.launch {
            val id = MovieId(movie.id)
            if (movie.mediaKey in _uiState.value.watchlistKeys) {
                removeMovieFromWatchlist(id, movie.mediaType)
            } else {
                addMovieToWatchlist(movie.toDomainMovie())
            }
        }
    }

    fun onMovieClicked(movieId: Long, mediaType: MediaType) {
        viewModelScope.launch { _effects.send(MoviesEffect.NavigateToDetail(movieId, mediaType)) }
    }

    private fun observeWatchlistState() {
        viewModelScope.launch {
            observeWatchlist().collect { movies ->
                _uiState.update { it.copy(watchlistMovies = movies.toMovieListItems()) }
            }
        }
        viewModelScope.launch {
            observeWatchlistItems().collect { items ->
                _uiState.update { it.copy(watchlistItems = items.toWatchlistItemUi()) }
            }
        }
        viewModelScope.launch {
            observeWatchlistKeys().collect { keys ->
                _uiState.update { it.copy(watchlistKeys = keys) }
            }
        }
    }

    private fun refreshHome(trendingWindow: TrendingWindow = _uiState.value.trendingWindow) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            val hasContent = _uiState.value.sections.isNotEmpty()
            _uiState.update {
                it.copy(
                    trendingWindow = trendingWindow,
                    isLoading = !hasContent,
                    isRefreshing = hasContent,
                    errorMessage = null,
                )
            }

            val result = loadSections(trendingWindow)
            result.fold(
                onSuccess = { sections ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            hero = sections.firstOrNull()?.movies?.firstOrNull(),
                            sections = sections.toImmutableList(),
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = throwable.asHomeErrorMessage(),
                        )
                    }
                },
            )
        }
    }

    private suspend fun loadSections(trendingWindow: TrendingWindow): Result<List<HomeSectionUi>> =
        coroutineScope {
            val lists = listOf(
                trendingWindow.toHomeList(),
                HomeList.POPULAR,
                HomeList.NOW_PLAYING,
                HomeList.TOP_RATED,
                HomeList.UPCOMING,
            )
            val deferred = lists.map { list -> list to async { getHomeList(list) } }
            val sections = mutableListOf<HomeSectionUi>()
            deferred.forEach { (list, request) ->
                val movies = request.await().getOrElse { return@coroutineScope Result.failure(it) }
                if (movies.isNotEmpty()) {
                    sections += HomeSectionUi(
                        list = list,
                        title = list.title(trendingWindow),
                        subtitle = list.subtitle(trendingWindow),
                        movies = movies.toMovieListItems(),
                    )
                }
            }
            Result.success(sections)
        }
}

private data class DiscoverRequest(
    val query: String,
    val filters: MovieFilters,
)

private fun Throwable.asHomeErrorMessage(): String = when (asAppError()) {
    AppError.Offline -> "You're offline. Connect and try again."
    AppError.InvalidToken -> "TMDB rejected the API token. Check your local.properties."
    AppError.NotFound -> "The TMDB list is unavailable right now."
    AppError.RateLimited -> "Too many requests. Give it a moment and retry."
    is AppError.Unknown -> "Something went wrong. Please try again."
}
