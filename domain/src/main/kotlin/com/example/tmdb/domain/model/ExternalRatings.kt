package com.example.tmdb.domain.model

/**
 * Third-party scores sourced from OMDb (TMDB itself exposes none of these).
 * Values are display-ready strings as OMDb provides them (e.g. "8.8", "87%").
 */
data class ExternalRatings(
    val imdb: String? = null,
    val rottenTomatoes: String? = null,
    val metascore: String? = null,
) {
    val isEmpty: Boolean get() = imdb == null && rottenTomatoes == null && metascore == null
}
