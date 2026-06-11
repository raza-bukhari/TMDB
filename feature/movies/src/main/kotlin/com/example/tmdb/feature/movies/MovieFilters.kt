package com.example.tmdb.feature.movies

import com.example.tmdb.domain.model.Movie

/** How the visible movie list is ordered (applied client-side over the cached list). */
enum class MovieSort(val label: String) {
    DEFAULT("TMDB order"),
    RATING_DESC("Rating"),
    RELEASE_DESC("Newest"),
    RELEASE_ASC("Oldest"),
    TITLE_ASC("A–Z"),
}

/** Bounds for the year RangeSlider; widened only if the data falls outside. */
const val MIN_FILTER_YEAR = 1950
const val MAX_FILTER_YEAR = 2026

data class MovieFilters(
    val sort: MovieSort = MovieSort.DEFAULT,
    val minRating: Float = 0f,
    val fromYear: Int = MIN_FILTER_YEAR,
    val toYear: Int = MAX_FILTER_YEAR,
) {
    /** Number of non-default constraints, for the filter badge. */
    val activeCount: Int =
        (if (sort != MovieSort.DEFAULT) 1 else 0) +
            (if (minRating > 0f) 1 else 0) +
            (if (fromYear > MIN_FILTER_YEAR || toYear < MAX_FILTER_YEAR) 1 else 0)

    val isDefault: Boolean get() = activeCount == 0
}

/** Filters then sorts [movies]; a movie with no release year is kept only when the year window is full. */
internal fun List<Movie>.applyFilters(filters: MovieFilters): List<Movie> {
    val yearWindowFull = filters.fromYear <= MIN_FILTER_YEAR && filters.toYear >= MAX_FILTER_YEAR
    val filtered = filter { movie ->
        val ratingOk = movie.voteAverage >= filters.minRating
        val year = movie.releaseDate?.year
        val yearOk = if (year == null) yearWindowFull else year in filters.fromYear..filters.toYear
        ratingOk && yearOk
    }
    return when (filters.sort) {
        MovieSort.DEFAULT -> filtered
        MovieSort.RATING_DESC -> filtered.sortedByDescending { it.voteAverage }
        MovieSort.RELEASE_DESC -> filtered.sortedByDescending { it.releaseDate }
        MovieSort.RELEASE_ASC -> filtered.sortedBy { it.releaseDate }
        MovieSort.TITLE_ASC -> filtered.sortedBy { it.title.lowercase() }
    }
}
