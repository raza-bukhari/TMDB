package com.example.tmdb.domain.usecase

import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.UserMediaActivity
import com.example.tmdb.domain.repository.MovieRepository

class ObserveWatchlistUseCase(
    private val repository: MovieRepository,
) {
    operator fun invoke() = repository.observeWatchlist()
}

class ObserveWatchlistItemsUseCase(
    private val repository: MovieRepository,
) {
    operator fun invoke() = repository.observeWatchlistItems()
}

class ObserveWatchlistIdsUseCase(
    private val repository: MovieRepository,
) {
    operator fun invoke() = repository.observeWatchlistIds()
}

class AddMovieToWatchlistUseCase(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(movie: Movie) = repository.addToWatchlist(movie)

    suspend operator fun invoke(detail: MovieDetail) = repository.addToWatchlist(detail)
}

class RemoveMovieFromWatchlistUseCase(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(id: MovieId) = repository.removeFromWatchlist(id)
}

class UpdateUserActivityUseCase(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(activity: UserMediaActivity) = repository.updateUserActivity(activity)
}
