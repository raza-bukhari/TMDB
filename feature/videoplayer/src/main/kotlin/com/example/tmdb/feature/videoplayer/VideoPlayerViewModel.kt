package com.example.tmdb.feature.videoplayer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.tmdb.core.navigation.VideoPlayerRoute
import com.example.tmdb.domain.model.MediaType
import com.example.tmdb.domain.model.MediaVideo
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.usecase.GetMediaVideosUseCase
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Owns the playlist state machine — selection, sequential auto-advance, end-of-playlist
 * handling and rotation-safe resume position. Deliberately knows nothing about the
 * playback engine: the screen feeds it player events (ended / position) and reacts to
 * [VideoPlayerUiState.currentIndex]. That separation is what lets a Media3 engine replace
 * the YouTube one for direct-URL sources without touching this class.
 */
internal class VideoPlayerViewModel(
    savedStateHandle: SavedStateHandle,
    private val getMediaVideos: GetMediaVideosUseCase,
) : ViewModel() {

    // Configurable end-of-playlist behaviour; default mirrors YouTube's "replay" affordance.
    // Kept off the constructor so Koin's viewModelOf doesn't try to resolve it from the graph.
    private val endBehavior: EndOfPlaylistBehavior = EndOfPlaylistBehavior.SHOW_REPLAY

    private val route = savedStateHandle.toRoute<VideoPlayerRoute>()
    private val movieId = MovieId(route.movieId)
    private val mediaType = runCatching { MediaType.valueOf(route.mediaType) }.getOrDefault(MediaType.MOVIE)

    private val _uiState = MutableStateFlow(VideoPlayerUiState())
    val uiState: StateFlow<VideoPlayerUiState> = _uiState.asStateFlow()

    /**
     * Last known playback position of the current video, in seconds. Plain field (not in
     * the observed state) so the per-second player callback doesn't trigger recomposition;
     * the screen reads it when (re)loading so a rotation resumes instead of restarting.
     */
    var resumeSeconds: Float = 0f
        private set

    init {
        load()
    }

    fun onRetry() = load()

    /** User tapped a playlist row. */
    fun onVideoSelected(index: Int) {
        val state = _uiState.value
        if (index !in state.videos.indices || index == state.currentIndex && !state.showReplay) return
        resumeSeconds = 0f
        _uiState.update {
            it.copy(currentIndex = index, showReplay = false, reloadToken = it.reloadToken + 1)
        }
    }

    /** The current video reported playback completed. */
    fun onVideoEnded() {
        val state = _uiState.value
        if (state.showReplay) return
        val isLast = state.currentIndex >= state.videos.lastIndex
        if (!isLast) {
            advanceTo(state.currentIndex + 1)
            return
        }
        when (endBehavior) {
            EndOfPlaylistBehavior.RESTART -> advanceTo(0)
            EndOfPlaylistBehavior.SHOW_REPLAY -> _uiState.update { it.copy(showReplay = true) }
            EndOfPlaylistBehavior.STOP -> Unit
        }
    }

    /** Replay control after the playlist finished — restart from the top. */
    fun onReplay() {
        if (_uiState.value.videos.isEmpty()) return
        advanceTo(0)
    }

    /** Per-second position tick from the player; kept off the observed state on purpose. */
    fun onPositionChanged(seconds: Float) {
        resumeSeconds = seconds
    }

    private fun advanceTo(index: Int) {
        resumeSeconds = 0f
        _uiState.update {
            it.copy(currentIndex = index, showReplay = false, reloadToken = it.reloadToken + 1)
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val playlist = getMediaVideos(movieId, mediaType)
                .getOrDefault(emptyList())
                .toPlaylist()
            if (playlist.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "No videos available.") }
                return@launch
            }
            val startIndex = playlist.indexOfFirst { it.key == route.startVideoKey }.coerceAtLeast(0)
            resumeSeconds = 0f
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = null,
                    videos = playlist.toImmutableList(),
                    currentIndex = startIndex,
                    showReplay = false,
                    reloadToken = it.reloadToken + 1,
                )
            }
        }
    }
}

/** YouTube videos only, official trailers first, then teasers, clips, the rest. */
private fun List<MediaVideo>.toPlaylist(): List<PlaylistVideoUi> =
    asSequence()
        .filter { it.site.equals("YouTube", ignoreCase = true) && it.key.isNotBlank() }
        .sortedWith(compareBy({ videoTypeRank(it.type) }, { !it.official }))
        .map { PlaylistVideoUi(key = it.key, title = it.name, type = it.type) }
        .toList()

private fun videoTypeRank(type: String): Int = when (type.lowercase()) {
    "trailer" -> 0
    "teaser" -> 1
    "clip" -> 2
    "featurette" -> 3
    "behind the scenes" -> 4
    else -> 5
}
