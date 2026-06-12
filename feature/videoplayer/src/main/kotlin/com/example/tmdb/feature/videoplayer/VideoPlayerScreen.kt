package com.example.tmdb.feature.videoplayer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.koin.androidx.compose.koinViewModel

object VideoPlayerTestTags {
    const val SCREEN = "video_player_screen"
    const val PLAYER = "video_player_surface"
    const val PLAYLIST = "video_player_playlist"
    const val BACK = "video_player_back"
    const val REPLAY = "video_player_replay"
    fun row(key: String) = "video_player_row_$key"
}

@Composable
fun VideoPlayerScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: VideoPlayerViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    VideoPlayerContent(
        state = state,
        startSeconds = { viewModel.resumeSeconds },
        onBackClick = onBackClick,
        onVideoSelected = viewModel::onVideoSelected,
        onVideoEnded = viewModel::onVideoEnded,
        onPositionChanged = viewModel::onPositionChanged,
        onReplay = viewModel::onReplay,
        onRetry = viewModel::onRetry,
        modifier = modifier,
    )
}

@Composable
internal fun VideoPlayerContent(
    state: VideoPlayerUiState,
    startSeconds: () -> Float,
    onBackClick: () -> Unit,
    onVideoSelected: (Int) -> Unit,
    onVideoEnded: () -> Unit,
    onPositionChanged: (Float) -> Unit,
    onReplay: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag(VideoPlayerTestTags.SCREEN),
        ) {
            // Pinned player.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .statusBarsPadding(),
            ) {
                val playerModifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)

                when {
                    state.isLoading -> PlayerPlaceholder(playerModifier) { CircularProgressIndicator(color = Color.White) }
                    state.errorMessage != null -> PlayerPlaceholder(playerModifier) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(state.errorMessage, color = Color.White)
                            Text(
                                text = "Retry",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable(onClick = onRetry),
                            )
                        }
                    }
                    state.currentVideo != null -> PlaylistYouTubePlayer(
                        currentKey = state.currentVideo!!.key,
                        reloadToken = state.reloadToken,
                        startSeconds = startSeconds,
                        onEnded = onVideoEnded,
                        onPosition = onPositionChanged,
                        modifier = playerModifier.testTag(VideoPlayerTestTags.PLAYER),
                    )
                }

                if (state.showReplay) {
                    ReplayOverlay(onReplay = onReplay, modifier = playerModifier)
                }

                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                        .testTag(VideoPlayerTestTags.BACK),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }

            // Playlist.
            Playlist(
                state = state,
                onVideoSelected = onVideoSelected,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun PlayerPlaceholder(modifier: Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun Playlist(
    state: VideoPlayerUiState,
    onVideoSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    // Keep the playing item visible as the playlist advances.
    LaunchedEffect(state.currentIndex) {
        if (state.videos.isNotEmpty()) listState.animateScrollToItem(state.currentIndex)
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = 24.dp),
        modifier = modifier.testTag(VideoPlayerTestTags.PLAYLIST),
    ) {
        item {
            Text(
                text = "Up Next · ${state.videos.size} videos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
        items(state.videos, key = { it.key }) { video ->
            val index = state.videos.indexOf(video)
            PlaylistRow(
                video = video,
                isCurrent = index == state.currentIndex,
                onClick = { onVideoSelected(index) },
            )
        }
        item { Spacer() }
    }
}

@Composable
private fun PlaylistRow(
    video: PlaylistVideoUi,
    isCurrent: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = if (isCurrent) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        border = if (isCurrent) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag(VideoPlayerTestTags.row(video.key)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(68.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            ) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrent) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (isCurrent) "Now playing · ${video.type}" else video.type,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun ReplayOverlay(onReplay: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            onClick = onReplay,
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag(VideoPlayerTestTags.REPLAY),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = "Replay playlist",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

/**
 * Reusable [YouTubePlayerView] (never recreated — only `loadVideo` is called) driven by
 * [reloadToken]: every selection / auto-advance / replay bumps the token, and on rotation
 * the player re-readies and resumes from [startSeconds]. Released with the lifecycle.
 */
@Composable
private fun PlaylistYouTubePlayer(
    currentKey: String,
    reloadToken: Int,
    startSeconds: () -> Float,
    onEnded: () -> Unit,
    onPosition: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val playerRef = remember { mutableStateOf<YouTubePlayer?>(null) }
    val playerView = remember {
        YouTubePlayerView(context).apply { enableAutomaticInitialization = false }
    }

    DisposableEffect(lifecycleOwner) {
        val listener = object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                playerRef.value = youTubePlayer
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                if (state == PlayerConstants.PlayerState.ENDED) onEnded()
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                onPosition(second)
            }
        }
        playerView.initialize(listener)
        lifecycleOwner.lifecycle.addObserver(playerView)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(playerView)
            playerView.release()
        }
    }

    // Load on first ready (incl. resume after rotation) and whenever a (re)play is requested.
    LaunchedEffect(playerRef.value, reloadToken) {
        playerRef.value?.loadVideo(currentKey, startSeconds())
    }

    AndroidView(factory = { playerView }, modifier = modifier)
}

@Composable
private fun Spacer() {
    Box(modifier = Modifier.navigationBarsPadding())
}
