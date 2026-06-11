package com.example.tmdb.feature.movies.di

import com.example.tmdb.feature.movies.MoviesViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureMoviesModule = module {
    viewModelOf(::MoviesViewModel)
}
