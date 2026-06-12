package com.example.tmdb.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

// v7 added TV season and episode detail fields.
@Database(
    entities = [MovieEntity::class, MovieDetailEntity::class, MovieRemoteKeysEntity::class, WatchlistMovieEntity::class],
    version = 7,
    exportSchema = false
)
abstract class TmdbDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun movieDetailDao(): MovieDetailDao
    abstract fun watchlistDao(): WatchlistDao
}
