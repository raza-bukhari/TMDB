package com.example.tmdb.domain.model

data class MediaVideo(
    val id: String,
    val name: String,
    val key: String,
    val site: String,
    val type: String,
    val official: Boolean,
) {
    val youtubeUrl: String?
        get() = if (site.equals("YouTube", ignoreCase = true) && key.isNotBlank()) {
            "https://www.youtube.com/watch?v=$key"
        } else {
            null
        }
}
