package com.example.tmdb.domain.di

import com.example.tmdb.domain.usecase.LoadMoreMoviesUseCase
import com.example.tmdb.domain.usecase.ObserveMovieDetailUseCase
import com.example.tmdb.domain.usecase.ObserveMoviesUseCase
import com.example.tmdb.domain.usecase.RefreshMovieDetailUseCase
import com.example.tmdb.domain.usecase.RefreshMoviesUseCase
import com.example.tmdb.domain.usecase.SearchMoviesUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::ObserveMoviesUseCase)
    factoryOf(::RefreshMoviesUseCase)
    factoryOf(::LoadMoreMoviesUseCase)
    factoryOf(::ObserveMovieDetailUseCase)
    factoryOf(::RefreshMovieDetailUseCase)
    factoryOf(::SearchMoviesUseCase)
}
