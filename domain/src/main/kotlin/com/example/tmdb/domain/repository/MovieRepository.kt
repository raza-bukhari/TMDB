package com.example.tmdb.domain.repository

import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.MovieId
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for movies. Implementations are offline-first:
 * observe* flows emit cached data immediately and again after any refresh.
 * refresh* failures carry an [com.example.tmdb.domain.model.AppException].
 */
interface MovieRepository {
    fun observePopularMovies(): Flow<List<Movie>>

    suspend fun refreshPopularMovies(): Result<Unit>

    /** Emits `null` while the movie has never been cached. */
    fun observeMovieDetail(id: MovieId): Flow<MovieDetail?>

    suspend fun refreshMovieDetail(id: MovieId): Result<Unit>
}
