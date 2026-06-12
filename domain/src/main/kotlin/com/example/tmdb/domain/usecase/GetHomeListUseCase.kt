package com.example.tmdb.domain.usecase

import com.example.tmdb.domain.model.HomeList
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.repository.MovieRepository

class GetHomeListUseCase(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(list: HomeList): Result<List<Movie>> =
        repository.homeList(list)
}
