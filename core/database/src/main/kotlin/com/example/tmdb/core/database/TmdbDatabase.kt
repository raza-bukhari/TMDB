package com.example.tmdb.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

// v10 keys cached details and watchlist rows by media type to avoid movie/series TMDB id collisions.
@Database(
    entities = [MovieEntity::class, MovieDetailEntity::class, MovieRemoteKeysEntity::class, WatchlistMovieEntity::class],
    version = 10,
    exportSchema = false
)
abstract class TmdbDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun movieDetailDao(): MovieDetailDao
    abstract fun watchlistDao(): WatchlistDao
}
