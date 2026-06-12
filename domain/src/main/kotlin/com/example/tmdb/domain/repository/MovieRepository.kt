package com.example.tmdb.domain.repository

import androidx.paging.PagingData
import com.example.tmdb.domain.model.DiscoverMovieFilters
import com.example.tmdb.domain.model.ExternalRatings
import com.example.tmdb.domain.model.HomeList
import com.example.tmdb.domain.model.MediaType
import com.example.tmdb.domain.model.MediaVideo
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieCategory
import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.MovieId
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for movies. Implementations are offline-first:
 * observe* flows emit cached data immediately and again after any refresh.
 * refresh* failures carry an [com.example.tmdb.domain.model.AppException].
 */
interface MovieRepository {
    fun observeMovies(category: MovieCategory): Flow<PagingData<Movie>>

    /** Emits `null` while the movie has never been cached. */
    fun observeMovieDetail(id: MovieId): Flow<MovieDetail?>

    suspend fun refreshMovieDetail(id: MovieId, mediaType: MediaType = MediaType.MOVIE): Result<Unit>

    /** Network-only (D-006). */
    fun searchMovies(query: String): Flow<PagingData<Movie>>

    /** Network-only paged TMDB discovery; used by the Discover tab. */
    fun discoverMovies(filters: DiscoverMovieFilters): Flow<PagingData<Movie>>

    /** One page of a curated front-page list; network-only, carousels don't paginate. */
    suspend fun homeList(list: HomeList): Result<List<Movie>>

    fun observeWatchlist(): Flow<List<Movie>>

    fun observeWatchlistIds(): Flow<Set<MovieId>>

    suspend fun addToWatchlist(movie: Movie): Result<Unit>

    suspend fun addToWatchlist(detail: MovieDetail): Result<Unit>

    suspend fun removeFromWatchlist(id: MovieId): Result<Unit>

    /** IMDb/RT/Metacritic scores via OMDb; empty success when no OMDb key is configured. */
    suspend fun externalRatings(imdbId: String): Result<ExternalRatings>

    suspend fun videos(id: MovieId, mediaType: MediaType): Result<List<MediaVideo>>
}
