package com.example.tmdb.feature.movies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
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

object MoviesTestTags {
    const val GRID = "movies_grid"
    fun movieCard(id: Long) = "movie_card_$id"
}

@Composable
fun MoviesScreen(
    onMovieClick: (Long) -> Unit,
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
        onRetryClick = viewModel::onRetryClicked,
        onMovieClick = viewModel::onMovieClicked,
        modifier = modifier,
    )
}

@Composable
internal fun MoviesScreenContent(
    state: MoviesUiState,
    onRetryClick: () -> Unit,
    onMovieClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        when (val content = state.content) {
            MoviesContent.Loading -> LoadingState()
            MoviesContent.Empty -> EmptyState(message = "No movies right now. Check back later.")
            is MoviesContent.Error -> ErrorState(
                message = content.error.toUserMessage(),
                onRetryClick = onRetryClick,
            )
            is MoviesContent.Movies -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.testTag(MoviesTestTags.GRID),
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
                        modifier = Modifier.testTag(MoviesTestTags.movieCard(movie.id)),
                    ) {
                        AsyncImage(
                            model = movie.posterUrl,
                            contentDescription = movie.title,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
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

@Preview(showBackground = true)
@Composable
private fun MoviesScreenContentPreview() {
    TMDBTheme {
        MoviesScreenContent(
            state = MoviesUiState(
                content = MoviesContent.Movies(
                    persistentListOf(
                        MovieListItem(1, "Fight Club", null, 8.4),
                        MovieListItem(2, "The Matrix", null, 8.2),
                    ),
                ),
            ),
            onRetryClick = {},
            onMovieClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MoviesScreenErrorPreview() {
    TMDBTheme {
        MoviesScreenContent(
            state = MoviesUiState(content = MoviesContent.Error(AppError.Offline)),
            onRetryClick = {},
            onMovieClick = {},
        )
    }
}
