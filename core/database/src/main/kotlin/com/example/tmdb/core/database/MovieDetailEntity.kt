package com.example.tmdb.core.database

import androidx.room.Entity

@Entity(tableName = "movie_details", primaryKeys = ["id", "mediaType"])
data class MovieDetailEntity(
    val id: Long,
    val mediaType: String = "MOVIE",
    val title: String,
    val overview: String,
    val tagline: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val runtimeMinutes: Int?,
    val numberOfSeasons: Int? = null,
    val numberOfEpisodes: Int? = null,
    val status: String? = null,
    val voteAverage: Double,
    val voteCount: Int,
    /** Pipe-joined genre names; a converter would be machinery for one read site. */
    val genres: String,
    val castJson: String? = null,
    val crewJson: String? = null,
    val similarMoviesJson: String? = null,
    val providersJson: String? = null,
    val seasonsJson: String? = null,
    val lastEpisodeJson: String? = null,
    val nextEpisodeJson: String? = null,
    val certification: String? = null,
    val imdbId: String? = null,
)
