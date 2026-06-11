package com.example.tmdb.feature.movies

import androidx.compose.runtime.Immutable
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieCategory
import kotlinx.collections.immutable.ImmutableList

data class MoviesUiState(
    val selectedCategory: MovieCategory = MovieCategory.POPULAR,
    val isRefreshing: Boolean = false,
    val filters: MovieFilters = MovieFilters(),
    val content: MoviesContent = MoviesContent.Loading,
)

/**
 * Exactly one of these renders at a time. Invariant: cached movies always win —
 * a refresh failure with a non-empty cache keeps showing [Movies], never [Error].
 */
sealed interface MoviesContent {
    data object Loading : MoviesContent
    data object Empty : MoviesContent
    data class Error(val error: AppError) : MoviesContent
    data class Movies(
        val movies: ImmutableList<MovieListItem>,
        val isAppending: Boolean = false,
        val canLoadMore: Boolean = false,
        /** Non-null when the last refresh failed but cached movies are still shown. */
        val staleError: AppError? = null,
    ) : MoviesContent

    /** Cache is non-empty but the active filters exclude everything. */
    data object NoMatches : MoviesContent
}

/** UI projection of [Movie]; poster path already resolved to a loadable URL. */
@Immutable
data class MovieListItem(
    val id: Long,
    val title: String,
    val posterUrl: String?,
    val rating: Double,
    val releaseYear: String?,
)

private const val POSTER_BASE = "https://image.tmdb.org/t/p/w342"

internal fun Movie.toListItem(): MovieListItem = MovieListItem(
    id = id.value,
    title = title,
    posterUrl = posterPath?.let { POSTER_BASE + it },
    rating = voteAverage,
    releaseYear = releaseDate?.year?.toString(),
)

/** Tab label for a category. */
internal fun MovieCategory.label(): String = when (this) {
    MovieCategory.POPULAR -> "Popular"
    MovieCategory.TOP_RATED -> "Top Rated"
    MovieCategory.NOW_PLAYING -> "Now Playing"
}
