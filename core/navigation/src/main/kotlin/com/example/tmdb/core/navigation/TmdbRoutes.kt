package com.example.tmdb.core.navigation

import kotlinx.serialization.Serializable

/**
 * Cross-feature navigation contracts. Feature modules depend on these types,
 * never on each other; :app owns the NavHost that binds routes to screens.
 */
@Serializable
data object MoviesRoute

@Serializable
data class MovieDetailRoute(
    val movieId: Long,
    val mediaType: String = "MOVIE",
)

@Serializable
data class PersonRoute(
    val personId: Long,
)

@Serializable
data class VideoPlayerRoute(
    val movieId: Long,
    val mediaType: String = "MOVIE",
    val startVideoKey: String,
)
