package com.example.tmdb.domain.usecase

import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.repository.MovieRepository

class GetTvSeasonUseCase(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(seriesId: MovieId, seasonNumber: Int) =
        repository.tvSeason(seriesId, seasonNumber)
}
