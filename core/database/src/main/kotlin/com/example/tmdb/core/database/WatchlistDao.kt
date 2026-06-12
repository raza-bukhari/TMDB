package com.example.tmdb.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist_movies ORDER BY addedAtMillis DESC")
    fun observeWatchlist(): Flow<List<WatchlistMovieEntity>>

    @Query("SELECT id FROM watchlist_movies")
    fun observeWatchlistIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(movie: WatchlistMovieEntity)

    @Query(
        """
        UPDATE watchlist_movies
        SET status = :status,
            favorite = :favorite,
            userRating = :userRating,
            watchedDate = :watchedDate,
            notes = :notes
        WHERE id = :id
        """,
    )
    suspend fun updateActivity(
        id: Long,
        status: String,
        favorite: Boolean,
        userRating: Double?,
        watchedDate: String?,
        notes: String,
    )

    @Query("DELETE FROM watchlist_movies WHERE id = :id")
    suspend fun delete(id: Long)
}
