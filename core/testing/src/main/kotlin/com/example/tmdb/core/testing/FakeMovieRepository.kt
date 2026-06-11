package com.example.tmdb.core.testing

import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeMovieRepository : MovieRepository {

    val moviesFlow = MutableStateFlow<List<Movie>>(emptyList())
    val detailFlow = MutableStateFlow<MovieDetail?>(null)
    var refreshResult: Result<Unit> = Result.success(Unit)
    var refreshCalls: Int = 0
        private set
    var lastDetailId: MovieId? = null
        private set

    /** When set, refresh applies it to the cache before returning, like a real fetch. */
    var onRefreshCachePopulation: (() -> List<Movie>)? = null

    /** When set, detail refresh populates the detail cache on success. */
    var onDetailRefreshCachePopulation: (() -> MovieDetail)? = null

    override fun observePopularMovies(): Flow<List<Movie>> = moviesFlow

    override suspend fun refreshPopularMovies(): Result<Unit> {
        refreshCalls++
        if (refreshResult.isSuccess) {
            onRefreshCachePopulation?.let { moviesFlow.value = it() }
        }
        return refreshResult
    }

    override fun observeMovieDetail(id: MovieId): Flow<MovieDetail?> {
        lastDetailId = id
        return detailFlow
    }

    override suspend fun refreshMovieDetail(id: MovieId): Result<Unit> {
        lastDetailId = id
        refreshCalls++
        if (refreshResult.isSuccess) {
            onDetailRefreshCachePopulation?.let { detailFlow.value = it() }
        }
        return refreshResult
    }
}
