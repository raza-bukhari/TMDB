package com.example.tmdb.domain.repository

import com.example.tmdb.domain.model.Movie
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for movies. Implementations are offline-first:
 * [observePopularMovies] emits cached data immediately and again after any refresh.
 */
interface MovieRepository {
    fun observePopularMovies(): Flow<List<Movie>>

    /** Fetches page 1 from TMDB into the cache. Failure carries an [com.example.tmdb.domain.model.AppException]. */
    suspend fun refreshPopularMovies(): Result<Unit>
}
