package com.example.tmdb.feature.detail

import androidx.compose.runtime.Immutable
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.ExternalRatings
import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MediaVideo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class MovieDetailUiState(
    val isRefreshing: Boolean = false,
    val content: MovieDetailContent = MovieDetailContent.Loading,
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
data class MovieSummaryUi(
    val id: Long,
    val title: String,
    val posterUrl: String?,
    val rating: Double,
)

@Immutable
data class MovieDetailUi(
    val id: Long,
    val title: String,
    val tagline: String?,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseYear: String?,
    val runtime: String?,
    val rating: Double,
    val certification: String?,
    val genres: ImmutableList<String>,
    val cast: ImmutableList<CastMemberUi>,
    val directors: ImmutableList<String>,
    val producers: ImmutableList<String>,
    val similarMovies: ImmutableList<MovieSummaryUi>,
    val watchProviders: ImmutableList<WatchProviderUi>,
    val trailerUrl: String? = null,
    val externalRatings: ExternalRatingsUi = ExternalRatingsUi(),
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
): MovieDetailUi = MovieDetailUi(
    id = id.value,
    title = title,
    tagline = tagline,
    overview = overview,
    posterUrl = posterPath?.let { POSTER_BASE + it },
    backdropUrl = backdropPath?.let { BACKDROP_BASE + it },
    releaseYear = releaseDate?.year?.toString(),
    runtime = runtimeMinutes?.let { formatRuntime(it) },
    rating = voteAverage,
    certification = certification,
    genres = genres.toImmutableList(),
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
    watchProviders = watchProviders.map { provider ->
        WatchProviderUi(
            id = provider.id,
            name = provider.name,
            logoUrl = provider.logoPath?.let { LOGO_BASE + it }
        )
    }.toImmutableList(),
    trailerUrl = null,
    externalRatings = externalRatings.toUi(),
    isWatchlisted = isWatchlisted,
)

internal fun List<MediaVideo>.primaryTrailerUrl(): String? =
    firstOrNull { it.type.equals("Trailer", ignoreCase = true) && it.official }?.youtubeUrl
        ?: firstOrNull { it.type.equals("Trailer", ignoreCase = true) }?.youtubeUrl
        ?: firstNotNullOfOrNull { it.youtubeUrl }

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
