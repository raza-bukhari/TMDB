package com.example.tmdb.domain.usecase

import androidx.paging.PagingData
import com.example.tmdb.domain.model.DiscoverMovieFilters
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

class SearchMoviesUseCase(
    private val repository: MovieRepository,
) {
    operator fun invoke(query: String): Flow<PagingData<Movie>> =
        repository.searchMovies(query)
}

class DiscoverMoviesUseCase(
    private val repository: MovieRepository,
) {
    operator fun invoke(filters: DiscoverMovieFilters): Flow<PagingData<Movie>> =
        repository.discoverMovies(filters)
}
