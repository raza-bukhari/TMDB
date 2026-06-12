package com.example.tmdb

import com.example.tmdb.core.common.di.commonModule
import com.example.tmdb.core.database.di.databaseModule
import com.example.tmdb.core.network.di.networkModule
import com.example.tmdb.data.di.dataModule
import com.example.tmdb.domain.di.domainModule
import com.example.tmdb.feature.detail.di.featureDetailModule
import com.example.tmdb.feature.movies.di.featureMoviesModule
import com.example.tmdb.feature.person.di.featurePersonModule
import com.example.tmdb.feature.search.di.featureSearchModule
import org.koin.core.module.Module

/** One Koin module per Gradle module, aggregated here and only here. */
val appModules: List<Module> = listOf(
    commonModule,
    networkModule,
    databaseModule,
    domainModule,
    dataModule,
    featureMoviesModule,
    featureDetailModule,
    featurePersonModule,
    featureSearchModule,
)
