package com.example.tmdb.core.testing

import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeMovieRepository : MovieRepository {

    val moviesFlow = MutableStateFlow<List<Movie>>(emptyList())
    var refreshResult: Result<Unit> = Result.success(Unit)
    var refreshCalls: Int = 0
        private set

    /** When set, refresh applies it to the cache before returning, like a real fetch. */
    var onRefreshCachePopulation: (() -> List<Movie>)? = null

    override fun observePopularMovies(): Flow<List<Movie>> = moviesFlow

    override suspend fun refreshPopularMovies(): Result<Unit> {
        refreshCalls++
        if (refreshResult.isSuccess) {
            onRefreshCachePopulation?.let { moviesFlow.value = it() }
        }
        return refreshResult
    }
}
