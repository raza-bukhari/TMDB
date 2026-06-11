package com.example.tmdb.domain.repository

import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieCategory
import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.MoviePage
import com.example.tmdb.domain.model.SearchResults
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for movies. Implementations are offline-first:
 * observe* flows emit cached data immediately and again after any refresh.
 * refresh* failures carry an [com.example.tmdb.domain.model.AppException].
 */
interface MovieRepository {
    fun observeMovies(category: MovieCategory): Flow<List<Movie>>

    /** Fetches page 1 and replaces the category cache; returns the new paging bounds. */
    suspend fun refreshMovies(category: MovieCategory): Result<MoviePage>

    /** Fetches [page] and appends it to the category cache; returns the new paging bounds. */
    suspend fun loadMoreMovies(category: MovieCategory, page: Int): Result<MoviePage>

    /** Emits `null` while the movie has never been cached. */
    fun observeMovieDetail(id: MovieId): Flow<MovieDetail?>

    suspend fun refreshMovieDetail(id: MovieId): Result<Unit>

    /** Network-only (D-006); results are never cached. */
    suspend fun searchMovies(query: String, page: Int = 1): Result<SearchResults>
}
