package com.example.tmdb.domain

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

/** Minimal programmable [MovieRepository] for domain use-case tests. */
class FakeRepo : MovieRepository {
    val movies = MutableStateFlow<List<Movie>>(emptyList())
    val detail = MutableStateFlow<MovieDetail?>(null)
    val watchlist = MutableStateFlow<List<Movie>>(emptyList())

    var detailResult: Result<Unit> = Result.success(Unit)
    var homeResult: Result<List<Movie>> = Result.success(emptyList())
    var externalRatingsResult: Result<ExternalRatings> = Result.success(ExternalRatings())
    var onSearch: (String) -> List<Movie> = { emptyList() }

    var lastCategory: MovieCategory? = null
    var lastDetailId: MovieId? = null
    var lastSearch: String? = null
    var lastHomeList: HomeList? = null
    var lastImdbId: String? = null

    override fun observeMovies(category: MovieCategory): Flow<PagingData<Movie>> {
        lastCategory = category
        return movies.map { PagingData.from(it) }
    }

    override fun observeMovieDetail(id: MovieId): Flow<MovieDetail?> {
        lastDetailId = id
        return detail
    }

    override suspend fun refreshMovieDetail(id: MovieId, mediaType: MediaType): Result<Unit> {
        lastDetailId = id
        return detailResult
    }

    override fun searchMovies(query: String): Flow<PagingData<Movie>> {
        lastSearch = query
        return flowOf(PagingData.from(onSearch(query)))
    }

    override fun discoverMovies(filters: DiscoverMovieFilters): Flow<PagingData<Movie>> =
        flowOf(PagingData.from(emptyList()))

    override suspend fun homeList(list: HomeList): Result<List<Movie>> {
        lastHomeList = list
        return homeResult
    }

    override suspend fun externalRatings(imdbId: String): Result<ExternalRatings> {
        lastImdbId = imdbId
        return externalRatingsResult
    }

    override fun observeWatchlist(): Flow<List<Movie>> = watchlist

    override fun observeWatchlistIds(): Flow<Set<MovieId>> =
        watchlist.map { movies -> movies.map { it.id }.toSet() }

    override suspend fun addToWatchlist(movie: Movie): Result<Unit> {
        watchlist.value = (listOf(movie) + watchlist.value.filterNot { it.id == movie.id })
        return Result.success(Unit)
    }

    override suspend fun addToWatchlist(detail: MovieDetail): Result<Unit> {
        return addToWatchlist(
            Movie(
                id = detail.id,
                title = detail.title,
                overview = detail.overview,
                posterPath = detail.posterPath,
                backdropPath = detail.backdropPath,
                releaseDate = detail.releaseDate,
                voteAverage = detail.voteAverage,
                voteCount = detail.voteCount,
            ),
        )
    }

    override suspend fun removeFromWatchlist(id: MovieId): Result<Unit> {
        watchlist.value = watchlist.value.filterNot { it.id == id }
        return Result.success(Unit)
    }
}
