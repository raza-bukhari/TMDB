package com.example.tmdb.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.tmdb.core.common.DispatcherProvider
import com.example.tmdb.core.database.MovieDao
import com.example.tmdb.core.database.MovieDetailDao
import com.example.tmdb.core.database.WatchlistDao
import com.example.tmdb.core.network.OmdbApi
import com.example.tmdb.core.network.OmdbConfig
import com.example.tmdb.core.network.TmdbApi
import com.example.tmdb.core.network.dto.VideoDto
import com.example.tmdb.core.network.tmdbCall
import com.example.tmdb.data.mapper.toDomain
import com.example.tmdb.data.mapper.toEntity
import com.example.tmdb.data.mapper.toWatchlistEntity
import com.example.tmdb.data.mapper.toWatchlistItem
import com.example.tmdb.domain.model.DiscoverMovieFilters
import com.example.tmdb.domain.model.ExternalRatings
import com.example.tmdb.domain.model.HomeList
import com.example.tmdb.domain.model.MediaType
import com.example.tmdb.domain.model.MediaVideo
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieCategory
import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.TvEpisode
import com.example.tmdb.domain.model.TvSeason
import com.example.tmdb.domain.model.UserMediaActivity
import com.example.tmdb.domain.model.WatchlistItem
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
    private val watchlistDao: WatchlistDao,
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

    override fun discoverMovies(filters: DiscoverMovieFilters): Flow<PagingData<Movie>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { DiscoverPagingSource(api, filters) },
        ).flow
            .flowOn(dispatchers.default)

    override fun observeMovieDetail(id: MovieId): Flow<MovieDetail?> =
        detailDao.observeById(id.value)
            .map { it?.toDomain() }
            .flowOn(dispatchers.default)

    override suspend fun refreshMovieDetail(id: MovieId, mediaType: MediaType): Result<Unit> =
        withContext(dispatchers.io) {
            tmdbCall {
                when (mediaType) {
                    MediaType.MOVIE -> api.movieDetail(id.value)
                    MediaType.TV -> api.tvDetail(id.value)
                }
            }.map { detailDao.upsert(it.toEntity()) }
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

    override fun observeWatchlist(): Flow<List<Movie>> =
        watchlistDao.observeWatchlist()
            .map { movies -> movies.map { it.toDomain() } }
            .flowOn(dispatchers.default)

    override fun observeWatchlistItems(): Flow<List<WatchlistItem>> =
        watchlistDao.observeWatchlist()
            .map { movies -> movies.map { it.toWatchlistItem() } }
            .flowOn(dispatchers.default)

    override fun observeWatchlistIds(): Flow<Set<MovieId>> =
        watchlistDao.observeWatchlistIds()
            .map { ids -> ids.map(::MovieId).toSet() }
            .flowOn(dispatchers.default)

    override suspend fun addToWatchlist(movie: Movie): Result<Unit> =
        withContext(dispatchers.io) {
            runCatching { watchlistDao.upsert(movie.toWatchlistEntity(System.currentTimeMillis())) }
        }

    override suspend fun addToWatchlist(detail: MovieDetail): Result<Unit> =
        withContext(dispatchers.io) {
            runCatching { watchlistDao.upsert(detail.toWatchlistEntity(System.currentTimeMillis())) }
        }

    override suspend fun removeFromWatchlist(id: MovieId): Result<Unit> =
        withContext(dispatchers.io) {
            runCatching { watchlistDao.delete(id.value) }
        }

    override suspend fun updateUserActivity(activity: UserMediaActivity): Result<Unit> =
        withContext(dispatchers.io) {
            runCatching {
                watchlistDao.updateActivity(
                    id = activity.mediaId.value,
                    status = activity.status.name,
                    favorite = activity.favorite,
                    userRating = activity.userRating,
                    watchedDate = activity.watchedDate?.toString(),
                    notes = activity.notes,
                )
            }
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

    override suspend fun videos(id: MovieId, mediaType: MediaType): Result<List<MediaVideo>> =
        withContext(dispatchers.io) {
            tmdbCall {
                when (mediaType) {
                    MediaType.MOVIE -> api.movieVideos(id.value)
                    MediaType.TV -> api.tvVideos(id.value)
                }
            }.map { response ->
                response.results
                    .filter { it.site.equals("YouTube", ignoreCase = true) }
                    .sortedWith(
                        compareByDescending<VideoDto> { it.official }
                            .thenBy { if (it.type.equals("Trailer", ignoreCase = true)) 0 else 1 },
                    )
                    .map { it.toDomain() }
            }
        }

    override suspend fun tvSeason(seriesId: MovieId, seasonNumber: Int): Result<TvSeason> =
        withContext(dispatchers.io) {
            tmdbCall { api.tvSeason(seriesId.value, seasonNumber) }
                .map { it.toDomain() }
        }

    override suspend fun tvEpisode(seriesId: MovieId, seasonNumber: Int, episodeNumber: Int): Result<TvEpisode> =
        withContext(dispatchers.io) {
            tmdbCall { api.tvEpisode(seriesId.value, seasonNumber, episodeNumber) }
                .map { it.toDomain() }
        }
}
