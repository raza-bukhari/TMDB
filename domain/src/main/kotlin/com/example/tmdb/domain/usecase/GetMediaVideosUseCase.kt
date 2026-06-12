package com.example.tmdb.domain.usecase

import com.example.tmdb.domain.model.MediaType
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.repository.MovieRepository

class GetMediaVideosUseCase(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(id: MovieId, mediaType: MediaType) = repository.videos(id, mediaType)
}
