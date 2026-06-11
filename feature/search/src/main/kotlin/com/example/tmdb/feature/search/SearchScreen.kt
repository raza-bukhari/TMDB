package com.example.tmdb.feature.search

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.tmdb.core.designsystem.ThemePreviews
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.tmdb.core.designsystem.component.EmptyState
import com.example.tmdb.core.designsystem.component.ErrorState
import com.example.tmdb.core.designsystem.component.LoadingState
import com.example.tmdb.core.designsystem.component.PosterCard
import com.example.tmdb.core.designsystem.theme.TMDBTheme
import com.example.tmdb.domain.model.AppError
import kotlinx.collections.immutable.persistentListOf
import org.koin.androidx.compose.koinViewModel

object SearchTestTags {
    const val INPUT = "search_input"
    const val GRID = "search_grid"
    const val BACK = "search_back"
    const val APPEND_SPINNER = "search_append_spinner"
}

/** Start asking for the next page this many items before the end of the grid. */
private const val LOAD_MORE_LOOKAHEAD = 6

@Composable
fun SearchScreen(
    onMovieClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SearchViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SearchScreenContent(
        state = state,
        onQueryChanged = viewModel::onQueryChanged,
        onRetryClick = viewModel::onRetryClicked,
        onLoadMoreRequested = viewModel::onLoadMoreRequested,
        onMovieClick = onMovieClick,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@Composable
internal fun SearchScreenContent(
    state: SearchUiState,
    onQueryChanged: (String) -> Unit,
    onRetryClick: () -> Unit,
    onLoadMoreRequested: () -> Unit,
    onMovieClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag(SearchTestTags.BACK),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChanged,
                placeholder = { Text("Search movies…") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 12.dp)
                    .testTag(SearchTestTags.INPUT),
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (val content = state.content) {
                SearchContent.Idle -> EmptyState(message = "Search TMDB by movie title.")
                SearchContent.Loading -> LoadingState()
                SearchContent.NoResults -> EmptyState(message = "No movies match that search.")
                is SearchContent.Error -> ErrorState(
                    message = content.error.toUserMessage(),
                    onRetryClick = onRetryClick,
                )
                is SearchContent.Results -> ResultsGrid(
                    content = content,
                    onMovieClick = onMovieClick,
                    onLoadMoreRequested = onLoadMoreRequested,
                )
            }
        }
    }
}

@Composable
private fun ResultsGrid(
    content: SearchContent.Results,
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
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.testTag(SearchTestTags.GRID),
    ) {
        items(
            items = content.movies,
            key = { it.id },
            contentType = { "movie" },
        ) { movie ->
            PosterCard(
                title = movie.title,
                rating = movie.rating,
                onClick = { onMovieClick(movie.id) },
                modifier = Modifier.animateItem(),
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
                        .testTag(SearchTestTags.APPEND_SPINNER),
                ) {
                    CircularProgressIndicator()
                }
            }
        }
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
private fun SearchResultsPreview() {
    TMDBTheme {
        SearchScreenContent(
            state = SearchUiState(
                query = "incep",
                content = SearchContent.Results(
                    movies = persistentListOf(
                        SearchResultItem(27205, "Inception", null, 8.4),
                        SearchResultItem(504253, "Inception: The Cobol Job", null, 7.3),
                    ),
                    isAppending = true,
                    canLoadMore = true,
                ),
            ),
            onQueryChanged = {},
            onRetryClick = {},
            onLoadMoreRequested = {},
            onMovieClick = {},
            onBackClick = {},
        )
    }
}

@ThemePreviews
@Composable
private fun SearchIdlePreview() {
    TMDBTheme {
        SearchScreenContent(
            state = SearchUiState(),
            onQueryChanged = {},
            onRetryClick = {},
            onLoadMoreRequested = {},
            onMovieClick = {},
            onBackClick = {},
        )
    }
}
