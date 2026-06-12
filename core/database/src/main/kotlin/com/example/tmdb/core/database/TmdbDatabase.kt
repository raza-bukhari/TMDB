package com.example.tmdb.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

// v4 added MovieRemoteKeysEntity for Paging 3.
@Database(
    entities = [MovieEntity::class, MovieDetailEntity::class, MovieRemoteKeysEntity::class],
    version = 5, // v5: MovieDetailEntity.imdbId for OMDb external ratings
    exportSchema = false
)
abstract class TmdbDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun movieDetailDao(): MovieDetailDao
}
