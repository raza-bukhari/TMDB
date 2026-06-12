package com.example.tmdb.core.testing

import androidx.paging.PagingData
import com.example.tmdb.domain.model.ExternalRatings
import com.example.tmdb.domain.model.HomeList
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieCategory
import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

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

    // --- home ---

    val homeListCalls = mutableListOf<HomeList>()
    var onHomeList: (HomeList) -> Result<List<Movie>> = { Result.success(emptyList()) }

    override suspend fun homeList(list: HomeList): Result<List<Movie>> {
        homeListCalls += list
        return onHomeList(list)
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

    override suspend fun refreshMovieDetail(id: MovieId): Result<Unit> {
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
