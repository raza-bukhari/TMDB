package com.example.tmdb.core.testing

import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieCategory
import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.MoviePage
import com.example.tmdb.domain.model.SearchResults
import com.example.tmdb.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeMovieRepository : MovieRepository {

    // --- category lists ---

    private val categoryFlows = mutableMapOf<MovieCategory, MutableStateFlow<List<Movie>>>()

    /** Backing cache flow for [category], created on first access. Tests mutate `.value` to seed the cache. */
    fun moviesFlow(category: MovieCategory): MutableStateFlow<List<Movie>> =
        categoryFlows.getOrPut(category) { MutableStateFlow(emptyList()) }

    val refreshedCategories = mutableListOf<MovieCategory>()
    val loadMoreCalls = mutableListOf<Pair<MovieCategory, Int>>()

    /** Programmable refresh; may also seed [moviesFlow] to mimic a real fetch. */
    var onRefresh: (MovieCategory) -> Result<MoviePage> = { Result.success(MoviePage(page = 1, totalPages = 1)) }
    var onLoadMore: (MovieCategory, Int) -> Result<MoviePage> =
        { _, page -> Result.success(MoviePage(page = page, totalPages = page)) }

    override fun observeMovies(category: MovieCategory): Flow<List<Movie>> = moviesFlow(category)

    override suspend fun refreshMovies(category: MovieCategory): Result<MoviePage> {
        refreshedCategories += category
        return onRefresh(category)
    }

    override suspend fun loadMoreMovies(category: MovieCategory, page: Int): Result<MoviePage> {
        loadMoreCalls += category to page
        return onLoadMore(category, page)
    }

    // --- search ---

    /** Queue of (query, page) pairs received by [searchMovies], oldest first. */
    val searchCalls = mutableListOf<Pair<String, Int>>()

    /** Programmable search behaviour; defaults to one empty page. */
    var onSearch: (query: String, page: Int) -> Result<SearchResults> = { _, page ->
        Result.success(SearchResults(movies = emptyList(), page = page, totalPages = page))
    }

    override suspend fun searchMovies(query: String, page: Int): Result<SearchResults> {
        searchCalls += query to page
        return onSearch(query, page)
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
}
