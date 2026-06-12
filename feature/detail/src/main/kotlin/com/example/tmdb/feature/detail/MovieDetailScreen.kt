package com.example.tmdb.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.tmdb.core.designsystem.ThemePreviews
import com.example.tmdb.core.designsystem.component.ErrorState
import com.example.tmdb.core.designsystem.component.GlassSurface
import com.example.tmdb.core.designsystem.component.GradientPrimaryButton
import com.example.tmdb.core.designsystem.component.LoadingState
import com.example.tmdb.core.designsystem.component.RatingRing
import com.example.tmdb.core.designsystem.theme.TMDBTheme
import com.example.tmdb.domain.model.AppError
import kotlinx.collections.immutable.persistentListOf
import org.koin.androidx.compose.koinViewModel

object DetailTestTags {
    const val SCREEN = "detail_screen"
    const val BACK = "detail_back"
    const val WATCHLIST = "detail_watchlist"
    const val TRAILER = "detail_trailer"
}

private val HeaderMaxHeight = 300.dp
private val ToolbarHeight = 56.dp

@Composable
fun MovieDetailScreen(
    onBackClick: () -> Unit,
    onMovieClick: (Long) -> Unit,
    onPersonClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: MovieDetailViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    MovieDetailScreenContent(
        state = state,
        onBackClick = onBackClick,
        onMovieClick = onMovieClick,
        onPersonClick = onPersonClick,
        onRetryClick = viewModel::onRetryClicked,
        onWatchlistToggle = viewModel::onWatchlistToggle,
        onTrailerClick = { url -> uriHandler.openUri(url) },
        modifier = modifier,
    )
}

@Composable
internal fun MovieDetailScreenContent(
    state: MovieDetailUiState,
    onBackClick: () -> Unit,
    onMovieClick: (Long) -> Unit,
    onPersonClick: (Long) -> Unit,
    onRetryClick: () -> Unit,
    onWatchlistToggle: () -> Unit,
    onTrailerClick: (String) -> Unit,
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
                is MovieDetailContent.Detail -> CollapsingDetail(content.detail, onBackClick, onMovieClick, onPersonClick, onWatchlistToggle, onTrailerClick)
            }
        }
    }
}

@Composable
private fun CollapsingDetail(
    detail: MovieDetailUi,
    onBackClick: () -> Unit,
    onMovieClick: (Long) -> Unit,
    onPersonClick: (Long) -> Unit,
    onWatchlistToggle: () -> Unit,
    onTrailerClick: (String) -> Unit,
) {
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    val headerMaxPx = with(density) { HeaderMaxHeight.toPx() }
    val toolbarPx = with(density) { ToolbarHeight.toPx() }

    val collapseFraction by remember {
        derivedStateOf { (scroll.value / (headerMaxPx - toolbarPx)).coerceIn(0f, 1f) }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val infoMinHeight = maxHeight - ToolbarHeight

        AsyncImage(
            model = detail.backdropUrl ?: detail.posterUrl,
            contentDescription = detail.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(HeaderMaxHeight)
                .graphicsLayer {
                    translationY = -scroll.value * 0.5f
                    alpha = 1f - collapseFraction * 0.9f
                }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            0.55f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.55f),
                        ),
                    )
                },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll),
        ) {
            Spacer(Modifier.height(HeaderMaxHeight))
            DetailInfo(detail, onMovieClick, onPersonClick, onWatchlistToggle, onTrailerClick, modifier = Modifier.heightIn(min = infoMinHeight))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = collapseFraction))
                .statusBarsPadding()
                .height(ToolbarHeight),
        ) {
            BackButton(onBackClick, scrim = collapseFraction < 0.5f)
            DetailWatchlistButton(
                isWatchlisted = detail.isWatchlisted,
                onClick = onWatchlistToggle,
                scrim = collapseFraction < 0.5f,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .statusBarsPadding()
                    .padding(end = 4.dp),
            )
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
private fun DetailInfo(
    detail: MovieDetailUi,
    onMovieClick: (Long) -> Unit,
    onPersonClick: (Long) -> Unit,
    onWatchlistToggle: () -> Unit,
    onTrailerClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Main Metadata
        Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RatingRing(rating = detail.rating)
                Text(
                    text = detail.title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f),
                )
                DetailWatchlistButton(
                    isWatchlisted = detail.isWatchlisted,
                    onClick = onWatchlistToggle,
                    scrim = false,
                )
            }
            if (detail.externalRatings.hasAny) {
                ExternalRatingsRow(detail.externalRatings)
            }
            if (detail.trailerUrl != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    GradientPrimaryButton(
                        text = "Trailer",
                        onClick = { onTrailerClick(detail.trailerUrl) },
                        modifier = Modifier.testTag(DetailTestTags.TRAILER),
                    )
                    GlassSurface(cornerRadius = 999.dp, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(
                            text = "Official video",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            detail.tagline?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                detail.certification?.let {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                detail.releaseYear?.let { Text(it, style = MaterialTheme.typography.labelLarge) }
                detail.runtime?.let { Text(it, style = MaterialTheme.typography.labelLarge) }
                detail.status?.let { Text(it, style = MaterialTheme.typography.labelLarge) }
            }
            SeriesStatsRow(detail)
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

        if (detail.seasons.isNotEmpty()) {
            SeasonsSection(detail.seasons)
        }

        if (detail.episodes.isNotEmpty()) {
            EpisodesSection(detail.episodes)
        }

        if (detail.lastEpisode != null || detail.nextEpisode != null) {
            EpisodeMilestonesSection(
                lastEpisode = detail.lastEpisode,
                nextEpisode = detail.nextEpisode,
            )
        }

        // Watch Providers
        if (detail.watchProviders.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Where to Watch",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(detail.watchProviders) { provider ->
                        AsyncImage(
                            model = provider.logoUrl,
                            contentDescription = provider.name,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
                Text(
                    text = "Powered by JustWatch",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Cast
        if (detail.cast.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Top Cast",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(detail.cast) { member ->
                        Column(
                            modifier = Modifier
                                .width(100.dp)
                                .clickable { onPersonClick(member.id) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = member.profileUrl,
                                contentDescription = member.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = member.name,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = member.character,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Crew
        if (detail.directors.isNotEmpty() || detail.producers.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (detail.directors.isNotEmpty()) {
                    CrewRow(label = "Director", names = detail.directors)
                }
                if (detail.producers.isNotEmpty()) {
                    CrewRow(label = "Producer", names = detail.producers)
                }
            }
        }

        // Similar Movies
        if (detail.similarMovies.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "You Might Also Like",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(detail.similarMovies) { movie ->
                        Card(
                            modifier = Modifier
                                .width(120.dp)
                                .clickable { onMovieClick(movie.id) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column {
                                AsyncImage(
                                    model = movie.posterUrl,
                                    contentDescription = movie.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(2f / 3f)
                                )
                                Text(
                                    text = movie.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeriesStatsRow(detail: MovieDetailUi) {
    val chips = buildList {
        detail.seasonCount?.let { add(if (it == 1) "1 season" else "$it seasons") }
        detail.episodeCount?.let { add(if (it == 1) "1 episode" else "$it episodes") }
    }
    if (chips.isEmpty()) return

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        chips.forEach { chip ->
            GlassSurface(cornerRadius = 999.dp, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)) {
                Text(
                    text = chip,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SeasonsSection(seasons: List<TvSeasonUi>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Seasons",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(seasons, key = { it.id }) { season ->
                GlassSurface(
                    modifier = Modifier.width(132.dp),
                    contentPadding = PaddingValues(8.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        AsyncImage(
                            model = season.posterUrl,
                            contentDescription = season.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(2f / 3f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        )
                        Text(
                            text = season.name,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = listOfNotNull(
                                season.airYear,
                                "${season.episodeCount} eps",
                            ).joinToString(" · "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodesSection(episodes: List<TvEpisodeUi>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Episodes",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            episodes.take(8).forEach { episode ->
                EpisodeRow(episode)
            }
        }
    }
}

@Composable
private fun EpisodeMilestonesSection(
    lastEpisode: TvEpisodeUi?,
    nextEpisode: TvEpisodeUi?,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        lastEpisode?.let { EpisodeMilestoneCard("Last Episode", it) }
        nextEpisode?.let { EpisodeMilestoneCard("Next Episode", it) }
    }
}

@Composable
private fun EpisodeMilestoneCard(label: String, episode: TvEpisodeUi) {
    GlassSurface(contentPadding = PaddingValues(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            Text(text = "S${episode.seasonNumber} E${episode.episodeNumber} · ${episode.title}", style = MaterialTheme.typography.titleSmall)
            episode.airDate?.let {
                Text(text = it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EpisodeRow(episode: TvEpisodeUi) {
    GlassSurface(contentPadding = PaddingValues(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = episode.stillUrl,
                contentDescription = episode.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(112.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "E${episode.episodeNumber} · ${episode.title}",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val meta = listOfNotNull(episode.airDate, episode.runtime).joinToString(" · ")
                if (meta.isNotBlank()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (episode.overview.isNotBlank()) {
                    Text(
                        text = episode.overview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailWatchlistButton(
    isWatchlisted: Boolean,
    onClick: () -> Unit,
    scrim: Boolean,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .then(if (scrim) Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.35f)) else Modifier)
            .testTag(DetailTestTags.WATCHLIST),
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = if (isWatchlisted) "Remove from watchlist" else "Add to watchlist",
            tint = if (isWatchlisted) MaterialTheme.colorScheme.secondary else if (scrim) Color.White else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ExternalRatingsRow(ratings: ExternalRatingsUi) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ratings.imdb?.let { RatingChip(label = "IMDb", value = it) }
        ratings.rottenTomatoes?.let { RatingChip(label = "RT", value = it) }
        ratings.metascore?.let { RatingChip(label = "Metascore", value = it) }
    }
}

@Composable
private fun RatingChip(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(6.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(text = value, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun CrewRow(label: String, names: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = names.joinToString(", "),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        status = null,
                        seasonCount = null,
                        episodeCount = null,
                        rating = 8.4,
                        certification = "R",
                        genres = persistentListOf("Drama", "Thriller"),
                        seasons = persistentListOf(),
                        episodes = persistentListOf(),
                        lastEpisode = null,
                        nextEpisode = null,
                        cast = persistentListOf(),
                        directors = persistentListOf("David Fincher"),
                        producers = persistentListOf(),
                        similarMovies = persistentListOf(),
                        watchProviders = persistentListOf(),
                        externalRatings = ExternalRatingsUi(
                            imdb = "8.8/10",
                            rottenTomatoes = "81%",
                            metascore = "66/100",
                        ),
                    ),
                ),
            ),
            onBackClick = {},
            onMovieClick = {},
            onPersonClick = {},
            onRetryClick = {},
            onWatchlistToggle = {},
            onTrailerClick = {},
        )
    }
}
