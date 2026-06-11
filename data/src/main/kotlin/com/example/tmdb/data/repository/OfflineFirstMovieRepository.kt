package com.example.tmdb.data.repository

import com.example.tmdb.core.common.DispatcherProvider
import com.example.tmdb.core.database.MovieCategories
import com.example.tmdb.core.database.MovieDao
import com.example.tmdb.core.network.TmdbApi
import com.example.tmdb.core.network.tmdbCall
import com.example.tmdb.data.mapper.toDomain
import com.example.tmdb.data.mapper.toEntity
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Room is the single source of truth; the network only ever writes into it. */
internal class OfflineFirstMovieRepository(
    private val api: TmdbApi,
    private val dao: MovieDao,
    private val dispatchers: DispatcherProvider,
) : MovieRepository {

    override fun observePopularMovies(): Flow<List<Movie>> =
        dao.observeByCategory(MovieCategories.POPULAR)
            .map { entities -> entities.map { it.toDomain() } }
            // List mapping is CPU work; keep it off Room's IO threads and the main thread.
            .flowOn(dispatchers.default)

    override suspend fun refreshPopularMovies(): Result<Unit> =
        withContext(dispatchers.io) {
            tmdbCall { api.popularMovies(page = 1) }.map { page ->
                dao.replaceCategory(
                    category = MovieCategories.POPULAR,
                    movies = page.results.mapIndexed { index, dto ->
                        dto.toEntity(MovieCategories.POPULAR, index)
                    },
                )
            }
        }
}
