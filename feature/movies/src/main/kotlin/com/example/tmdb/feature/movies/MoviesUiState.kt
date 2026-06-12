package com.example.tmdb.feature.movies

import androidx.compose.runtime.Immutable
import com.example.tmdb.domain.model.HomeList
import com.example.tmdb.domain.model.Movie
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

data class MoviesUiState(
    val trendingWindow: TrendingWindow = TrendingWindow.TODAY,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val hero: MovieListItem? = null,
    val sections: ImmutableList<HomeSectionUi> = persistentListOf(),
)

enum class TrendingWindow {
    TODAY,
    THIS_WEEK,
}

@Immutable
data class HomeSectionUi(
    val list: HomeList,
    val title: String,
    val subtitle: String,
    val movies: ImmutableList<MovieListItem>,
)

/** UI projection of [Movie]; image paths are already resolved to loadable URLs. */
@Immutable
data class MovieListItem(
    val id: Long,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val rating: Double,
    val voteCount: Int,
    val releaseYear: String?,
)

private const val POSTER_BASE = "https://image.tmdb.org/t/p/w342"
private const val BACKDROP_BASE = "https://image.tmdb.org/t/p/w780"

internal fun Movie.toListItem(): MovieListItem = MovieListItem(
    id = id.value,
    title = title,
    overview = overview,
    posterUrl = posterPath?.let { POSTER_BASE + it },
    backdropUrl = backdropPath?.let { BACKDROP_BASE + it },
    rating = voteAverage,
    voteCount = voteCount,
    releaseYear = releaseDate?.year?.toString(),
)

internal fun List<Movie>.toMovieListItems(): ImmutableList<MovieListItem> =
    map { it.toListItem() }.toImmutableList()

internal fun HomeList.title(trendingWindow: TrendingWindow): String = when (this) {
    HomeList.TRENDING_TODAY,
    HomeList.TRENDING_THIS_WEEK -> "Trending"
    HomeList.POPULAR -> "What's Popular"
    HomeList.NOW_PLAYING -> "In Theaters"
    HomeList.TOP_RATED -> "Top Rated"
    HomeList.UPCOMING -> "Coming Soon"
}

internal fun HomeList.subtitle(trendingWindow: TrendingWindow): String = when (this) {
    HomeList.TRENDING_TODAY,
    HomeList.TRENDING_THIS_WEEK -> when (trendingWindow) {
        TrendingWindow.TODAY -> "Movies people are watching today"
        TrendingWindow.THIS_WEEK -> "Movies people talked about this week"
    }
    HomeList.POPULAR -> "High-traffic picks from TMDB"
    HomeList.NOW_PLAYING -> "Current theatrical releases"
    HomeList.TOP_RATED -> "Audience favorites with strong vote averages"
    HomeList.UPCOMING -> "Upcoming releases to keep on your radar"
}

internal fun TrendingWindow.toHomeList(): HomeList = when (this) {
    TrendingWindow.TODAY -> HomeList.TRENDING_TODAY
    TrendingWindow.THIS_WEEK -> HomeList.TRENDING_THIS_WEEK
}
