package com.example.tmdb.feature.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.HomeList
import com.example.tmdb.domain.model.asAppError
import com.example.tmdb.domain.usecase.GetHomeListUseCase
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface MoviesEffect {
    data class NavigateToDetail(val movieId: Long) : MoviesEffect
}

internal class MoviesViewModel(
    private val getHomeList: GetHomeListUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoviesUiState())
    val uiState: StateFlow<MoviesUiState> = _uiState

    private val _effects = Channel<MoviesEffect>(Channel.BUFFERED)
    val effects: Flow<MoviesEffect> = _effects.receiveAsFlow()
    private var refreshJob: Job? = null

    init {
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

    fun onMovieClicked(movieId: Long) {
        viewModelScope.launch { _effects.send(MoviesEffect.NavigateToDetail(movieId)) }
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

private fun Throwable.asHomeErrorMessage(): String = when (asAppError()) {
    AppError.Offline -> "You're offline. Connect and try again."
    AppError.InvalidToken -> "TMDB rejected the API token. Check your local.properties."
    AppError.NotFound -> "The TMDB list is unavailable right now."
    AppError.RateLimited -> "Too many requests. Give it a moment and retry."
    is AppError.Unknown -> "Something went wrong. Please try again."
}
