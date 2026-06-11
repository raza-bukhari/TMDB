package com.example.tmdb.core.navigation

import kotlinx.serialization.Serializable

/**
 * Cross-feature navigation contracts. Feature modules depend on these types,
 * never on each other; :app owns the NavHost that binds routes to screens.
 */
@Serializable
data object MoviesRoute

@Serializable
data class MovieDetailRoute(val movieId: Long)

@Serializable
data object SearchRoute
