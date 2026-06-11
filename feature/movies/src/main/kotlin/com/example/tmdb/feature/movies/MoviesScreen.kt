package com.example.tmdb.feature.movies

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.tmdb.core.designsystem.ThemePreviews
import com.example.tmdb.core.designsystem.component.EmptyState
import com.example.tmdb.core.designsystem.component.ErrorState
import com.example.tmdb.core.designsystem.component.LoadingState
import com.example.tmdb.core.designsystem.component.PosterCard
import com.example.tmdb.core.designsystem.theme.TMDBTheme
import com.example.tmdb.core.designsystem.theme.ThemeMode
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.MovieCategory
import kotlinx.collections.immutable.persistentListOf
import org.koin.androidx.compose.koinViewModel

object MoviesTestTags {
    const val GRID = "movies_grid"
    const val SEARCH = "movies_search_button"
    const val FILTER = "movies_filter_button"
    const val THEME = "movies_theme_button"
    const val APPEND_SPINNER = "movies_append_spinner"
    const val STALE_BANNER = "movies_stale_banner"
    fun movieCard(id: Long) = "movie_card_$id"
    fun tab(category: MovieCategory) = "movies_tab_${category.name}"
}

/** Request the next page this many items before the end of the grid. */
private const val LOAD_MORE_LOOKAHEAD = 6

@Composable
fun MoviesScreen(
    onMovieClick: (Long) -> Unit,
    onSearchClick: () -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: MoviesViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel, onMovieClick) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MoviesEffect.NavigateToDetail -> onMovieClick(effect.movieId)
            }
        }
    }

    MoviesScreenContent(
        state = state,
        themeMode = themeMode,
        onToggleTheme = onToggleTheme,
        onCategorySelected = viewModel::onCategorySelected,
        onRetryClick = viewModel::onRetryClicked,
        onRefresh = viewModel::onRefresh,
        onMovieClick = viewModel::onMovieClicked,
        onLoadMoreRequested = viewModel::onLoadMoreRequested,
        onFiltersChanged = viewModel::onFiltersChanged,
        onFiltersReset = viewModel::onFiltersReset,
        onSearchClick = onSearchClick,
        modifier = modifier,
    )
}

@Composable
internal fun MoviesScreenContent(
    state: MoviesUiState,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onCategorySelected: (MovieCategory) -> Unit,
    onRetryClick: () -> Unit,
    onRefresh: () -> Unit,
    onMovieClick: (Long) -> Unit,
    onLoadMoreRequested: () -> Unit,
    onFiltersChanged: (MovieFilters) -> Unit,
    onFiltersReset: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showFilters by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        TopBar(
            themeMode = themeMode,
            activeFilterCount = state.filters.activeCount,
            onToggleTheme = onToggleTheme,
            onFilterClick = { showFilters = true },
            onSearchClick = onSearchClick,
        )

        ScrollableTabRow(
            selectedTabIndex = state.selectedCategory.ordinal,
            edgePadding = 12.dp,
        ) {
            MovieCategory.entries.forEach { category ->
                Tab(
                    selected = category == state.selectedCategory,
                    onClick = { onCategorySelected(category) },
                    text = { Text(category.label()) },
                    modifier = Modifier.testTag(MoviesTestTags.tab(category)),
                )
            }
        }

        MoviesBody(
            content = state.content,
            isRefreshing = state.isRefreshing,
            onRetryClick = onRetryClick,
            onRefresh = onRefresh,
            onMovieClick = onMovieClick,
            onLoadMoreRequested = onLoadMoreRequested,
        )
    }

    if (showFilters) {
        FilterSheet(
            filters = state.filters,
            onFiltersChanged = onFiltersChanged,
            onReset = onFiltersReset,
            onDismiss = { showFilters = false },
        )
    }
}

@Composable
private fun TopBar(
    themeMode: ThemeMode,
    activeFilterCount: Int,
    onToggleTheme: () -> Unit,
    onFilterClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp),
    ) {
        Text(
            text = "TMDB",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
        )
        TextButton(
            onClick = onToggleTheme,
            modifier = Modifier.testTag(MoviesTestTags.THEME),
        ) {
            Text(
                text = when (themeMode) {
                    ThemeMode.SYSTEM -> "Auto"
                    ThemeMode.LIGHT -> "Light"
                    ThemeMode.DARK -> "Dark"
                },
            )
        }
        // Core icon set has no "tune"/"filter" glyph, so a labelled button reads clearest.
        TextButton(
            onClick = onFilterClick,
            modifier = Modifier.testTag(MoviesTestTags.FILTER),
        ) {
            Text(if (activeFilterCount > 0) "Filter ($activeFilterCount)" else "Filter")
        }
        IconButton(
            onClick = onSearchClick,
            modifier = Modifier.testTag(MoviesTestTags.SEARCH),
        ) {
            Icon(Icons.Filled.Search, contentDescription = "Search movies")
        }
    }
}

@Composable
private fun MoviesBody(
    content: MoviesContent,
    isRefreshing: Boolean,
    onRetryClick: () -> Unit,
    onRefresh: () -> Unit,
    onMovieClick: (Long) -> Unit,
    onLoadMoreRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Crossfade+slight slide between the content states so transitions don't snap.
    AnimatedContent(
        targetState = content,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        contentKey = { it::class },
        label = "movies-content",
        modifier = modifier.fillMaxSize(),
    ) { target ->
        when (target) {
            is MoviesContent.Movies -> RefreshableGrid(
                content = target,
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                onMovieClick = onMovieClick,
                onLoadMoreRequested = onLoadMoreRequested,
            )
            MoviesContent.Loading -> Box(Modifier.fillMaxSize()) { LoadingState() }
            MoviesContent.Empty -> Box(Modifier.fillMaxSize()) {
                EmptyState(message = "No movies right now. Check back later.")
            }
            MoviesContent.NoMatches -> Box(Modifier.fillMaxSize()) {
                EmptyState(message = "No movies match your filters.")
            }
            is MoviesContent.Error -> Box(Modifier.fillMaxSize()) {
                ErrorState(message = target.error.toUserMessage(), onRetryClick = onRetryClick)
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun RefreshableGrid(
    content: MoviesContent.Movies,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onMovieClick: (Long) -> Unit,
    onLoadMoreRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(Modifier.fillMaxSize()) {
            AnimatedVisibility(visible = content.staleError != null) {
                content.staleError?.let { StaleBanner(it) }
            }
            MoviesGrid(
                content = content,
                onMovieClick = onMovieClick,
                onLoadMoreRequested = onLoadMoreRequested,
            )
        }
    }
}

@Composable
private fun MoviesGrid(
    content: MoviesContent.Movies,
    onMovieClick: (Long) -> Unit,
    onLoadMoreRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState()
    val nearEnd by remember(gridState) {
        derivedStateOf {
            val info = gridState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= info.totalItemsCount - LOAD_MORE_LOOKAHEAD
        }
    }
    LaunchedEffect(nearEnd, content.canLoadMore, content.isAppending) {
        if (nearEnd && content.canLoadMore && !content.isAppending) onLoadMoreRequested()
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Adaptive(minSize = 120.dp),
        // navigationBars inset so the last row clears the gesture/nav bar (edge-to-edge).
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag(MoviesTestTags.GRID),
    ) {
        items(
            items = content.movies,
            key = { it.id },
            contentType = { "movie" },
        ) { movie ->
            PosterCard(
                title = movie.title,
                rating = movie.rating,
                subtitle = movie.releaseYear,
                onClick = { onMovieClick(movie.id) },
                // Animate position when sort reorders the grid.
                modifier = Modifier
                    .animateItem()
                    .testTag(MoviesTestTags.movieCard(movie.id)),
            ) {
                AsyncImage(
                    model = movie.posterUrl,
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        if (content.isAppending) {
            item(span = { GridItemSpan(maxLineSpan) }, contentType = "spinner") {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag(MoviesTestTags.APPEND_SPINNER),
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun StaleBanner(error: AppError, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        modifier = modifier
            .fillMaxWidth()
            .testTag(MoviesTestTags.STALE_BANNER),
    ) {
        Text(
            text = "${error.toUserMessage()} Showing saved movies.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

internal fun AppError.toUserMessage(): String = when (this) {
    AppError.Offline -> "You're offline. Connect and try again."
    AppError.InvalidToken -> "TMDB rejected the API token. Check your local.properties."
    AppError.NotFound -> "Couldn't find what you were looking for."
    AppError.RateLimited -> "Too many requests. Give it a moment and retry."
    is AppError.Unknown -> "Something went wrong. Please try again."
}

@ThemePreviews
@Composable
private fun MoviesScreenContentPreview() {
    TMDBTheme(themeMode = ThemeMode.SYSTEM) {
        MoviesScreenContent(
            state = MoviesUiState(
                selectedCategory = MovieCategory.TOP_RATED,
                filters = MovieFilters(sort = MovieSort.RATING_DESC),
                content = MoviesContent.Movies(
                    persistentListOf(
                        MovieListItem(1, "Fight Club", null, 8.4, "1999"),
                        MovieListItem(2, "The Matrix", null, 8.2, "1999"),
                    ),
                    isAppending = true,
                    canLoadMore = true,
                ),
            ),
            themeMode = ThemeMode.SYSTEM,
            onToggleTheme = {},
            onCategorySelected = {},
            onRetryClick = {},
            onRefresh = {},
            onMovieClick = {},
            onLoadMoreRequested = {},
            onFiltersChanged = {},
            onFiltersReset = {},
            onSearchClick = {},
        )
    }
}

@ThemePreviews
@Composable
private fun MoviesScreenErrorPreview() {
    TMDBTheme(themeMode = ThemeMode.DARK) {
        MoviesScreenContent(
            state = MoviesUiState(content = MoviesContent.Error(AppError.Offline)),
            themeMode = ThemeMode.DARK,
            onToggleTheme = {},
            onCategorySelected = {},
            onRetryClick = {},
            onRefresh = {},
            onMovieClick = {},
            onLoadMoreRequested = {},
            onFiltersChanged = {},
            onFiltersReset = {},
            onSearchClick = {},
        )
    }
}
