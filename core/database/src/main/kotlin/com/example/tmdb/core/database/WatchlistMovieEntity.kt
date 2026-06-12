package com.example.tmdb.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist_movies")
data class WatchlistMovieEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val addedAtMillis: Long,
    val mediaType: String = "MOVIE",
    val status: String = "PLAN_TO_WATCH",
    val favorite: Boolean = false,
    val userRating: Double? = null,
    val watchedDate: String? = null,
    val notes: String = "",
)
