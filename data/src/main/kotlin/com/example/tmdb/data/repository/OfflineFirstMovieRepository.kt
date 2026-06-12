package com.example.tmdb.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.tmdb.core.common.DispatcherProvider
import com.example.tmdb.core.database.MovieDao
import com.example.tmdb.core.database.MovieDetailDao
import com.example.tmdb.core.network.OmdbApi
import com.example.tmdb.core.network.OmdbConfig
import com.example.tmdb.core.network.TmdbApi
import com.example.tmdb.core.network.tmdbCall
import com.example.tmdb.data.mapper.toDomain
import com.example.tmdb.data.mapper.toEntity
import com.example.tmdb.domain.model.ExternalRatings
import com.example.tmdb.domain.model.HomeList
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieCategory
import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val PAGE_SIZE = 20

internal class OfflineFirstMovieRepository(
    private val api: TmdbApi,
    private val omdbApi: OmdbApi,
    private val dao: MovieDao,
    private val detailDao: MovieDetailDao,
    private val dispatchers: DispatcherProvider,
) : MovieRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun observeMovies(category: MovieCategory): Flow<PagingData<Movie>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            remoteMediator = MovieRemoteMediator(category, api, dao),
            pagingSourceFactory = { dao.getPagingSource(category.name.lowercase()) }
        ).flow
            .map { pagingData -> pagingData.map { it.toDomain() } }
            .flowOn(dispatchers.default)

    override fun searchMovies(query: String): Flow<PagingData<Movie>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { SearchPagingSource(api, query) }
        ).flow
            .flowOn(dispatchers.default)

    override fun observeMovieDetail(id: MovieId): Flow<MovieDetail?> =
        detailDao.observeById(id.value)
            .map { it?.toDomain() }
            .flowOn(dispatchers.default)

    override suspend fun refreshMovieDetail(id: MovieId): Result<Unit> =
        withContext(dispatchers.io) {
            tmdbCall { api.movieDetail(id.value) }.map { detailDao.upsert(it.toEntity()) }
        }

    override suspend fun homeList(list: HomeList): Result<List<Movie>> =
        withContext(dispatchers.io) {
            tmdbCall {
                when (list) {
                    HomeList.TRENDING_TODAY -> api.trendingMovies(timeWindow = "day")
                    HomeList.TRENDING_THIS_WEEK -> api.trendingMovies(timeWindow = "week")
                    HomeList.POPULAR -> api.popularMovies()
                    HomeList.NOW_PLAYING -> api.nowPlayingMovies()
                    HomeList.TOP_RATED -> api.topRatedMovies()
                    HomeList.UPCOMING -> api.upcomingMovies()
                }
            }.map { response -> response.results.map { it.toDomain() } }
        }

    override suspend fun externalRatings(imdbId: String): Result<ExternalRatings> =
        withContext(dispatchers.io) {
            if (!OmdbConfig.isConfigured) {
                Result.success(ExternalRatings())
            } else {
                tmdbCall { omdbApi.ratingsByImdbId(imdbId) }
                    .map { it.toDomain() }
            }
        }
}
