package com.example.tmdb.feature.person.di

import com.example.tmdb.feature.person.PersonViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featurePersonModule = module {
    viewModelOf(::PersonViewModel)
}
