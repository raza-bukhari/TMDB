package com.example.tmdb.core.testing

import androidx.paging.PagingData
import com.example.tmdb.domain.model.DiscoverMovieFilters
import com.example.tmdb.domain.model.ExternalRatings
import com.example.tmdb.domain.model.HomeList
import com.example.tmdb.domain.model.MediaType
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieCategory
import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.MovieId
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
    val watchlistIdsFlow = MutableStateFlow<Set<MovieId>>(emptySet())

    override fun observeWatchlist(): Flow<List<Movie>> = watchlistFlow

    override fun observeWatchlistIds(): Flow<Set<MovieId>> = watchlistIdsFlow

    override suspend fun addToWatchlist(movie: Movie): Result<Unit> {
        watchlistFlow.update { current -> listOf(movie) + current.filterNot { it.id == movie.id } }
        watchlistIdsFlow.update { it + movie.id }
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
        )
        return addToWatchlist(movie)
    }

    override suspend fun removeFromWatchlist(id: MovieId): Result<Unit> {
        watchlistFlow.update { current -> current.filterNot { it.id == id } }
        watchlistIdsFlow.update { it - id }
        return Result.success(Unit)
    }

    // --- detail ---

    val detailFlow = MutableStateFlow<MovieDetail?>(null)
    var refreshResult: Result<Unit> = Result.success(Unit)
    var lastDetailId: MovieId? = null
        private set

    /** When set, detail refresh populates the detail cache on success. */
    var onDetailRefreshCachePopulation: (() -> MovieDetail)? = null

    override fun observeMovieDetail(id: MovieId): Flow<MovieDetail?> {
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
}
