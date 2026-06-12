package com.example.tmdb.feature.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.example.tmdb.core.designsystem.ThemePreviews
import com.example.tmdb.core.designsystem.component.ErrorState
import com.example.tmdb.core.designsystem.component.FilterChipPill
import com.example.tmdb.core.designsystem.component.PosterCard
import com.example.tmdb.core.designsystem.component.RatingBadge
import com.example.tmdb.core.designsystem.theme.TMDBTheme
import com.example.tmdb.core.designsystem.theme.ThemeMode
import com.example.tmdb.domain.model.MovieId
import kotlinx.collections.immutable.persistentListOf
import org.koin.androidx.compose.koinViewModel

object MoviesTestTags {
    const val HOME = "movies_home"
    const val SEARCH = "movies_search_button"
    const val SEARCH_FIELD = "movies_inline_search_field"
    const val SEARCH_RESULTS = "movies_inline_search_results"
    const val SEARCH_EMPTY = "movies_inline_search_empty"
    const val THEME = "movies_theme_button"
    const val BOTTOM_NAV = "movies_bottom_nav"
    const val DISCOVER = "movies_discover"
    const val DISCOVER_FILTERS = "movies_discover_filters"
    const val DISCOVER_SEARCH_FIELD = "movies_discover_search_field"
    const val WATCHLIST = "movies_watchlist"
    const val PROFILE = "movies_profile"
    const val WATCHLIST_TOGGLE = "movies_watchlist_toggle"
    const val TRENDING_TODAY = "movies_trending_today"
    const val TRENDING_WEEK = "movies_trending_week"
    const val REFRESHING = "movies_refreshing"
    fun movieCard(id: Long) = "movie_card_$id"
}

@Composable
fun MoviesScreen(
    onMovieClick: (Long, com.example.tmdb.domain.model.MediaType) -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: MoviesViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()
    val discoverResults = viewModel.discoverResults.collectAsLazyPagingItems()

    LaunchedEffect(viewModel, onMovieClick) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MoviesEffect.NavigateToDetail -> onMovieClick(effect.movieId, effect.mediaType)
            }
        }
    }

    MoviesScreenContent(
        state = state,
        themeMode = themeMode,
        onToggleTheme = onToggleTheme,
        searchResults = searchResults,
        discoverResults = discoverResults,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onDiscoverQueryChanged = viewModel::onDiscoverQueryChanged,
        onDiscoverFiltersChanged = viewModel::onDiscoverFiltersChanged,
        onDiscoverFiltersReset = viewModel::onDiscoverFiltersReset,
        onTabSelected = viewModel::onTabSelected,
        onWatchlistFilterSelected = viewModel::onWatchlistFilterSelected,
        onWatchlistToggle = viewModel::onWatchlistToggle,
        onTrendingWindowSelected = viewModel::onTrendingWindowSelected,
        onRetryClick = viewModel::onRetryClicked,
        onRefresh = viewModel::onRefresh,
        onMovieClick = viewModel::onMovieClicked,
        modifier = modifier,
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun MoviesScreenContent(
    state: MoviesUiState,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    searchResults: LazyPagingItems<MovieListItem>?,
    discoverResults: LazyPagingItems<MovieListItem>?,
    onSearchQueryChanged: (String) -> Unit,
    onDiscoverQueryChanged: (String) -> Unit,
    onDiscoverFiltersChanged: (MovieFilters) -> Unit,
    onDiscoverFiltersReset: () -> Unit,
    onTabSelected: (MoviesTab) -> Unit,
    onWatchlistFilterSelected: (WatchlistFilter) -> Unit,
    onWatchlistToggle: (MovieListItem) -> Unit,
    onTrendingWindowSelected: (TrendingWindow) -> Unit,
    onRetryClick: () -> Unit,
    onRefresh: () -> Unit,
    onMovieClick: (Long, com.example.tmdb.domain.model.MediaType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> LoadingHome()
                state.errorMessage != null && state.sections.isEmpty() -> {
                    ErrorState(message = state.errorMessage, onRetryClick = onRetryClick)
                }
                else -> {
                    PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        HomeFeed(
                            state = state,
                            themeMode = themeMode,
                            onToggleTheme = onToggleTheme,
                            searchResults = searchResults,
                            discoverResults = discoverResults,
                            onSearchQueryChanged = onSearchQueryChanged,
                            onDiscoverQueryChanged = onDiscoverQueryChanged,
                            onDiscoverFiltersChanged = onDiscoverFiltersChanged,
                            onDiscoverFiltersReset = onDiscoverFiltersReset,
                            onTabSelected = onTabSelected,
                            onWatchlistFilterSelected = onWatchlistFilterSelected,
                            onWatchlistToggle = onWatchlistToggle,
                            onTrendingWindowSelected = onTrendingWindowSelected,
                            onMovieClick = onMovieClick,
                        )
                    }
                }
            }
            TmdbBottomNav(
                selectedTab = state.selectedTab,
                onTabSelected = onTabSelected,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun LoadingHome() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator()
    }
}

@Composable
private fun HomeFeed(
    state: MoviesUiState,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    searchResults: LazyPagingItems<MovieListItem>?,
    discoverResults: LazyPagingItems<MovieListItem>?,
    onSearchQueryChanged: (String) -> Unit,
    onDiscoverQueryChanged: (String) -> Unit,
    onDiscoverFiltersChanged: (MovieFilters) -> Unit,
    onDiscoverFiltersReset: () -> Unit,
    onTabSelected: (MoviesTab) -> Unit,
    onWatchlistFilterSelected: (WatchlistFilter) -> Unit,
    onWatchlistToggle: (MovieListItem) -> Unit,
    onTrendingWindowSelected: (TrendingWindow) -> Unit,
    onMovieClick: (Long, com.example.tmdb.domain.model.MediaType) -> Unit,
) {
    val isSearching = state.searchQuery.isNotBlank()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(MoviesTestTags.HOME),
        verticalArrangement = Arrangement.spacedBy(if (isSearching) 8.dp else 24.dp),
    ) {
        if (isSearching) {
            item {
                CompactSearchHeader(
                    query = state.searchQuery,
                    onQueryChanged = onSearchQueryChanged,
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme,
                )
            }
            searchResults?.let {
                searchResultsContent(
                    searchResults = it,
                    watchlistIds = state.watchlistIds,
                    onMovieClick = onMovieClick,
                    onWatchlistToggle = onWatchlistToggle,
                )
            }
        } else {
            when (state.selectedTab) {
                MoviesTab.HOME -> {
                    item {
                        Hero(
                            movie = state.hero,
                            themeMode = themeMode,
                            onToggleTheme = onToggleTheme,
                            searchQuery = state.searchQuery,
                            onSearchQueryChanged = onSearchQueryChanged,
                            onMovieClick = onMovieClick,
                        )
                    }
                    state.errorMessage?.let { message ->
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(
                                    text = "$message Showing the latest loaded movies.",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp),
                                )
                            }
                        }
                    }
                    items(
                        items = state.sections,
                        key = { it.list.name },
                    ) { section ->
                        MovieSection(
                            section = section,
                            trendingWindow = state.trendingWindow,
                            watchlistIds = state.watchlistIds,
                            onTrendingWindowSelected = onTrendingWindowSelected,
                            onMovieClick = onMovieClick,
                            onWatchlistToggle = onWatchlistToggle,
                        )
                    }
                    personalizedSection(
                        title = "Continue watching",
                        subtitle = "Titles currently marked as watching",
                        items = state.watchlistItems.filter {
                            it.status == com.example.tmdb.domain.model.WatchlistStatus.WATCHING
                        }.map { it.movie },
                        watchlistIds = state.watchlistIds,
                        onMovieClick = onMovieClick,
                        onWatchlistToggle = onWatchlistToggle,
                    )
                    personalizedSection(
                        title = "Because you saved",
                        subtitle = "Your local queue, ready to resume",
                        items = state.watchlistItems.take(10).map { it.movie },
                        watchlistIds = state.watchlistIds,
                        onMovieClick = onMovieClick,
                        onWatchlistToggle = onWatchlistToggle,
                    )
                    personalizedSection(
                        title = "More like your favorites",
                        subtitle = "Favorites from your local profile",
                        items = state.watchlistItems.filter { it.favorite }.map { it.movie },
                        watchlistIds = state.watchlistIds,
                        onMovieClick = onMovieClick,
                        onWatchlistToggle = onWatchlistToggle,
                    )
                }
                MoviesTab.DISCOVER -> discoverContent(
                    discoverResults = discoverResults,
                    query = state.discoverQuery,
                    filters = state.discoverFilters,
                    watchlistIds = state.watchlistIds,
                    onQueryChanged = onDiscoverQueryChanged,
                    onFiltersChanged = onDiscoverFiltersChanged,
                    onFiltersReset = onDiscoverFiltersReset,
                    onMovieClick = onMovieClick,
                    onWatchlistToggle = onWatchlistToggle,
                )
                MoviesTab.WATCHLIST -> watchlistContent(
                    items = state.watchlistItems,
                    selectedFilter = state.selectedWatchlistFilter,
                    watchlistIds = state.watchlistIds,
                    onFilterSelected = onWatchlistFilterSelected,
                    onMovieClick = onMovieClick,
                    onWatchlistToggle = onWatchlistToggle,
                )
                MoviesTab.PROFILE -> profileContent(
                    items = state.watchlistItems,
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme,
                )
            }
        }

        item {
            Spacer(Modifier.height(104.dp))
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun CompactSearchHeader(
    query: String,
    onQueryChanged: (String) -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        InlineSearchBar(
            query = query,
            onQueryChanged = onQueryChanged,
            actionText = themeMode.label,
            onActionClick = onToggleTheme,
            actionTestTag = MoviesTestTags.THEME,
        )
    }
}

@Composable
private fun Hero(
    movie: MovieListItem?,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onMovieClick: (Long, com.example.tmdb.domain.model.MediaType) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to MaterialTheme.colorScheme.primary,
                        1f to MaterialTheme.colorScheme.secondary,
                    ),
                ),
        )
        AsyncImage(
            model = movie?.backdropUrl ?: movie?.posterUrl,
            contentDescription = movie?.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.18f),
                            0.52f to Color.Black.copy(alpha = 0.56f),
                            1f to Color.Black.copy(alpha = 0.92f),
                        ),
                    )
                },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            InlineSearchBar(
                query = searchQuery,
                onQueryChanged = onSearchQueryChanged,
                actionText = themeMode.label,
                onActionClick = onToggleTheme,
                actionTestTag = MoviesTestTags.THEME,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "Welcome.",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Find what to watch from trending, popular, theatrical, top rated, and upcoming movies.",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            movie?.let {
                Spacer(Modifier.height(20.dp))
                FeaturedMovie(movie = it, onMovieClick = onMovieClick)
            }
        }
    }
}

@Composable
private fun InlineSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    actionText: String,
    onActionClick: () -> Unit,
    actionTestTag: String,
    searchFieldTestTag: String = MoviesTestTags.SEARCH_FIELD,
    placeholder: String = "Search movies",
    clearContentDescription: String = "Clear search",
) {
    Surface(
        color = Color.Black.copy(alpha = 0.28f),
        contentColor = Color.White,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
        shape = RoundedCornerShape(28.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChanged,
                placeholder = { Text(placeholder) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { onQueryChanged("") },
                            modifier = Modifier.testTag(MoviesTestTags.SEARCH),
                        ) {
                            Icon(Icons.Filled.Clear, contentDescription = clearContentDescription)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White.copy(alpha = 0.12f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.22f),
                    focusedLeadingIconColor = Color.White,
                    unfocusedLeadingIconColor = Color.White.copy(alpha = 0.72f),
                    focusedTrailingIconColor = Color.White,
                    unfocusedTrailingIconColor = Color.White.copy(alpha = 0.72f),
                    focusedPlaceholderColor = Color.White.copy(alpha = 0.68f),
                    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.68f),
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag(searchFieldTestTag),
            )
            TextButton(
                onClick = onActionClick,
                modifier = Modifier.testTag(actionTestTag),
            ) {
                Text(text = actionText, color = Color.White)
            }
        }
    }
}

@Composable
private fun FeaturedMovie(movie: MovieListItem, onMovieClick: (Long, com.example.tmdb.domain.model.MediaType) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMovieClick(movie.id, movie.mediaType) },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = movie.title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            RatingBadge(rating = movie.rating)
            movie.releaseYear?.let { HeroMetaChip(it) }
            HeroMetaChip("${movie.voteCount} votes")
        }
        Text(
            text = movie.overview,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.78f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.searchResultsContent(
    searchResults: LazyPagingItems<MovieListItem>,
    watchlistIds: Set<MovieId>,
    onMovieClick: (Long, com.example.tmdb.domain.model.MediaType) -> Unit,
    onWatchlistToggle: (MovieListItem) -> Unit,
) {
    when {
        searchResults.loadState.refresh is LoadState.Loading && searchResults.itemCount == 0 -> {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        searchResults.loadState.refresh is LoadState.NotLoading && searchResults.itemCount == 0 -> {
            item {
                Text(
                    text = "No movies found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .testTag(MoviesTestTags.SEARCH_EMPTY),
                )
            }
        }
        else -> {
            items(
                count = searchResults.itemCount,
                key = searchResults.itemKey { it.id },
            ) { index ->
                searchResults[index]?.let { movie ->
                    SearchResultRow(
                        movie = movie,
                        isWatchlisted = MovieId(movie.id) in watchlistIds,
                        onMovieClick = onMovieClick,
                        onWatchlistToggle = onWatchlistToggle,
                    )
                }
            }
            if (searchResults.loadState.append is LoadState.Loading) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.discoverContent(
    discoverResults: LazyPagingItems<MovieListItem>?,
    query: String,
    filters: MovieFilters,
    watchlistIds: Set<MovieId>,
    onQueryChanged: (String) -> Unit,
    onFiltersChanged: (MovieFilters) -> Unit,
    onFiltersReset: () -> Unit,
    onMovieClick: (Long, com.example.tmdb.domain.model.MediaType) -> Unit,
    onWatchlistToggle: (MovieListItem) -> Unit,
) {
    item {
        DiscoverHeader(
            query = query,
            filters = filters,
            loadedCount = discoverResults?.itemCount ?: 0,
            onQueryChanged = onQueryChanged,
            onFiltersChanged = onFiltersChanged,
            onFiltersReset = onFiltersReset,
            modifier = Modifier.testTag(MoviesTestTags.DISCOVER),
        )
    }
    if (discoverResults == null) return
    when {
        discoverResults.loadState.refresh is LoadState.Loading && discoverResults.itemCount == 0 -> {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        discoverResults.loadState.refresh is LoadState.NotLoading && discoverResults.itemCount == 0 -> {
            item {
                Text(
                    text = if (query.isBlank()) "No movies match these filters." else "No movies found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                )
            }
        }
        else -> {
            items(
                count = (discoverResults.itemCount + 2) / 3,
                key = { rowIndex ->
                    val first = discoverResults.peek(rowIndex * 3)
                    first?.id ?: rowIndex
                },
            ) { rowIndex ->
                DiscoverMovieGridRow(
                    rowIndex = rowIndex,
                    discoverResults = discoverResults,
                    watchlistIds = watchlistIds,
                    onMovieClick = onMovieClick,
                    onWatchlistToggle = onWatchlistToggle,
                )
            }
            if (discoverResults.loadState.append is LoadState.Loading) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoverHeader(
    query: String,
    filters: MovieFilters,
    loadedCount: Int,
    onQueryChanged: (String) -> Unit,
    onFiltersChanged: (MovieFilters) -> Unit,
    onFiltersReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showFilters by remember { mutableStateOf(false) }
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 4.dp),
    ) {
        Text(text = "Discover", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = if (query.isBlank()) {
                "Browse TMDB's full movie catalog. Loaded $loadedCount so far."
            } else {
                "Searching TMDB's full movie catalog. Loaded $loadedCount results so far."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        InlineSearchBar(
            query = query,
            onQueryChanged = onQueryChanged,
            actionText = if (filters.activeCount == 0) "Filters" else "Filters ${filters.activeCount}",
            onActionClick = { showFilters = true },
            actionTestTag = MoviesTestTags.DISCOVER_FILTERS,
            searchFieldTestTag = MoviesTestTags.DISCOVER_SEARCH_FIELD,
            placeholder = "Search movies",
            clearContentDescription = "Clear Discover search",
        )
        val activeLabels = filters.activeLabels()
        if (activeLabels.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(activeLabels, key = { it }) { label ->
                    FilterChipPill(
                        text = label,
                        selected = true,
                        onClick = { showFilters = true },
                    )
                }
            }
        }
    }

    if (showFilters) {
        FilterSheet(
            filters = filters,
            onFiltersChanged = onFiltersChanged,
            onReset = onFiltersReset,
            onDismiss = { showFilters = false },
        )
    }
}

@Composable
private fun DiscoverMovieGridRow(
    rowIndex: Int,
    discoverResults: LazyPagingItems<MovieListItem>,
    watchlistIds: Set<MovieId>,
    onMovieClick: (Long, com.example.tmdb.domain.model.MediaType) -> Unit,
    onWatchlistToggle: (MovieListItem) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        repeat(3) { columnIndex ->
            val itemIndex = rowIndex * 3 + columnIndex
            val movie = if (itemIndex < discoverResults.itemCount) {
                discoverResults[itemIndex]
            } else {
                null
            }
            if (movie != null) {
                HomeMovieCard(
                    movie = movie,
                    isWatchlisted = MovieId(movie.id) in watchlistIds,
                    onMovieClick = onMovieClick,
                    onWatchlistToggle = onWatchlistToggle,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.watchlistContent(
    items: List<WatchlistItemUi>,
    selectedFilter: WatchlistFilter,
    watchlistIds: Set<MovieId>,
    onFilterSelected: (WatchlistFilter) -> Unit,
    onMovieClick: (Long, com.example.tmdb.domain.model.MediaType) -> Unit,
    onWatchlistToggle: (MovieListItem) -> Unit,
) {
    val filteredItems = items.filteredBy(selectedFilter)
    item {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
                .testTag(MoviesTestTags.WATCHLIST),
        ) {
            Text(text = "Watchlist", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = if (items.isEmpty()) "Save movies and series from Home, Discover, or Details." else "Your saved movies, series, favorites, and activity.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(WatchlistFilter.entries, key = { it.name }) { filter ->
                    FilterChipPill(
                        text = filter.label,
                        selected = filter == selectedFilter,
                        onClick = { onFilterSelected(filter) },
                    )
                }
            }
        }
    }
    if (filteredItems.isEmpty()) {
        item {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Text(
                    text = if (items.isEmpty()) {
                        "Your cinematic queue is empty."
                    } else {
                        "No titles match this watchlist filter."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        return
    }
    items(
        items = filteredItems,
        key = { "${it.movie.mediaType}:${it.movie.id}" },
    ) { item ->
        WatchlistResultRow(
            item = item,
            isWatchlisted = MovieId(item.movie.id) in watchlistIds,
            onMovieClick = onMovieClick,
            onWatchlistToggle = onWatchlistToggle,
        )
    }
}

@Composable
private fun WatchlistResultRow(
    item: WatchlistItemUi,
    isWatchlisted: Boolean,
    onMovieClick: (Long, com.example.tmdb.domain.model.MediaType) -> Unit,
    onWatchlistToggle: (MovieListItem) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(horizontal = 16.dp)) {
        SearchResultRow(
            movie = item.movie,
            isWatchlisted = isWatchlisted,
            onMovieClick = onMovieClick,
            onWatchlistToggle = onWatchlistToggle,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.status.name.lowercase().replace('_', ' ').replaceFirstChar { it.titlecase() },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            item.userRating?.let {
                Text(
                    text = "Your rating $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (item.favorite) {
                Text(
                    text = "Favorite",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.profileContent(
    items: List<WatchlistItemUi>,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
) {
    val savedCount = items.size
    val movieCount = items.count { it.movie.mediaType == com.example.tmdb.domain.model.MediaType.MOVIE }
    val seriesCount = items.count { it.movie.mediaType == com.example.tmdb.domain.model.MediaType.TV }
    val watchingCount = items.count { it.status == com.example.tmdb.domain.model.WatchlistStatus.WATCHING }
    val completedCount = items.count { it.status == com.example.tmdb.domain.model.WatchlistStatus.COMPLETED }
    val favoriteCount = items.count { it.favorite }
    val averageUserRating = items.mapNotNull { it.userRating }.takeIf { it.isNotEmpty() }?.average()
    val recentNotes = items.filter { it.notes.isNotBlank() }.take(3)

    item {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
                .testTag(MoviesTestTags.PROFILE),
        ) {
            Text(text = "Profile", style = MaterialTheme.typography.headlineSmall)
            ProfileMetricRow(
                label = "Saved titles",
                value = savedCount.toString(),
            )
            ProfileMetricRow(label = "Movies / Series", value = "$movieCount / $seriesCount")
            ProfileMetricRow(label = "Watching / Completed", value = "$watchingCount / $completedCount")
            ProfileMetricRow(label = "Favorites", value = favoriteCount.toString())
            ProfileMetricRow(label = "Average user rating", value = averageUserRating?.let { String.format("%.1f", it) } ?: "Not rated")
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp),
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(text = "Theme", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = when (themeMode) {
                                ThemeMode.SYSTEM -> "Following system"
                                ThemeMode.LIGHT -> "Light mode"
                                ThemeMode.DARK -> "Dark mode"
                            },
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    TextButton(onClick = onToggleTheme) {
                        Text("Change")
                    }
                }
            }
            if (recentNotes.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Recent notes", style = MaterialTheme.typography.titleMedium)
                        recentNotes.forEach { item ->
                            Text(
                                text = "${item.movie.title}: ${item.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
            ProfileMetricRow(label = "Data source", value = "TMDB")
            ProfileMetricRow(label = "Navigation", value = "Home, Discover, Watchlist, Profile")
        }
    }
}

@Composable
private fun ProfileMetricRow(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun SearchResultRow(
    movie: MovieListItem,
    isWatchlisted: Boolean,
    onMovieClick: (Long, com.example.tmdb.domain.model.MediaType) -> Unit,
    onWatchlistToggle: (MovieListItem) -> Unit,
    rank: Int? = null,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMovieClick(movie.id, movie.mediaType) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag(MoviesTestTags.SEARCH_RESULTS),
    ) {
        AsyncImage(
            model = movie.posterUrl,
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(72.dp)
                .height(108.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                rank?.let { HeroMetaChip("#$it") }
                RatingBadge(rating = movie.rating)
                movie.releaseYear?.let { HeroMetaChip(it) }
            }
            if (movie.overview.isNotBlank()) {
                Text(
                    text = movie.overview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        WatchlistButton(
            isWatchlisted = isWatchlisted,
            onClick = { onWatchlistToggle(movie) },
        )
    }
}

@Composable
private fun HeroMetaChip(text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.14f),
        contentColor = Color.White,
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.personalizedSection(
    title: String,
    subtitle: String,
    items: List<MovieListItem>,
    watchlistIds: Set<MovieId>,
    onMovieClick: (Long, com.example.tmdb.domain.model.MediaType) -> Unit,
    onWatchlistToggle: (MovieListItem) -> Unit,
) {
    if (items.isEmpty()) return
    item {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(42.dp)
                        .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(4.dp)),
                )
                Spacer(Modifier.width(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = title, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(items, key = { "${it.mediaType}:${it.id}" }) { movie ->
                    HomeMovieCard(
                        movie = movie,
                        isWatchlisted = MovieId(movie.id) in watchlistIds,
                        onMovieClick = onMovieClick,
                        onWatchlistToggle = onWatchlistToggle,
                    )
                }
            }
        }
    }
}

@Composable
private fun MovieSection(
    section: HomeSectionUi,
    trendingWindow: TrendingWindow,
    watchlistIds: Set<MovieId>,
    onTrendingWindowSelected: (TrendingWindow) -> Unit,
    onMovieClick: (Long, com.example.tmdb.domain.model.MediaType) -> Unit,
    onWatchlistToggle: (MovieListItem) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(42.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = section.title, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = section.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (section.list == trendingWindow.toHomeList()) {
                TrendingToggle(
                    selected = trendingWindow,
                    onSelected = onTrendingWindowSelected,
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(section.movies, key = { it.id }) { movie ->
                HomeMovieCard(
                    movie = movie,
                    isWatchlisted = MovieId(movie.id) in watchlistIds,
                    onMovieClick = onMovieClick,
                    onWatchlistToggle = onWatchlistToggle,
                )
            }
        }
    }
}

@Composable
private fun TrendingToggle(
    selected: TrendingWindow,
    onSelected: (TrendingWindow) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(Modifier.padding(2.dp)) {
            TrendingToggleItem(
                text = "Today",
                selected = selected == TrendingWindow.TODAY,
                onClick = { onSelected(TrendingWindow.TODAY) },
                modifier = Modifier.testTag(MoviesTestTags.TRENDING_TODAY),
            )
            TrendingToggleItem(
                text = "Week",
                selected = selected == TrendingWindow.THIS_WEEK,
                onClick = { onSelected(TrendingWindow.THIS_WEEK) },
                modifier = Modifier.testTag(MoviesTestTags.TRENDING_WEEK),
            )
        }
    }
}

@Composable
private fun TrendingToggleItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun HomeMovieCard(
    movie: MovieListItem,
    isWatchlisted: Boolean,
    onMovieClick: (Long, com.example.tmdb.domain.model.MediaType) -> Unit,
    onWatchlistToggle: (MovieListItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    PosterCard(
        title = movie.title,
        rating = movie.rating,
        subtitle = movie.releaseYear ?: "${movie.voteCount} votes",
        onClick = { onMovieClick(movie.id, movie.mediaType) },
        modifier = modifier
            .width(140.dp)
            .testTag(MoviesTestTags.movieCard(movie.id)),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            WatchlistButton(
                isWatchlisted = isWatchlisted,
                onClick = { onWatchlistToggle(movie) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            )
        }
    }
}

@Composable
private fun WatchlistButton(
    isWatchlisted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.52f), RoundedCornerShape(18.dp))
            .testTag(MoviesTestTags.WATCHLIST_TOGGLE),
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = if (isWatchlisted) "Remove from watchlist" else "Add to watchlist",
            tint = if (isWatchlisted) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.82f),
        )
    }
}

@Composable
private fun TmdbBottomNav(
    selectedTab: MoviesTab,
    onTabSelected: (MoviesTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        BottomNavItem(MoviesTab.HOME, "Home", Icons.Filled.Home),
        BottomNavItem(MoviesTab.DISCOVER, "Discover", Icons.Filled.Search),
        BottomNavItem(MoviesTab.WATCHLIST, "Watchlist", Icons.Filled.Star),
        BottomNavItem(MoviesTab.PROFILE, "Profile", Icons.Filled.Person),
    )
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag(MoviesTestTags.BOTTOM_NAV),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 10.dp),
        ) {
            items.forEach { item ->
                BottomNavButton(
                    item = item,
                    selected = item.tab == selectedTab,
                    onClick = { onTabSelected(item.tab) },
                )
            }
        }
    }
}

@Composable
private fun BottomNavButton(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .width(76.dp)
            .clickable(onClick = onClick),
    ) {
        Surface(
            color = if (selected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f) else Color.Transparent,
            contentColor = contentColor,
            shape = RoundedCornerShape(18.dp),
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .size(22.dp),
            )
        }
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private data class BottomNavItem(
    val tab: MoviesTab,
    val label: String,
    val icon: ImageVector,
)

private val ThemeMode.label: String
    get() = when (this) {
        ThemeMode.SYSTEM -> "Auto"
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
    }

@ThemePreviews
@Composable
private fun MoviesHomePreview() {
    TMDBTheme {
        MoviesScreenContent(
            state = MoviesUiState(
                isLoading = false,
                hero = previewMovie,
                sections = persistentListOf(
                    HomeSectionUi(
                        list = TrendingWindow.TODAY.toHomeList(),
                        title = "Trending",
                        subtitle = "Movies people are watching today",
                        movies = persistentListOf(previewMovie, previewMovie.copy(id = 551, title = "The Creator")),
                    ),
                ),
            ),
            themeMode = ThemeMode.SYSTEM,
            onToggleTheme = {},
            searchResults = null,
            discoverResults = null,
            onSearchQueryChanged = {},
            onDiscoverQueryChanged = {},
            onDiscoverFiltersChanged = {},
            onDiscoverFiltersReset = {},
            onTabSelected = {},
            onWatchlistFilterSelected = {},
            onWatchlistToggle = {},
            onTrendingWindowSelected = {},
            onRetryClick = {},
            onRefresh = {},
            onMovieClick = { _, _ -> },
        )
    }
}

private val previewMovie = MovieListItem(
    id = 550,
    title = "Fight Club",
    overview = "An insomniac and a soap maker channel primal aggression into an underground club.",
    posterUrl = null,
    backdropUrl = null,
    rating = 8.4,
    voteCount = 26280,
    releaseYear = "1999",
    genreIds = listOf(18, 53),
)
