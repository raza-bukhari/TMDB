package com.example.tmdb.domain.model

/** The browseable movie lists TMDB exposes as tabs. */
enum class MovieCategory {
    POPULAR,
    TOP_RATED,
    NOW_PLAYING,
}

/** Paging bounds for a category list; the cached items themselves flow separately. */
data class MoviePage(
    val page: Int,
    val totalPages: Int,
) {
    val canLoadMore: Boolean get() = page < totalPages
}
