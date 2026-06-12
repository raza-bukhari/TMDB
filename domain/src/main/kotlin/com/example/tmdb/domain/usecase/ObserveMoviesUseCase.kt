package com.example.tmdb.domain.usecase

import androidx.paging.PagingData
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieCategory
import com.example.tmdb.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

class ObserveMoviesUseCase(
    private val repository: MovieRepository,
) {
    operator fun invoke(category: MovieCategory): Flow<PagingData<Movie>> =
        repository.observeMovies(category)
}
