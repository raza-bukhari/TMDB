package com.example.tmdb.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CertificationDto(
    @SerialName("certification") val certification: String = "",
    @SerialName("iso_639_1") val iso6391: String = "",
    @SerialName("release_date") val releaseDate: String = "",
    @SerialName("type") val type: Int = 0,
)

@Serializable
data class ReleaseDateResultDto(
    @SerialName("iso_3166_1") val iso31661: String,
    @SerialName("release_dates") val releaseDates: List<CertificationDto> = emptyList(),
)

@Serializable
data class ReleaseDatesResponseDto(
    @SerialName("results") val results: List<ReleaseDateResultDto> = emptyList(),
)
