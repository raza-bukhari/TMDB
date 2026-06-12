package com.example.tmdb.domain.model

import java.time.LocalDate

data class Person(
    val id: Long,
    val name: String,
    val biography: String,
    val profilePath: String?,
    val birthday: LocalDate?,
    val deathday: LocalDate?,
    val placeOfBirth: String?,
    val knownForDepartment: String?,
    val popularity: Double,
)

data class PersonCredits(
    val cast: List<Movie>,
    val crew: List<Movie>,
) {
    val combined: List<Movie> =
        (cast + crew).distinctBy { "${it.mediaType}:${it.id.value}" }.sortedByDescending { it.voteCount }
}
