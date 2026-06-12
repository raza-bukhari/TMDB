package com.example.tmdb.domain.di

import com.example.tmdb.domain.usecase.AddMovieToWatchlistUseCase
import com.example.tmdb.domain.usecase.DiscoverMoviesUseCase
import com.example.tmdb.domain.usecase.GetExternalRatingsUseCase
import com.example.tmdb.domain.usecase.GetHomeListUseCase
import com.example.tmdb.domain.usecase.ObserveMovieDetailUseCase
import com.example.tmdb.domain.usecase.ObserveMoviesUseCase
import com.example.tmdb.domain.usecase.ObserveWatchlistIdsUseCase
import com.example.tmdb.domain.usecase.ObserveWatchlistUseCase
import com.example.tmdb.domain.usecase.RefreshMovieDetailUseCase
import com.example.tmdb.domain.usecase.RemoveMovieFromWatchlistUseCase
import com.example.tmdb.domain.usecase.SearchMoviesUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::GetExternalRatingsUseCase)
    factoryOf(::GetHomeListUseCase)
    factoryOf(::ObserveMoviesUseCase)
    factoryOf(::ObserveMovieDetailUseCase)
    factoryOf(::RefreshMovieDetailUseCase)
    factoryOf(::SearchMoviesUseCase)
    factoryOf(::DiscoverMoviesUseCase)
    factoryOf(::ObserveWatchlistUseCase)
    factoryOf(::ObserveWatchlistIdsUseCase)
    factoryOf(::AddMovieToWatchlistUseCase)
    factoryOf(::RemoveMovieFromWatchlistUseCase)
}
