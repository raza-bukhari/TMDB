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
)
