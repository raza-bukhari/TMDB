package com.example.tmdb.core.testing

import androidx.paging.PagingData
import com.example.tmdb.domain.model.DiscoverMovieFilters
import com.example.tmdb.domain.model.ExternalRatings
import com.example.tmdb.domain.model.HomeList
import com.example.tmdb.domain.model.MediaKey
import com.example.tmdb.domain.model.MediaType
import com.example.tmdb.domain.model.MediaVideo
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieCategory
import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.Person
import com.example.tmdb.domain.model.PersonCredits
import com.example.tmdb.domain.model.TvEpisode
import com.example.tmdb.domain.model.TvSeason
import com.example.tmdb.domain.model.UserMediaActivity
import com.example.tmdb.domain.model.WatchProviderRegion
import com.example.tmdb.domain.model.WatchlistItem
import com.example.tmdb.domain.model.WatchlistStatus
import com.example.tmdb.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeMovieRepository : MovieRepository {

    // --- category lists ---

    private val categoryFlows = mutableMapOf<MovieCategory, MutableStateFlow<List<Movie>>>()

    /** Backing cache flow for [category], created on first access. Tests mutate `.value` to seed the cache. */
    fun moviesFlow(category: MovieCategory): MutableStateFlow<List<Movie>> =
        categoryFlows.getOrPut(category) { MutableStateFlow(emptyList()) }

    override fun observeMovies(category: MovieCategory): Flow<PagingData<Movie>> =
        moviesFlow(category).map { PagingData.from(it) }

    // --- search ---

    /** Queue of (query) pairs received by [searchMovies], oldest first. */
    val searchCalls = mutableListOf<String>()

    /** Programmable search behaviour; defaults to one empty page. */
    var onSearch: (query: String) -> List<Movie> = { emptyList() }

    override fun searchMovies(query: String): Flow<PagingData<Movie>> {
        searchCalls += query
        return flowOf(PagingData.from(onSearch(query)))
    }

    var onDiscover: (DiscoverMovieFilters) -> List<Movie> = { emptyList() }

    override fun discoverMovies(filters: DiscoverMovieFilters): Flow<PagingData<Movie>> =
        flowOf(PagingData.from(onDiscover(filters)))

    // --- home ---

    val homeListCalls = mutableListOf<HomeList>()
    var onHomeList: (HomeList) -> Result<List<Movie>> = { Result.success(emptyList()) }

    override suspend fun homeList(list: HomeList): Result<List<Movie>> {
        homeListCalls += list
        return onHomeList(list)
    }

    // --- watchlist ---

    val watchlistFlow = MutableStateFlow<List<Movie>>(emptyList())
    val watchlistItemsFlow = MutableStateFlow<List<WatchlistItem>>(emptyList())
    val watchlistKeysFlow = MutableStateFlow<Set<MediaKey>>(emptySet())

    override fun observeWatchlist(): Flow<List<Movie>> = watchlistFlow

    override fun observeWatchlistItems(): Flow<List<WatchlistItem>> = watchlistItemsFlow

    override fun observeWatchlistKeys(): Flow<Set<MediaKey>> = watchlistKeysFlow

    override suspend fun addToWatchlist(movie: Movie): Result<Unit> {
        watchlistFlow.update { current -> listOf(movie) + current.filterNot { it.mediaKey == movie.mediaKey } }
        watchlistItemsFlow.update { current ->
            listOf(movie.toWatchlistItem()) + current.filterNot { it.movie.mediaKey == movie.mediaKey }
        }
        watchlistKeysFlow.update { it + movie.mediaKey }
        return Result.success(Unit)
    }

    override suspend fun addToWatchlist(detail: MovieDetail): Result<Unit> {
        val movie = Movie(
            id = detail.id,
            title = detail.title,
            overview = detail.overview,
            posterPath = detail.posterPath,
            backdropPath = detail.backdropPath,
            releaseDate = detail.releaseDate,
            voteAverage = detail.voteAverage,
            voteCount = detail.voteCount,
            mediaType = detail.mediaType,
        )
        return addToWatchlist(movie)
    }

    override suspend fun removeFromWatchlist(id: MovieId, mediaType: MediaType): Result<Unit> {
        val key = MediaKey(id, mediaType)
        watchlistFlow.update { current -> current.filterNot { it.mediaKey == key } }
        watchlistItemsFlow.update { current -> current.filterNot { it.movie.mediaKey == key } }
        watchlistKeysFlow.update { it - key }
        return Result.success(Unit)
    }

    override suspend fun updateUserActivity(activity: UserMediaActivity): Result<Unit> {
        watchlistItemsFlow.update { current ->
            current.map { item ->
                if (item.movie.mediaKey == MediaKey(activity.mediaId, activity.mediaType)) {
                    item.copy(
                        status = activity.status,
                        favorite = activity.favorite,
                        userRating = activity.userRating,
                        watchedDate = activity.watchedDate,
                        notes = activity.notes,
                    )
                } else {
                    item
                }
            }
        }
        return Result.success(Unit)
    }

    // --- detail ---

    val detailFlow = MutableStateFlow<MovieDetail?>(null)
    var refreshResult: Result<Unit> = Result.success(Unit)
    var lastDetailId: MovieId? = null
        private set

    /** When set, detail refresh populates the detail cache on success. */
    var onDetailRefreshCachePopulation: (() -> MovieDetail)? = null

    override fun observeMovieDetail(id: MovieId, mediaType: MediaType): Flow<MovieDetail?> {
        lastDetailId = id
        return detailFlow
    }

    override suspend fun refreshMovieDetail(id: MovieId, mediaType: MediaType): Result<Unit> {
        lastDetailId = id
        if (refreshResult.isSuccess) {
            onDetailRefreshCachePopulation?.let { detailFlow.value = it() }
        }
        return refreshResult
    }

    // --- external ratings ---

    val externalRatingCalls = mutableListOf<String>()
    var externalRatingsResult: Result<ExternalRatings> = Result.success(ExternalRatings())

    override suspend fun externalRatings(imdbId: String): Result<ExternalRatings> {
        externalRatingCalls += imdbId
        return externalRatingsResult
    }

    var videosResult: Result<List<MediaVideo>> = Result.success(emptyList())

    override suspend fun videos(id: MovieId, mediaType: MediaType): Result<List<MediaVideo>> = videosResult

    var watchProvidersResult: Result<WatchProviderRegion> = Result.success(WatchProviderRegion(region = "US", link = null))

    override suspend fun watchProviders(id: MovieId, mediaType: MediaType, region: String): Result<WatchProviderRegion> =
        watchProvidersResult

    var tvSeasonResult: Result<TvSeason> = Result.success(
        TvSeason(
            id = 1,
            name = "Season 1",
            overview = "",
            posterPath = null,
            airDate = null,
            seasonNumber = 1,
            episodeCount = 0,
            voteAverage = 0.0,
        ),
    )

    override suspend fun tvSeason(seriesId: MovieId, seasonNumber: Int): Result<TvSeason> = tvSeasonResult

    var tvEpisodeResult: Result<TvEpisode> = Result.success(
        TvEpisode(
            id = 1,
            name = "Episode 1",
            overview = "",
            stillPath = null,
            airDate = null,
            seasonNumber = 1,
            episodeNumber = 1,
            runtimeMinutes = null,
            voteAverage = 0.0,
            voteCount = 0,
        ),
    )

    override suspend fun tvEpisode(seriesId: MovieId, seasonNumber: Int, episodeNumber: Int): Result<TvEpisode> =
        tvEpisodeResult

    override suspend fun personDetail(personId: Long): Result<Person> =
        Result.success(
            Person(
                id = personId,
                name = "Person $personId",
                biography = "",
                profilePath = null,
                birthday = null,
                deathday = null,
                placeOfBirth = null,
                knownForDepartment = null,
                popularity = 0.0,
            ),
        )

    override suspend fun personCredits(personId: Long): Result<PersonCredits> =
        Result.success(PersonCredits(cast = emptyList(), crew = emptyList()))

    private fun Movie.toWatchlistItem(): WatchlistItem = WatchlistItem(
        movie = this,
        status = WatchlistStatus.PLAN_TO_WATCH,
        favorite = false,
        userRating = null,
        watchedDate = null,
        notes = "",
        addedAtMillis = 0,
    )
}
