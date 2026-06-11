package com.example.tmdb.domain.usecase

import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieCategory
import com.example.tmdb.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

class ObserveMoviesUseCase(
    private val repository: MovieRepository,
) {
    operator fun invoke(category: MovieCategory): Flow<List<Movie>> =
        repository.observeMovies(category)
}
