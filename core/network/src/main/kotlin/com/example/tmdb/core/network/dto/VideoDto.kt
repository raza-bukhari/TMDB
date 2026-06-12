package com.example.tmdb.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String = "",
    @SerialName("key") val key: String = "",
    @SerialName("site") val site: String = "",
    @SerialName("type") val type: String = "",
    @SerialName("official") val official: Boolean = false,
)

@Serializable
data class VideosResponseDto(
    @SerialName("id") val id: Long = 0,
    @SerialName("results") val results: List<VideoDto> = emptyList(),
)
