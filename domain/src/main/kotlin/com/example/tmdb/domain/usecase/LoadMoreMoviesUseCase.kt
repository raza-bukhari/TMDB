package com.example.tmdb.domain.usecase

import com.example.tmdb.domain.model.MovieCategory
import com.example.tmdb.domain.model.MoviePage
import com.example.tmdb.domain.repository.MovieRepository

class LoadMoreMoviesUseCase(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(category: MovieCategory, page: Int): Result<MoviePage> =
        repository.loadMoreMovies(category, page)
}
