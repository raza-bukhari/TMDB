package com.example.tmdb.domain.model

data class WatchProviderRegion(
    val region: String,
    val link: String?,
    val flatrate: List<WatchProvider> = emptyList(),
    val rent: List<WatchProvider> = emptyList(),
    val buy: List<WatchProvider> = emptyList(),
) {
    val displayProviders: List<WatchProvider>
        get() = (flatrate + rent + buy).distinctBy { it.id }
}
