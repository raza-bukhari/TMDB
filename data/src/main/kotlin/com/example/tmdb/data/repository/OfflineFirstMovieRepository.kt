package com.example.tmdb.data.repository

import com.example.tmdb.core.common.DispatcherProvider
import com.example.tmdb.core.database.MovieCategories
import com.example.tmdb.core.database.MovieDao
import com.example.tmdb.core.database.MovieDetailDao
import com.example.tmdb.core.network.TmdbApi
import com.example.tmdb.core.network.dto.MovieDto
import com.example.tmdb.core.network.dto.PagedResponseDto
import com.example.tmdb.core.network.tmdbCall
import com.example.tmdb.data.mapper.toDomain
import com.example.tmdb.data.mapper.toEntity
import com.example.tmdb.data.mapper.toSearchResults
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieCategory
import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.MoviePage
import com.example.tmdb.domain.model.SearchResults
import com.example.tmdb.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** TMDB returns 20 items per page; used to keep [com.example.tmdb.core.database.MovieEntity.orderIndex] monotonic across appended pages. */
private const val PAGE_SIZE = 20

/** Room is the single source of truth; the network only ever writes into it. */
internal class OfflineFirstMovieRepository(
    private val api: TmdbApi,
    private val dao: MovieDao,
    private val detailDao: MovieDetailDao,
    private val dispatchers: DispatcherProvider,
) : MovieRepository {

    override fun observeMovies(category: MovieCategory): Flow<List<Movie>> =
        dao.observeByCategory(category.cacheKey())
            .map { entities -> entities.map { it.toDomain() } }
            // List mapping is CPU work; keep it off Room's IO threads and the main thread.
            .flowOn(dispatchers.default)

    override suspend fun refreshMovies(category: MovieCategory): Result<MoviePage> =
        withContext(dispatchers.io) {
            tmdbCall { fetchPage(category, page = 1) }.map { response ->
                dao.replaceCategory(
                    category = category.cacheKey(),
                    movies = response.results.mapIndexed { index, dto ->
                        dto.toEntity(category.cacheKey(), index)
                    },
                )
                MoviePage(page = response.page, totalPages = response.totalPages)
            }
        }

    override suspend fun loadMoreMovies(category: MovieCategory, page: Int): Result<MoviePage> =
        withContext(dispatchers.io) {
            tmdbCall { fetchPage(category, page) }.map { response ->
                // Continue orderIndex from where the prior pages left off so the cache stays ranked.
                val base = (response.page - 1) * PAGE_SIZE
                dao.insertAll(
                    response.results.mapIndexed { index, dto ->
                        dto.toEntity(category.cacheKey(), base + index)
                    },
                )
                MoviePage(page = response.page, totalPages = response.totalPages)
            }
        }

    private suspend fun fetchPage(category: MovieCategory, page: Int): PagedResponseDto<MovieDto> =
        when (category) {
            MovieCategory.POPULAR -> api.popularMovies(page)
            MovieCategory.TOP_RATED -> api.topRatedMovies(page)
            MovieCategory.NOW_PLAYING -> api.nowPlayingMovies(page)
        }

    private fun MovieCategory.cacheKey(): String = when (this) {
        MovieCategory.POPULAR -> MovieCategories.POPULAR
        MovieCategory.TOP_RATED -> MovieCategories.TOP_RATED
        MovieCategory.NOW_PLAYING -> MovieCategories.NOW_PLAYING
    }

    override suspend fun searchMovies(query: String, page: Int): Result<SearchResults> =
        withContext(dispatchers.io) {
            tmdbCall { api.searchMovies(query = query, page = page) }.map { it.toSearchResults() }
        }

    override fun observeMovieDetail(id: MovieId): Flow<MovieDetail?> =
        detailDao.observeById(id.value)
            .map { it?.toDomain() }
            .flowOn(dispatchers.default)

    override suspend fun refreshMovieDetail(id: MovieId): Result<Unit> =
        withContext(dispatchers.io) {
            tmdbCall { api.movieDetail(id.value) }.map { detailDao.upsert(it.toEntity()) }
        }
}
