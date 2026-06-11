package com.example.tmdb.domain

import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieCategory
import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.MoviePage
import com.example.tmdb.domain.model.SearchResults
import com.example.tmdb.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Minimal programmable [MovieRepository] for domain use-case tests. */
class FakeRepo : MovieRepository {
    val movies = MutableStateFlow<List<Movie>>(emptyList())
    val detail = MutableStateFlow<MovieDetail?>(null)

    var refreshResult: Result<MoviePage> = Result.success(MoviePage(1, 1))
    var loadMoreResult: Result<MoviePage> = Result.success(MoviePage(2, 2))
    var detailResult: Result<Unit> = Result.success(Unit)
    var searchResult: (String, Int) -> Result<SearchResults> = { _, page ->
        Result.success(SearchResults(emptyList(), page, page))
    }

    var lastCategory: MovieCategory? = null
    var lastLoadMore: Pair<MovieCategory, Int>? = null
    var lastDetailId: MovieId? = null
    var lastSearch: Pair<String, Int>? = null

    override fun observeMovies(category: MovieCategory): Flow<List<Movie>> {
        lastCategory = category
        return movies
    }

    override suspend fun refreshMovies(category: MovieCategory): Result<MoviePage> {
        lastCategory = category
        return refreshResult
    }

    override suspend fun loadMoreMovies(category: MovieCategory, page: Int): Result<MoviePage> {
        lastLoadMore = category to page
        return loadMoreResult
    }

    override fun observeMovieDetail(id: MovieId): Flow<MovieDetail?> {
        lastDetailId = id
        return detail
    }

    override suspend fun refreshMovieDetail(id: MovieId): Result<Unit> {
        lastDetailId = id
        return detailResult
    }

    override suspend fun searchMovies(query: String, page: Int): Result<SearchResults> {
        lastSearch = query to page
        return searchResult(query, page)
    }
}
