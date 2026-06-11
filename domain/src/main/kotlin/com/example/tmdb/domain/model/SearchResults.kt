package com.example.tmdb.domain.model

/** One page of search results. Search is network-only by design (D-006). */
data class SearchResults(
    val movies: List<Movie>,
    val page: Int,
    val totalPages: Int,
) {
    val canLoadMore: Boolean get() = page < totalPages
}
