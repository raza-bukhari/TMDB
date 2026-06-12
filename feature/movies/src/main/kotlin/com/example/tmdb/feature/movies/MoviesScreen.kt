package com.example.tmdb.feature.movies

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.tmdb.core.designsystem.ThemePreviews
import com.example.tmdb.core.designsystem.component.ErrorState
import com.example.tmdb.core.designsystem.component.PosterCard
import com.example.tmdb.core.designsystem.component.RatingBadge
import com.example.tmdb.core.designsystem.theme.TMDBTheme
import com.example.tmdb.core.designsystem.theme.ThemeMode
import kotlinx.collections.immutable.persistentListOf
import org.koin.androidx.compose.koinViewModel

object MoviesTestTags {
    const val HOME = "movies_home"
    const val SEARCH = "movies_search_button"
    const val THEME = "movies_theme_button"
    const val TRENDING_TODAY = "movies_trending_today"
    const val TRENDING_WEEK = "movies_trending_week"
    const val REFRESHING = "movies_refreshing"
    fun movieCard(id: Long) = "movie_card_$id"
}

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
        onTrendingWindowSelected = viewModel::onTrendingWindowSelected,
        onRetryClick = viewModel::onRetryClicked,
        onRefresh = viewModel::onRefresh,
        onMovieClick = viewModel::onMovieClicked,
        onSearchClick = onSearchClick,
        modifier = modifier,
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun MoviesScreenContent(
    state: MoviesUiState,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onTrendingWindowSelected: (TrendingWindow) -> Unit,
    onRetryClick: () -> Unit,
    onRefresh: () -> Unit,
    onMovieClick: (Long) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
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
                        onTrendingWindowSelected = onTrendingWindowSelected,
                        onMovieClick = onMovieClick,
                        onSearchClick = onSearchClick,
                    )
                }
            }
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
    onTrendingWindowSelected: (TrendingWindow) -> Unit,
    onMovieClick: (Long) -> Unit,
    onSearchClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(MoviesTestTags.HOME),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            Hero(
                movie = state.hero,
                themeMode = themeMode,
                onToggleTheme = onToggleTheme,
                onSearchClick = onSearchClick,
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
                onTrendingWindowSelected = onTrendingWindowSelected,
                onMovieClick = onMovieClick,
            )
        }

        item {
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun Hero(
    movie: MovieListItem?,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onSearchClick: () -> Unit,
    onMovieClick: (Long) -> Unit,
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
            TopBar(themeMode = themeMode, onToggleTheme = onToggleTheme, onSearchClick = onSearchClick)
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
            Spacer(Modifier.height(20.dp))
            SearchPill(onSearchClick)
            movie?.let {
                Spacer(Modifier.height(20.dp))
                FeaturedMovie(movie = it, onMovieClick = onMovieClick)
            }
        }
    }
}

@Composable
private fun TopBar(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onSearchClick: () -> Unit,
) {
    Surface(
        color = Color.Black.copy(alpha = 0.28f),
        contentColor = Color.White,
        shape = RoundedCornerShape(28.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        ) {
            Text(
                text = "TMDB",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
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
                    color = Color.White,
                )
            }
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.testTag(MoviesTestTags.SEARCH),
            ) {
                Icon(Icons.Filled.Search, contentDescription = "Search movies", tint = Color.White)
            }
        }
    }
}

@Composable
private fun SearchPill(onSearchClick: () -> Unit) {
    Surface(
        color = Color.White.copy(alpha = 0.96f),
        contentColor = Color.Black,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSearchClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 18.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        ) {
            Text(
                text = "Search for a movie",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black.copy(alpha = 0.62f),
                modifier = Modifier.weight(1f),
            )
            Button(onClick = onSearchClick, shape = RoundedCornerShape(24.dp)) {
                Text("Search")
            }
        }
    }
}

@Composable
private fun FeaturedMovie(movie: MovieListItem, onMovieClick: (Long) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMovieClick(movie.id) },
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

@Composable
private fun MovieSection(
    section: HomeSectionUi,
    trendingWindow: TrendingWindow,
    onTrendingWindowSelected: (TrendingWindow) -> Unit,
    onMovieClick: (Long) -> Unit,
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
                HomeMovieCard(movie = movie, onMovieClick = onMovieClick)
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
private fun HomeMovieCard(movie: MovieListItem, onMovieClick: (Long) -> Unit) {
    PosterCard(
        title = movie.title,
        rating = movie.rating,
        subtitle = movie.releaseYear ?: "${movie.voteCount} votes",
        onClick = { onMovieClick(movie.id) },
        modifier = Modifier
            .width(140.dp)
            .testTag(MoviesTestTags.movieCard(movie.id)),
    ) {
        AsyncImage(
            model = movie.posterUrl,
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
    }
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
            onTrendingWindowSelected = {},
            onRetryClick = {},
            onRefresh = {},
            onMovieClick = {},
            onSearchClick = {},
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
)
