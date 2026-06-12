package com.example.tmdb.domain.model

import java.time.LocalDate

data class TvSeason(
    val id: Long,
    val name: String,
    val overview: String,
    val posterPath: String?,
    val airDate: LocalDate?,
    val seasonNumber: Int,
    val episodeCount: Int,
    val voteAverage: Double,
    val episodes: List<TvEpisode> = emptyList(),
)

data class TvEpisode(
    val id: Long,
    val name: String,
    val overview: String,
    val stillPath: String?,
    val airDate: LocalDate?,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val runtimeMinutes: Int?,
    val voteAverage: Double,
    val voteCount: Int,
)
