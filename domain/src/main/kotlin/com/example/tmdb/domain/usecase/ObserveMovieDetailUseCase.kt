package com.example.tmdb.domain.usecase

import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.MediaType
import com.example.tmdb.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

class ObserveMovieDetailUseCase(
    private val repository: MovieRepository,
) {
    operator fun invoke(id: MovieId, mediaType: MediaType = MediaType.MOVIE): Flow<MovieDetail?> =
        repository.observeMovieDetail(id, mediaType)
}
