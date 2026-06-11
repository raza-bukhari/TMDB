package com.example.tmdb.data.di

import com.example.tmdb.data.repository.OfflineFirstMovieRepository
import com.example.tmdb.domain.repository.MovieRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    singleOf(::OfflineFirstMovieRepository) bind MovieRepository::class
}
