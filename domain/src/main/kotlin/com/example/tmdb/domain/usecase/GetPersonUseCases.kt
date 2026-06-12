package com.example.tmdb.domain.usecase

import com.example.tmdb.domain.repository.MovieRepository

class GetPersonDetailUseCase(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(personId: Long) = repository.personDetail(personId)
}

class GetPersonCreditsUseCase(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(personId: Long) = repository.personCredits(personId)
}
