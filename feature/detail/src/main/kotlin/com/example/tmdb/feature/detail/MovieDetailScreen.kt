package com.example.tmdb.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.tmdb.core.designsystem.ThemePreviews
import com.example.tmdb.core.designsystem.component.ErrorState
import com.example.tmdb.core.designsystem.component.LoadingState
import com.example.tmdb.core.designsystem.component.RatingRing
import com.example.tmdb.core.designsystem.theme.TMDBTheme
import com.example.tmdb.domain.model.AppError
import kotlinx.collections.immutable.persistentListOf
import org.koin.androidx.compose.koinViewModel

object DetailTestTags {
    const val SCREEN = "detail_screen"
    const val BACK = "detail_back"
}

private val HeaderMaxHeight = 300.dp
private val ToolbarHeight = 56.dp

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
                is MovieDetailContent.Error -> {
                    ErrorState(message = content.error.toUserMessage(), onRetryClick = onRetryClick)
                    BackButton(onBackClick, scrim = false)
                }
                is MovieDetailContent.Detail -> CollapsingDetail(content.detail, onBackClick)
            }
        }
    }
}

@Composable
private fun CollapsingDetail(detail: MovieDetailUi, onBackClick: () -> Unit) {
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    val headerMaxPx = with(density) { HeaderMaxHeight.toPx() }
    val toolbarPx = with(density) { ToolbarHeight.toPx() }

    // 0f fully expanded → 1f collapsed to the toolbar.
    val collapseFraction by remember {
        derivedStateOf { (scroll.value / (headerMaxPx - toolbarPx)).coerceIn(0f, 1f) }
    }

    Box(Modifier.fillMaxSize()) {
        // 1) Backdrop, pinned behind the content with a parallax + fade as it collapses.
        AsyncImage(
            model = detail.backdropUrl ?: detail.posterUrl,
            contentDescription = detail.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(HeaderMaxHeight)
                .graphicsLayer {
                    translationY = -scroll.value * 0.5f
                    alpha = 1f - collapseFraction * 0.9f
                }
                .drawWithContent {
                    drawContent()
                    // Bottom gradient so the title/scrim stays legible on bright posters.
                    drawRect(
                        brush = Brush.verticalGradient(
                            0.55f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.55f),
                        ),
                    )
                },
        )

        // 2) Scrolling content: transparent spacer reveals the backdrop; surface scrolls over it.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll),
        ) {
            Spacer(Modifier.height(HeaderMaxHeight))
            DetailInfo(detail)
        }

        // 3) Pinned toolbar — background and title fade in as the header collapses.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = collapseFraction))
                .statusBarsPadding()
                .height(ToolbarHeight),
        ) {
            BackButton(onBackClick, scrim = collapseFraction < 0.5f)
            Text(
                text = detail.title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 56.dp)
                    .graphicsLayer { alpha = collapseFraction },
            )
        }
    }
}

@Composable
private fun DetailInfo(detail: MovieDetailUi, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RatingRing(rating = detail.rating)
            Text(
                text = detail.title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
        }
        detail.tagline?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            detail.releaseYear?.let { Text(it, style = MaterialTheme.typography.labelLarge) }
            detail.runtime?.let { Text(it, style = MaterialTheme.typography.labelLarge) }
        }
        if (detail.genres.isNotEmpty()) {
            Text(
                text = detail.genres.joinToString(" · "),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = detail.overview,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BoxScope.BackButton(onBackClick: () -> Unit, scrim: Boolean) {
    IconButton(
        onClick = onBackClick,
        modifier = Modifier
            .align(Alignment.CenterStart)
            .statusBarsPadding()
            .padding(start = 4.dp)
            .then(
                if (scrim) Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.35f)) else Modifier,
            )
            .testTag(DetailTestTags.BACK),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = if (scrim) Color.White else MaterialTheme.colorScheme.onSurface,
        )
    }
}

internal fun AppError.toUserMessage(): String = when (this) {
    AppError.Offline -> "You're offline. Connect and try again."
    AppError.InvalidToken -> "TMDB rejected the API token. Check your local.properties."
    AppError.NotFound -> "This movie isn't available."
    AppError.RateLimited -> "Too many requests. Give it a moment and retry."
    is AppError.Unknown -> "Something went wrong. Please try again."
}

@ThemePreviews
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

@ThemePreviews
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
