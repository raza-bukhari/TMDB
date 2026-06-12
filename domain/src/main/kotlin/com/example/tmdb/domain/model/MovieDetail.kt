package com.example.tmdb.domain.model

import java.time.LocalDate

data class MovieDetail(
    val id: MovieId,
    val title: String,
    val overview: String,
    val tagline: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: LocalDate?,
    val runtimeMinutes: Int?,
    val voteAverage: Double,
    val voteCount: Int,
    val genres: List<String>,
    val cast: List<CastMember> = emptyList(),
    val directors: List<String> = emptyList(),
    val producers: List<String> = emptyList(),
    val certification: String? = null,
    val similarMovies: List<Movie> = emptyList(),
    val watchProviders: List<WatchProvider> = emptyList(),
    /** IMDb identifier (tt…), the join key for OMDb external ratings. */
    val imdbId: String? = null,
)
