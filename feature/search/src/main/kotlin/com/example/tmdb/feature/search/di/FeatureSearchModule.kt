package com.example.tmdb.feature.search.di

import com.example.tmdb.feature.search.SearchViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureSearchModule = module {
    viewModelOf(::SearchViewModel)
}
