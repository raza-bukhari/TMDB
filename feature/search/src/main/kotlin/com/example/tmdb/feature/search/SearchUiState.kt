package com.example.tmdb.feature.search

import androidx.compose.runtime.Immutable
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.Movie
import kotlinx.collections.immutable.ImmutableList

data class SearchUiState(
    val query: String = "",
    val content: SearchContent = SearchContent.Idle,
)

sealed interface SearchContent {
    /** Blank query — show the search prompt. */
    data object Idle : SearchContent
    data object Loading : SearchContent
    data object NoResults : SearchContent
    data class Error(val error: AppError) : SearchContent
    data class Results(
        val movies: ImmutableList<SearchResultItem>,
        val isAppending: Boolean,
        val canLoadMore: Boolean,
    ) : SearchContent
}

@Immutable
data class SearchResultItem(
    val id: Long,
    val title: String,
    val posterUrl: String?,
    val rating: Double,
)

private const val POSTER_BASE = "https://image.tmdb.org/t/p/w342"

internal fun Movie.toSearchItem(): SearchResultItem = SearchResultItem(
    id = id.value,
    title = title,
    posterUrl = posterPath?.let { POSTER_BASE + it },
    rating = voteAverage,
)
