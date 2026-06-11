package com.example.tmdb.feature.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.MovieCategory
import com.example.tmdb.domain.model.MoviePage
import com.example.tmdb.domain.model.asAppError
import com.example.tmdb.domain.usecase.LoadMoreMoviesUseCase
import com.example.tmdb.domain.usecase.ObserveMoviesUseCase
import com.example.tmdb.domain.usecase.RefreshMoviesUseCase
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface MoviesEffect {
    data class NavigateToDetail(val movieId: Long) : MoviesEffect
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class MoviesViewModel(
    private val observeMovies: ObserveMoviesUseCase,
    private val refreshMovies: RefreshMoviesUseCase,
    private val loadMoreMovies: LoadMoreMoviesUseCase,
) : ViewModel() {

    private val selectedCategory = MutableStateFlow(MovieCategory.POPULAR)
    private val status = MutableStateFlow(Status())

    // Per-category paging + refresh bookkeeping; only touched on the Main dispatcher (viewModelScope).
    private val pageByCategory = mutableMapOf<MovieCategory, MoviePage>()
    private val refreshedCategories = mutableSetOf<MovieCategory>()

    private val _effects = Channel<MoviesEffect>(Channel.BUFFERED)
    val effects: Flow<MoviesEffect> = _effects.receiveAsFlow()

    val uiState: StateFlow<MoviesUiState> =
        combine(
            // Pair category with its movies in one source so the displayed category and the
            // shown list never disagree mid-switch; flatMapLatest cancels the old tab's DB query.
            selectedCategory.flatMapLatest { category -> observeMovies(category).map { category to it } },
            status,
        ) { (category, movies), st ->
            MoviesUiState(
                selectedCategory = category,
                isRefreshing = st.isRefreshing,
                content = when {
                    movies.isNotEmpty() -> MoviesContent.Movies(
                        movies = movies.map { it.toListItem() }.toImmutableList(),
                        isAppending = st.isAppending,
                        canLoadMore = st.canLoadMore,
                        // Cache wins, but flag a failed refresh so the UI can warn non-blockingly.
                        staleError = st.error,
                    )
                    st.isRefreshing -> MoviesContent.Loading
                    st.error != null -> MoviesContent.Error(st.error)
                    else -> MoviesContent.Empty
                },
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MoviesUiState(isRefreshing = true),
        )

    init {
        refresh(MovieCategory.POPULAR)
    }

    fun onCategorySelected(category: MovieCategory) {
        if (selectedCategory.value == category) return
        selectedCategory.value = category
        // Reflect the newly-selected category's known paging; drop the other tab's transient error/append.
        status.value = Status(
            isRefreshing = category !in refreshedCategories,
            canLoadMore = pageByCategory[category]?.canLoadMore == true,
        )
        if (category !in refreshedCategories) refresh(category)
    }

    fun onRetryClicked() = refresh(selectedCategory.value)

    /** Pull-to-refresh on the current tab. */
    fun onRefresh() = refresh(selectedCategory.value)

    fun onLoadMoreRequested() {
        val category = selectedCategory.value
        val page = pageByCategory[category] ?: return
        if (!page.canLoadMore || status.value.isAppending || status.value.isRefreshing) return
        status.update { it.copy(isAppending = true) }
        viewModelScope.launch {
            loadMoreMovies(category, page.page + 1).fold(
                onSuccess = { next ->
                    pageByCategory[category] = next
                    if (selectedCategory.value == category) {
                        status.update { it.copy(isAppending = false, canLoadMore = next.canLoadMore) }
                    }
                },
                // Keep the existing list; the user can scroll again to retry the append.
                onFailure = {
                    if (selectedCategory.value == category) status.update { it.copy(isAppending = false) }
                },
            )
        }
    }

    fun onMovieClicked(movieId: Long) {
        viewModelScope.launch { _effects.send(MoviesEffect.NavigateToDetail(movieId)) }
    }

    private fun refresh(category: MovieCategory) {
        status.update { it.copy(isRefreshing = true, error = null) }
        viewModelScope.launch {
            refreshMovies(category).fold(
                onSuccess = { page ->
                    refreshedCategories += category
                    pageByCategory[category] = page
                    if (selectedCategory.value == category) {
                        status.update { it.copy(isRefreshing = false, canLoadMore = page.canLoadMore) }
                    }
                },
                onFailure = { t ->
                    if (selectedCategory.value == category) {
                        status.update { it.copy(isRefreshing = false, error = t.asAppError()) }
                    }
                },
            )
        }
    }

    private data class Status(
        val isRefreshing: Boolean = false,
        val isAppending: Boolean = false,
        val error: AppError? = null,
        val canLoadMore: Boolean = false,
    )
}
