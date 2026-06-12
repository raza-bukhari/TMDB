package com.example.tmdb.core.database

import androidx.paging.PagingSource
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

    @Query("SELECT * FROM movies WHERE category = :category ORDER BY orderIndex ASC")
    fun getPagingSource(category: String): PagingSource<Int, MovieEntity>

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

    // Remote Keys
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemoteKeys(keys: List<MovieRemoteKeysEntity>)

    @Query("SELECT * FROM movie_remote_keys WHERE id = :id AND category = :category")
    suspend fun getRemoteKey(id: Long, category: String): MovieRemoteKeysEntity?

    @Query("DELETE FROM movie_remote_keys WHERE category = :category")
    suspend fun deleteRemoteKeysByCategory(category: String)

    @Transaction
    suspend fun clearAndInsert(category: String, movies: List<MovieEntity>, keys: List<MovieRemoteKeysEntity>) {
        deleteByCategory(category)
        deleteRemoteKeysByCategory(category)
        insertAll(movies)
        insertRemoteKeys(keys)
    }
}
