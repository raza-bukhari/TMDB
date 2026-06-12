package com.example.tmdb.feature.videoplayer

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** One entry in the player's playlist. Duration is not available from TMDB's video feed. */
@Immutable
data class PlaylistVideoUi(
    val key: String,
    val title: String,
    val type: String,
) {
    val thumbnailUrl: String get() = "https://img.youtube.com/vi/$key/hqdefault.jpg"
}

/** What happens after the final video in the playlist ends. */
enum class EndOfPlaylistBehavior {
    /** Stop on the last frame and show a replay control (default, YouTube-like). */
    SHOW_REPLAY,

    /** Loop back to the first video and keep playing. */
    RESTART,

    /** Stop on the last frame with no further affordance. */
    STOP,
}

@Immutable
data class VideoPlayerUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val videos: ImmutableList<PlaylistVideoUi> = persistentListOf(),
    val currentIndex: Int = 0,
    /** Set when the playlist has finished and a replay affordance should be shown. */
    val showReplay: Boolean = false,
    /**
     * Bumped on every explicit (re)play request — selection, auto-advance, replay.
     * The screen keys the player's load call on this so even a same-index replay reloads.
     */
    val reloadToken: Int = 0,
) {
    val currentVideo: PlaylistVideoUi? get() = videos.getOrNull(currentIndex)
}
