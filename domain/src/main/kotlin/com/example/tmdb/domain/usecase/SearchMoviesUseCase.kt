package com.example.tmdb.domain.usecase

import com.example.tmdb.domain.model.SearchResults
import com.example.tmdb.domain.repository.MovieRepository

class SearchMoviesUseCase(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(query: String, page: Int = 1): Result<SearchResults> =
        repository.searchMovies(query, page)
}
