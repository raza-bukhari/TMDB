package com.example.tmdb.core.database

import androidx.room.Entity

@Entity(tableName = "watchlist_movies", primaryKeys = ["id", "mediaType"])
data class WatchlistMovieEntity(
    val id: Long,
    val mediaType: String = "MOVIE",
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val addedAtMillis: Long,
    val status: String = "PLAN_TO_WATCH",
    val favorite: Boolean = false,
    val userRating: Double? = null,
    val watchedDate: String? = null,
    val notes: String = "",
)
