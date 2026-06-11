package com.example.tmdb.core.common.di

import com.example.tmdb.core.common.DefaultDispatcherProvider
import com.example.tmdb.core.common.DispatcherProvider
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val commonModule = module {
    singleOf(::DefaultDispatcherProvider) bind DispatcherProvider::class
}
