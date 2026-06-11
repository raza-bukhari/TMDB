package com.example.tmdb.feature.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.tmdb.core.designsystem.component.ErrorState
import com.example.tmdb.core.designsystem.component.LoadingState
import com.example.tmdb.core.designsystem.component.RatingBadge
import com.example.tmdb.core.designsystem.theme.TMDBTheme
import com.example.tmdb.domain.model.AppError
import kotlinx.collections.immutable.persistentListOf
import org.koin.androidx.compose.koinViewModel

object DetailTestTags {
    const val SCREEN = "detail_screen"
    const val BACK = "detail_back"
}

@Composable
fun MovieDetailScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: MovieDetailViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MovieDetailScreenContent(
        state = state,
        onBackClick = onBackClick,
        onRetryClick = viewModel::onRetryClicked,
        modifier = modifier,
    )
}

@Composable
internal fun MovieDetailScreenContent(
    state: MovieDetailUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.testTag(DetailTestTags.SCREEN)) {
            when (val content = state.content) {
                MovieDetailContent.Loading -> LoadingState()
                is MovieDetailContent.Error -> ErrorState(
                    message = content.error.toUserMessage(),
                    onRetryClick = onRetryClick,
                )
                is MovieDetailContent.Detail -> DetailBody(content.detail)
            }
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .statusBarsPadding()
                    .testTag(DetailTestTags.BACK),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        }
    }
}

@Composable
private fun DetailBody(detail: MovieDetailUi, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        AsyncImage(
            model = detail.backdropUrl ?: detail.posterUrl,
            contentDescription = detail.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
        )
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = detail.title, style = MaterialTheme.typography.headlineMedium)
            detail.tagline?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RatingBadge(rating = detail.rating)
                detail.releaseYear?.let { Text(text = it, style = MaterialTheme.typography.labelLarge) }
                detail.runtime?.let { Text(text = it, style = MaterialTheme.typography.labelLarge) }
            }
            if (detail.genres.isNotEmpty()) {
                Text(
                    text = detail.genres.joinToString(" · "),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(text = detail.overview, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

internal fun AppError.toUserMessage(): String = when (this) {
    AppError.Offline -> "You're offline. Connect and try again."
    AppError.InvalidToken -> "TMDB rejected the API token. Check your local.properties."
    AppError.NotFound -> "This movie isn't available."
    AppError.RateLimited -> "Too many requests. Give it a moment and retry."
    is AppError.Unknown -> "Something went wrong. Please try again."
}

@Preview(showBackground = true)
@Composable
private fun MovieDetailPreview() {
    TMDBTheme {
        MovieDetailScreenContent(
            state = MovieDetailUiState(
                content = MovieDetailContent.Detail(
                    MovieDetailUi(
                        id = 550,
                        title = "Fight Club",
                        tagline = "Mischief. Mayhem. Soap.",
                        overview = "A ticking-time-bomb insomniac and a slippery soap salesman channel primal male aggression into a shocking new form of therapy.",
                        posterUrl = null,
                        backdropUrl = null,
                        releaseYear = "1999",
                        runtime = "2h 19m",
                        rating = 8.4,
                        genres = persistentListOf("Drama", "Thriller"),
                    ),
                ),
            ),
            onBackClick = {},
            onRetryClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MovieDetailErrorPreview() {
    TMDBTheme {
        MovieDetailScreenContent(
            state = MovieDetailUiState(content = MovieDetailContent.Error(AppError.NotFound)),
            onBackClick = {},
            onRetryClick = {},
        )
    }
}
