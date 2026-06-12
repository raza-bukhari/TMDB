package com.example.tmdb.feature.detail

import androidx.compose.runtime.Immutable
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.ExternalRatings
import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MediaType
import com.example.tmdb.domain.model.MediaVideo
import com.example.tmdb.domain.model.TvEpisode
import com.example.tmdb.domain.model.TvSeason
import com.example.tmdb.domain.model.WatchProvider
import com.example.tmdb.domain.model.WatchlistItem
import com.example.tmdb.domain.model.WatchlistStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

data class MovieDetailUiState(
    val isRefreshing: Boolean = false,
    val content: MovieDetailContent = MovieDetailContent.Loading,
    val selectedEpisode: TvEpisodeUi? = null,
    val isEpisodeLoading: Boolean = false,
)

/** Cached detail always wins over a refresh error (same invariant as the movies list). */
sealed interface MovieDetailContent {
    data object Loading : MovieDetailContent
    data class Error(val error: AppError) : MovieDetailContent
    data class Detail(val detail: MovieDetailUi) : MovieDetailContent
}

@Immutable
data class CastMemberUi(
    val id: Long,
    val name: String,
    val character: String,
    val profileUrl: String?,
)

@Immutable
data class WatchProviderUi(
    val id: Int,
    val name: String,
    val logoUrl: String?,
)

@Immutable
data class VideoUi(
    val key: String,
    val name: String,
    val type: String,
) {
    /** YouTube still used as the carousel card preview before playback starts. */
    val thumbnailUrl: String get() = "https://img.youtube.com/vi/$key/hqdefault.jpg"
}

@Immutable
data class MovieSummaryUi(
    val id: Long,
    val title: String,
    val posterUrl: String?,
    val rating: Double,
)

@Immutable
data class TvSeasonUi(
    val id: Long,
    val name: String,
    val posterUrl: String?,
    val airYear: String?,
    val seasonNumber: Int,
    val episodeCount: Int,
    val rating: Double,
)

@Immutable
data class TvEpisodeUi(
    val id: Long,
    val title: String,
    val stillUrl: String?,
    val airDate: String?,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val runtime: String?,
    val rating: Double,
    val overview: String,
)

@Immutable
data class UserActivityUi(
    val status: WatchlistStatus = WatchlistStatus.PLAN_TO_WATCH,
    val favorite: Boolean = false,
    val userRating: Double? = null,
    val watchedDate: String? = null,
    val notes: String = "",
)

@Immutable
data class MovieDetailUi(
    val id: Long,
    val mediaType: MediaType = MediaType.MOVIE,
    val title: String,
    val tagline: String?,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseYear: String?,
    val runtime: String?,
    val status: String?,
    val seasonCount: Int?,
    val episodeCount: Int?,
    val rating: Double,
    val certification: String?,
    val genres: ImmutableList<String>,
    val seasons: ImmutableList<TvSeasonUi>,
    val selectedSeasonNumber: Int?,
    val episodes: ImmutableList<TvEpisodeUi>,
    val lastEpisode: TvEpisodeUi?,
    val nextEpisode: TvEpisodeUi?,
    val cast: ImmutableList<CastMemberUi>,
    val directors: ImmutableList<String>,
    val producers: ImmutableList<String>,
    val similarMovies: ImmutableList<MovieSummaryUi>,
    val watchProviders: ImmutableList<WatchProviderUi>,
    val videos: ImmutableList<VideoUi> = persistentListOf(),
    val externalRatings: ExternalRatingsUi = ExternalRatingsUi(),
    val userActivity: UserActivityUi? = null,
    val isWatchlisted: Boolean = false,
)

@Immutable
data class ExternalRatingsUi(
    val imdb: String? = null,
    val rottenTomatoes: String? = null,
    val metascore: String? = null,
) {
    val hasAny: Boolean get() = imdb != null || rottenTomatoes != null || metascore != null
}

private const val POSTER_BASE = "https://image.tmdb.org/t/p/w342"
private const val BACKDROP_BASE = "https://image.tmdb.org/t/p/w780"
private const val PROFILE_BASE = "https://image.tmdb.org/t/p/w185"
private const val LOGO_BASE = "https://image.tmdb.org/t/p/w92"

internal fun MovieDetail.toUi(
    externalRatings: ExternalRatings = ExternalRatings(),
    isWatchlisted: Boolean = false,
    watchProvidersOverride: List<WatchProvider>? = null,
): MovieDetailUi = MovieDetailUi(
    id = id.value,
    mediaType = mediaType,
    title = title,
    tagline = tagline,
    overview = overview,
    posterUrl = posterPath?.let { POSTER_BASE + it },
    backdropUrl = backdropPath?.let { BACKDROP_BASE + it },
    releaseYear = releaseDate?.year?.toString(),
    runtime = runtimeMinutes?.let { formatRuntime(it) },
    status = status,
    seasonCount = numberOfSeasons,
    episodeCount = numberOfEpisodes,
    rating = voteAverage,
    certification = certification,
    genres = genres.toImmutableList(),
    seasons = seasons.map { it.toUi() }.toImmutableList(),
    selectedSeasonNumber = seasons.firstOrNull()?.seasonNumber,
    episodes = persistentListOf(),
    lastEpisode = lastEpisodeToAir?.toUi(),
    nextEpisode = nextEpisodeToAir?.toUi(),
    cast = cast.map { member ->
        CastMemberUi(
            id = member.id,
            name = member.name,
            character = member.character,
            profileUrl = member.profilePath?.let { PROFILE_BASE + it }
        )
    }.toImmutableList(),
    directors = directors.toImmutableList(),
    producers = producers.toImmutableList(),
    similarMovies = similarMovies.map { movie ->
        MovieSummaryUi(
            id = movie.id.value,
            title = movie.title,
            posterUrl = movie.posterPath?.let { POSTER_BASE + it },
            rating = movie.voteAverage
        )
    }.toImmutableList(),
    watchProviders = (watchProvidersOverride ?: watchProviders).map { provider ->
        WatchProviderUi(
            id = provider.id,
            name = provider.name,
            logoUrl = provider.logoPath?.let { LOGO_BASE + it }
        )
    }.toImmutableList(),
    videos = persistentListOf(),
    externalRatings = externalRatings.toUi(),
    userActivity = null,
    isWatchlisted = isWatchlisted,
)

/** YouTube videos for the carousel, official trailers first, then teasers, clips, the rest. */
internal fun List<MediaVideo>.toVideoUiList(): List<VideoUi> =
    asSequence()
        .filter { it.site.equals("YouTube", ignoreCase = true) && it.key.isNotBlank() }
        .sortedWith(compareBy({ videoTypeRank(it.type) }, { !it.official }))
        .map { VideoUi(key = it.key, name = it.name, type = it.type) }
        .toList()

private fun videoTypeRank(type: String): Int = when (type.lowercase()) {
    "trailer" -> 0
    "teaser" -> 1
    "clip" -> 2
    "featurette" -> 3
    "behind the scenes" -> 4
    else -> 5
}

internal fun TvSeason.toUi(): TvSeasonUi = TvSeasonUi(
    id = id,
    name = name,
    posterUrl = posterPath?.let { POSTER_BASE + it },
    airYear = airDate?.year?.toString(),
    seasonNumber = seasonNumber,
    episodeCount = episodeCount,
    rating = voteAverage,
)

internal fun TvEpisode.toUi(): TvEpisodeUi = TvEpisodeUi(
    id = id,
    title = name,
    stillUrl = stillPath?.let { BACKDROP_BASE + it },
    airDate = airDate?.toString(),
    seasonNumber = seasonNumber,
    episodeNumber = episodeNumber,
    runtime = runtimeMinutes?.let { formatRuntime(it) },
    rating = voteAverage,
    overview = overview,
)

internal fun WatchlistItem.toActivityUi(): UserActivityUi = UserActivityUi(
    status = status,
    favorite = favorite,
    userRating = userRating,
    watchedDate = watchedDate?.toString(),
    notes = notes,
)

private fun ExternalRatings.toUi(): ExternalRatingsUi = ExternalRatingsUi(
    imdb = imdb?.let { "$it/10" },
    rottenTomatoes = rottenTomatoes,
    metascore = metascore?.let { "$it/100" },
)

internal fun formatRuntime(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours == 0 -> "${minutes}m"
        minutes == 0 -> "${hours}h"
        else -> "${hours}h ${minutes}m"
    }
}
