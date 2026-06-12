package com.example.tmdb.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WatchProviderDto(
    @SerialName("provider_id") val providerId: Int,
    @SerialName("provider_name") val providerName: String,
    @SerialName("logo_path") val logoPath: String? = null,
    @SerialName("display_priority") val displayPriority: Int = 0,
)

@Serializable
data class WatchProvidersResultDto(
    @SerialName("link") val link: String? = null,
    @SerialName("flatrate") val flatrate: List<WatchProviderDto> = emptyList(),
    @SerialName("rent") val rent: List<WatchProviderDto> = emptyList(),
    @SerialName("buy") val buy: List<WatchProviderDto> = emptyList(),
)

@Serializable
data class WatchProvidersResponseDto(
    @SerialName("results") val results: Map<String, WatchProvidersResultDto> = emptyMap(),
)
