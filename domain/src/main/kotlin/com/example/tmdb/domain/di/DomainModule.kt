package com.example.tmdb.domain.di

import com.example.tmdb.domain.usecase.ObserveMovieDetailUseCase
import com.example.tmdb.domain.usecase.ObservePopularMoviesUseCase
import com.example.tmdb.domain.usecase.RefreshMovieDetailUseCase
import com.example.tmdb.domain.usecase.RefreshPopularMoviesUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::ObservePopularMoviesUseCase)
    factoryOf(::RefreshPopularMoviesUseCase)
    factoryOf(::ObserveMovieDetailUseCase)
    factoryOf(::RefreshMovieDetailUseCase)
}
