package com.example.tmdb.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

// v2 added MovieEntity.genreIds. The cache only mirrors the network, so destructive
// migration (configured on the builder) is acceptable — see DatabaseModule.
@Database(entities = [MovieEntity::class, MovieDetailEntity::class], version = 2, exportSchema = false)
abstract class TmdbDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun movieDetailDao(): MovieDetailDao
}
