package com.example.tmdb.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CastMemberDto(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("character") val character: String = "",
    @SerialName("profile_path") val profilePath: String? = null,
    @SerialName("order") val order: Int,
)

@Serializable
data class CrewMemberDto(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("job") val job: String,
    @SerialName("profile_path") val profilePath: String? = null,
)

@Serializable
data class CreditsDto(
    @SerialName("cast") val cast: List<CastMemberDto> = emptyList(),
    @SerialName("crew") val crew: List<CrewMemberDto> = emptyList(),
)
