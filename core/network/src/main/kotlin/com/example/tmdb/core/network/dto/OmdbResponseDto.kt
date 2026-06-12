package com.example.tmdb.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OmdbRatingDto(
    @SerialName("Source") val source: String = "",
    @SerialName("Value") val value: String = "",
)

/** OMDb uses PascalCase keys and the literal string "N/A" for missing values. */
@Serializable
data class OmdbResponseDto(
    @SerialName("Response") val response: String = "False",
    @SerialName("imdbRating") val imdbRating: String? = null,
    @SerialName("Metascore") val metascore: String? = null,
    @SerialName("Ratings") val ratings: List<OmdbRatingDto> = emptyList(),
)
