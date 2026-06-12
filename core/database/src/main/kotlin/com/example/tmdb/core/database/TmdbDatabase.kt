package com.example.tmdb.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

// v4 added MovieRemoteKeysEntity for Paging 3.
@Database(
    entities = [MovieEntity::class, MovieDetailEntity::class, MovieRemoteKeysEntity::class, WatchlistMovieEntity::class],
    version = 6, // v6: local watchlist
    exportSchema = false
)
abstract class TmdbDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun movieDetailDao(): MovieDetailDao
    abstract fun watchlistDao(): WatchlistDao
}
