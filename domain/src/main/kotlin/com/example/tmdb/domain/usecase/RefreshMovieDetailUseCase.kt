package com.example.tmdb.domain.usecase

import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.repository.MovieRepository

class RefreshMovieDetailUseCase(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(id: MovieId): Result<Unit> = repository.refreshMovieDetail(id)
}
