package com.example.tmdb.domain.model

import java.time.LocalDate

enum class WatchlistStatus {
    PLAN_TO_WATCH,
    WATCHING,
    COMPLETED,
}

data class UserMediaActivity(
    val mediaId: MovieId,
    val mediaType: MediaType = MediaType.MOVIE,
    val status: WatchlistStatus = WatchlistStatus.PLAN_TO_WATCH,
    val favorite: Boolean = false,
    val userRating: Double? = null,
    val watchedDate: LocalDate? = null,
    val notes: String = "",
)

data class WatchlistItem(
    val movie: Movie,
    val status: WatchlistStatus,
    val favorite: Boolean,
    val userRating: Double?,
    val watchedDate: LocalDate?,
    val notes: String,
    val addedAtMillis: Long,
)
