package com.example.tmdb.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie_details")
data class MovieDetailEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val overview: String,
    val tagline: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val runtimeMinutes: Int?,
    val voteAverage: Double,
    val voteCount: Int,
    /** Pipe-joined genre names; a converter would be machinery for one read site. */
    val genres: String,
    val castJson: String? = null,
    val crewJson: String? = null,
    val similarMoviesJson: String? = null,
    val providersJson: String? = null,
    val certification: String? = null,
    val imdbId: String? = null,
)
