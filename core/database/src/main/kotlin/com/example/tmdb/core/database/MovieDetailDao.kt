package com.example.tmdb.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDetailDao {

    @Query("SELECT * FROM movie_details WHERE id = :id AND mediaType = :mediaType")
    fun observeById(id: Long, mediaType: String): Flow<MovieDetailEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(detail: MovieDetailEntity)
}
