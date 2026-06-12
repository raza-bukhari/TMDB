package com.example.tmdb.domain.usecase

import com.example.tmdb.domain.model.MediaType
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.repository.MovieRepository

class GetWatchProvidersUseCase(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(id: MovieId, mediaType: MediaType, region: String = "US") =
        repository.watchProviders(id, mediaType, region)
}
