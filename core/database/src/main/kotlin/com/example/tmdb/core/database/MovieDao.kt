package com.example.tmdb.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Query("SELECT * FROM movies WHERE category = :category ORDER BY orderIndex ASC")
    fun observeByCategory(category: String): Flow<List<MovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movies: List<MovieEntity>)

    @Query("DELETE FROM movies WHERE category = :category")
    suspend fun deleteByCategory(category: String)

    /** Refresh = replace the category atomically so observers never see a half-written list. */
    @Transaction
    suspend fun replaceCategory(category: String, movies: List<MovieEntity>) {
        deleteByCategory(category)
        insertAll(movies)
    }
}
