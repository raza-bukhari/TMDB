package com.example.tmdb.domain.di

import com.example.tmdb.domain.usecase.AddMovieToWatchlistUseCase
import com.example.tmdb.domain.usecase.DiscoverMoviesUseCase
import com.example.tmdb.domain.usecase.GetExternalRatingsUseCase
import com.example.tmdb.domain.usecase.GetHomeListUseCase
import com.example.tmdb.domain.usecase.GetMediaVideosUseCase
import com.example.tmdb.domain.usecase.GetPersonCreditsUseCase
import com.example.tmdb.domain.usecase.GetPersonDetailUseCase
import com.example.tmdb.domain.usecase.GetTvEpisodeUseCase
import com.example.tmdb.domain.usecase.GetTvSeasonUseCase
import com.example.tmdb.domain.usecase.ObserveMovieDetailUseCase
import com.example.tmdb.domain.usecase.ObserveMoviesUseCase
import com.example.tmdb.domain.usecase.ObserveWatchlistItemsUseCase
import com.example.tmdb.domain.usecase.ObserveWatchlistKeysUseCase
import com.example.tmdb.domain.usecase.ObserveWatchlistUseCase
import com.example.tmdb.domain.usecase.RefreshMovieDetailUseCase
import com.example.tmdb.domain.usecase.RemoveMovieFromWatchlistUseCase
import com.example.tmdb.domain.usecase.SearchMoviesUseCase
import com.example.tmdb.domain.usecase.UpdateUserActivityUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::GetExternalRatingsUseCase)
    factoryOf(::GetMediaVideosUseCase)
    factoryOf(::GetPersonCreditsUseCase)
    factoryOf(::GetPersonDetailUseCase)
    factoryOf(::GetTvEpisodeUseCase)
    factoryOf(::GetTvSeasonUseCase)
    factoryOf(::GetHomeListUseCase)
    factoryOf(::ObserveMoviesUseCase)
    factoryOf(::ObserveMovieDetailUseCase)
    factoryOf(::RefreshMovieDetailUseCase)
    factoryOf(::SearchMoviesUseCase)
    factoryOf(::DiscoverMoviesUseCase)
    factoryOf(::ObserveWatchlistUseCase)
    factoryOf(::ObserveWatchlistItemsUseCase)
    factoryOf(::ObserveWatchlistKeysUseCase)
    factoryOf(::AddMovieToWatchlistUseCase)
    factoryOf(::RemoveMovieFromWatchlistUseCase)
    factoryOf(::UpdateUserActivityUseCase)
}
