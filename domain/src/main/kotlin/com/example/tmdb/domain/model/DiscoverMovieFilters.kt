package com.example.tmdb.domain.model

data class DiscoverMovieFilters(
    val sortBy: DiscoverMovieSort = DiscoverMovieSort.POPULARITY_DESC,
    val minRating: Double = 0.0,
    val fromYear: Int? = null,
    val toYear: Int? = null,
    val genres: Set<MovieGenre> = emptySet(),
    val mediaType: MediaType? = null,
    val language: String? = null,
    val runtimeGte: Int? = null,
    val runtimeLte: Int? = null,
    val watchProviderId: Int? = null,
    val watchRegion: String = "US",
    val watchMonetizationTypes: String? = null,
)

enum class DiscoverMovieSort(val apiValue: String) {
    POPULARITY_DESC("popularity.desc"),
    RATING_DESC("vote_average.desc"),
    RELEASE_DESC("primary_release_date.desc"),
    RELEASE_ASC("primary_release_date.asc"),
    TITLE_ASC("title.asc"),
}
