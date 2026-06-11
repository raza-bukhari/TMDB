package com.example.tmdb.core.database.di

import androidx.room.Room
import com.example.tmdb.core.database.MovieDao
import com.example.tmdb.core.database.MovieDetailDao
import com.example.tmdb.core.database.TmdbDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single<TmdbDatabase> {
        Room.databaseBuilder(androidContext(), TmdbDatabase::class.java, "tmdb.db")
            // Cache is a disposable mirror of TMDB; rebuild it rather than write migrations.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
    single<MovieDao> { get<TmdbDatabase>().movieDao() }
    single<MovieDetailDao> { get<TmdbDatabase>().movieDetailDao() }
}
