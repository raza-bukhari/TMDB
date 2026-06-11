package com.example.tmdb.core.database

import androidx.room.Entity

/**
 * Cached movie row. A movie can appear in several categories, so the key is
 * (id, category); [orderIndex] preserves TMDB's ranking within a category.
 */
@Entity(tableName = "movies", primaryKeys = ["id", "category"])
data class MovieEntity(
    val id: Long,
    val category: String,
    val orderIndex: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val voteCount: Int,
    /** Comma-joined TMDB genre ids; a converter would be machinery for one read site. */
    val genreIds: String = "",
)

object MovieCategories {
    const val POPULAR = "popular"
    const val TOP_RATED = "top_rated"
    const val NOW_PLAYING = "now_playing"
}
