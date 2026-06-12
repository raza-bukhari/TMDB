package com.example.tmdb.feature.movies

import com.example.tmdb.domain.model.DiscoverMovieFilters
import com.example.tmdb.domain.model.DiscoverMovieSort
import com.example.tmdb.domain.model.MediaType
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieGenre

/** How the visible movie list is ordered (applied client-side over the cached list). */
enum class MovieSort(val label: String) {
    DEFAULT("TMDB order"),
    RATING_DESC("Rating"),
    RELEASE_DESC("Newest"),
    RELEASE_ASC("Oldest"),
    TITLE_ASC("A–Z"),
}

enum class LanguageFilter(val label: String, val code: String?) {
    ANY("Any language", null),
    ENGLISH("English", "en"),
    SPANISH("Spanish", "es"),
    KOREAN("Korean", "ko"),
    JAPANESE("Japanese", "ja"),
    HINDI("Hindi", "hi"),
}

enum class RuntimeFilter(val label: String, val minMinutes: Int?, val maxMinutes: Int?) {
    ANY("Any runtime", null, null),
    SHORT("Under 90m", null, 89),
    STANDARD("90m-150m", 90, 150),
    LONG("Over 150m", 151, null),
}

enum class WatchProviderFilter(val label: String, val providerId: Int?) {
    ANY("Any provider", null),
    NETFLIX("Netflix", 8),
    PRIME("Prime Video", 9),
    DISNEY("Disney+", 337),
    MAX("Max", 1899),
}

enum class AvailabilityFilter(val label: String, val apiValue: String?) {
    ANY("Any availability", null),
    STREAM("Streaming", "flatrate"),
    RENT("Rent", "rent"),
    BUY("Buy", "buy"),
}

/** Bounds for the year RangeSlider; widened only if the data falls outside. */
const val MIN_FILTER_YEAR = 1950
const val MAX_FILTER_YEAR = 2026

data class MovieFilters(
    val sort: MovieSort = MovieSort.DEFAULT,
    val minRating: Float = 0f,
    val fromYear: Int = MIN_FILTER_YEAR,
    val toYear: Int = MAX_FILTER_YEAR,
    val genres: Set<MovieGenre> = emptySet(),
    val mediaType: MediaType = MediaType.MOVIE,
    val language: LanguageFilter = LanguageFilter.ANY,
    val runtime: RuntimeFilter = RuntimeFilter.ANY,
    val watchProvider: WatchProviderFilter = WatchProviderFilter.ANY,
    val region: String = "US",
    val availability: AvailabilityFilter = AvailabilityFilter.ANY,
) {
    /** Number of non-default constraints, for the filter badge. */
    val activeCount: Int =
        (if (sort != MovieSort.DEFAULT) 1 else 0) +
            (if (minRating > 0f) 1 else 0) +
            (if (fromYear > MIN_FILTER_YEAR || toYear < MAX_FILTER_YEAR) 1 else 0) +
            (if (genres.isNotEmpty()) 1 else 0) +
            (if (mediaType != MediaType.MOVIE) 1 else 0) +
            (if (language != LanguageFilter.ANY) 1 else 0) +
            (if (runtime != RuntimeFilter.ANY) 1 else 0) +
            (if (watchProvider != WatchProviderFilter.ANY) 1 else 0) +
            (if (region != "US") 1 else 0) +
            (if (availability != AvailabilityFilter.ANY) 1 else 0)

    val isDefault: Boolean get() = activeCount == 0
}

internal fun Movie.matches(filters: MovieFilters): Boolean {
    val yearWindowFull = filters.fromYear <= MIN_FILTER_YEAR && filters.toYear >= MAX_FILTER_YEAR
    val genreIds = filters.genres.map { it.id }.toSet()

    val ratingOk = voteAverage >= filters.minRating
    val year = releaseDate?.year
    val yearOk = if (year == null) yearWindowFull else year in filters.fromYear..filters.toYear
    val genreOk = genreIds.isEmpty() || this.genreIds.any { it in genreIds }

    return ratingOk && yearOk && genreOk
}

/** Filters then sorts [movies]; a movie with no release year is kept only when the year window is full. */
internal fun List<Movie>.applyFilters(filters: MovieFilters): List<Movie> {
    val yearWindowFull = filters.fromYear <= MIN_FILTER_YEAR && filters.toYear >= MAX_FILTER_YEAR
    val genreIds = filters.genres.map { it.id }.toSet()
    val filtered = filter { movie ->
        val ratingOk = movie.voteAverage >= filters.minRating
        val year = movie.releaseDate?.year
        val yearOk = if (year == null) yearWindowFull else year in filters.fromYear..filters.toYear
        // Match if the movie carries ANY of the selected genres.
        val genreOk = genreIds.isEmpty() || movie.genreIds.any { it in genreIds }
        ratingOk && yearOk && genreOk
    }
    return when (filters.sort) {
        MovieSort.DEFAULT -> filtered
        MovieSort.RATING_DESC -> filtered.sortedByDescending { it.voteAverage }
        MovieSort.RELEASE_DESC -> filtered.sortedByDescending { it.releaseDate }
        MovieSort.RELEASE_ASC -> filtered.sortedBy { it.releaseDate }
        MovieSort.TITLE_ASC -> filtered.sortedBy { it.title.lowercase() }
    }
}

internal fun MovieListItem.matches(query: String, filters: MovieFilters): Boolean {
    val trimmedQuery = query.trim()
    val queryOk = trimmedQuery.isBlank() ||
        title.contains(trimmedQuery, ignoreCase = true) ||
        overview.contains(trimmedQuery, ignoreCase = true)
    val yearWindowFull = filters.fromYear <= MIN_FILTER_YEAR && filters.toYear >= MAX_FILTER_YEAR
    val genreIds = filters.genres.map { it.id }.toSet()
    val year = releaseYear?.toIntOrNull()
    val ratingOk = rating >= filters.minRating
    val yearOk = if (year == null) yearWindowFull else year in filters.fromYear..filters.toYear
    val genreOk = genreIds.isEmpty() || this.genreIds.any { it in genreIds }
    return queryOk && ratingOk && yearOk && genreOk
}

internal fun List<MovieListItem>.applyFilters(query: String, filters: MovieFilters): List<MovieListItem> {
    val filtered = filter { movie -> movie.matches(query, filters) }
    return when (filters.sort) {
        MovieSort.DEFAULT -> filtered
        MovieSort.RATING_DESC -> filtered.sortedByDescending { it.rating }
        MovieSort.RELEASE_DESC -> filtered.sortedByDescending { it.releaseYear?.toIntOrNull() }
        MovieSort.RELEASE_ASC -> filtered.sortedBy { it.releaseYear?.toIntOrNull() ?: Int.MAX_VALUE }
        MovieSort.TITLE_ASC -> filtered.sortedBy { it.title.lowercase() }
    }
}

internal fun MovieFilters.toDiscoverMovieFilters(): DiscoverMovieFilters = DiscoverMovieFilters(
    sortBy = when (sort) {
        MovieSort.DEFAULT -> DiscoverMovieSort.POPULARITY_DESC
        MovieSort.RATING_DESC -> DiscoverMovieSort.RATING_DESC
        MovieSort.RELEASE_DESC -> DiscoverMovieSort.RELEASE_DESC
        MovieSort.RELEASE_ASC -> DiscoverMovieSort.RELEASE_ASC
        MovieSort.TITLE_ASC -> DiscoverMovieSort.TITLE_ASC
    },
    minRating = minRating.toDouble(),
    fromYear = fromYear.takeUnless { it <= MIN_FILTER_YEAR },
    toYear = toYear.takeUnless { it >= MAX_FILTER_YEAR },
    genres = genres,
    mediaType = mediaType,
    language = language.code,
    runtimeGte = runtime.minMinutes,
    runtimeLte = runtime.maxMinutes,
    watchProviderId = watchProvider.providerId,
    watchRegion = region,
    watchMonetizationTypes = availability.apiValue,
)

internal fun MovieFilters.activeLabels(): List<String> = buildList {
    if (mediaType != MediaType.MOVIE) add("Series")
    if (sort != MovieSort.DEFAULT) add(sort.label)
    if (minRating > 0f) add("${"%.1f".format(minRating)}+")
    if (fromYear > MIN_FILTER_YEAR || toYear < MAX_FILTER_YEAR) add("$fromYear-$toYear")
    genres.forEach { add(it.displayName) }
    if (language != LanguageFilter.ANY) add(language.label)
    if (runtime != RuntimeFilter.ANY) add(runtime.label)
    if (watchProvider != WatchProviderFilter.ANY) add(watchProvider.label)
    if (region != "US") add(region)
    if (availability != AvailabilityFilter.ANY) add(availability.label)
}
