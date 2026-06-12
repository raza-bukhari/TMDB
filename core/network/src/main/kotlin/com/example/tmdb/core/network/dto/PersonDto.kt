package com.example.tmdb.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PersonDto(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String = "",
    @SerialName("biography") val biography: String = "",
    @SerialName("profile_path") val profilePath: String? = null,
    @SerialName("birthday") val birthday: String? = null,
    @SerialName("deathday") val deathday: String? = null,
    @SerialName("place_of_birth") val placeOfBirth: String? = null,
    @SerialName("known_for_department") val knownForDepartment: String? = null,
    @SerialName("popularity") val popularity: Double = 0.0,
)

@Serializable
data class PersonCombinedCreditsDto(
    @SerialName("cast") val cast: List<MovieDto> = emptyList(),
    @SerialName("crew") val crew: List<MovieDto> = emptyList(),
)
