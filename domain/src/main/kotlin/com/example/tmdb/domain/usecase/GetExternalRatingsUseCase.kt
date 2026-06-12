package com.example.tmdb.domain.usecase

import com.example.tmdb.domain.model.ExternalRatings
import com.example.tmdb.domain.repository.MovieRepository

class GetExternalRatingsUseCase(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(imdbId: String): Result<ExternalRatings> =
        repository.externalRatings(imdbId)
}
