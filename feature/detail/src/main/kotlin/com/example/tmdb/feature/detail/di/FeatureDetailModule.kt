package com.example.tmdb.feature.detail.di

import com.example.tmdb.feature.detail.MovieDetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureDetailModule = module {
    viewModelOf(::MovieDetailViewModel)
}
