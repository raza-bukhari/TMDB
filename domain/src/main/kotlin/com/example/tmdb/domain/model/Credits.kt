package com.example.tmdb.domain.model

data class CastMember(
    val id: Long,
    val name: String,
    val character: String,
    val profilePath: String?,
    val order: Int,
)

data class CrewMember(
    val id: Long,
    val name: String,
    val job: String,
    val profilePath: String?,
)
