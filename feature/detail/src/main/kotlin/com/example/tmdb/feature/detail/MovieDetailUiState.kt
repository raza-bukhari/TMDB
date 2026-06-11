package com.example.tmdb.feature.detail

import androidx.compose.runtime.Immutable
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.MovieDetail
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
    val genres: ImmutableList<String>,
)

private const val POSTER_BASE = "https://image.tmdb.org/t/p/w342"
private const val BACKDROP_BASE = "https://image.tmdb.org/t/p/w780"

internal fun MovieDetail.toUi(): MovieDetailUi = MovieDetailUi(
    id = id.value,
    title = title,
    tagline = tagline,
    overview = overview,
    posterUrl = posterPath?.let { POSTER_BASE + it },
    backdropUrl = backdropPath?.let { BACKDROP_BASE + it },
    releaseYear = releaseDate?.year?.toString(),
    runtime = runtimeMinutes?.let { formatRuntime(it) },
    rating = voteAverage,
    genres = genres.toImmutableList(),
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
