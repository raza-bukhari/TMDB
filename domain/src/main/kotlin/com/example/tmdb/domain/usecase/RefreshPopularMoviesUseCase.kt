package com.example.tmdb.domain.usecase

import com.example.tmdb.domain.repository.MovieRepository

class RefreshPopularMoviesUseCase(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(): Result<Unit> = repository.refreshPopularMovies()
}
