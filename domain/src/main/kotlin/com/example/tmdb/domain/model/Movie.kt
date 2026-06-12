package com.example.tmdb.domain.model

import java.time.LocalDate

@JvmInline
value class MovieId(val value: Long)

enum class MediaType {
    MOVIE,
    TV,
}

data class Movie(
    val id: MovieId,
    val title: String,
    val overview: String,
    /** Raw TMDB poster path (e.g. `/abc.jpg`); URL assembly is a presentation concern. */
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: LocalDate?,
    val voteAverage: Double,
    val voteCount: Int,
    val genreIds: List<Int> = emptyList(),
    val mediaType: MediaType = MediaType.MOVIE,
)
