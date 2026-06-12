package com.example.tmdb.feature.movies

import androidx.compose.runtime.Immutable
import com.example.tmdb.domain.model.HomeList
import com.example.tmdb.domain.model.MediaKey
import com.example.tmdb.domain.model.MediaType
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieGenre
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.WatchlistItem
import com.example.tmdb.domain.model.WatchlistStatus
import java.time.LocalDate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

data class MoviesUiState(
    val selectedTab: MoviesTab = MoviesTab.HOME,
    val trendingWindow: TrendingWindow = TrendingWindow.TODAY,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val hero: MovieListItem? = null,
    val sections: ImmutableList<HomeSectionUi> = persistentListOf(),
    val watchlistMovies: ImmutableList<MovieListItem> = persistentListOf(),
    val watchlistItems: ImmutableList<WatchlistItemUi> = persistentListOf(),
    val selectedWatchlistFilter: WatchlistFilter = WatchlistFilter.ALL,
    val selectedWatchlistSort: WatchlistSort = WatchlistSort.RECENTLY_ADDED,
    val watchlistKeys: Set<MediaKey> = emptySet(),
    val discoverQuery: String = "",
    val discoverFilters: MovieFilters = MovieFilters(),
)

enum class MoviesTab {
    HOME,
    DISCOVER,
    WATCHLIST,
    PROFILE,
}

enum class WatchlistFilter(val label: String) {
    ALL("All"),
    MOVIES("Movies"),
    SERIES("Series"),
    FAVORITES("Favorites"),
    WATCHING("Watching"),
    COMPLETED("Completed"),
    PLAN_TO_WATCH("Plan to watch"),
}

enum class WatchlistSort(val label: String) {
    RECENTLY_ADDED("Recent"),
    RATING("Rating"),
    YEAR("Year"),
    TITLE("Title"),
}

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
    val genreIds: List<Int> = emptyList(),
    val mediaType: MediaType = MediaType.MOVIE,
)

internal val MovieListItem.mediaKey: MediaKey
    get() = MediaKey(MovieId(id), mediaType)

@Immutable
data class WatchlistItemUi(
    val movie: MovieListItem,
    val status: WatchlistStatus,
    val favorite: Boolean,
    val userRating: Double?,
    val watchedDate: String?,
    val notes: String,
    val addedAtMillis: Long,
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
    genreIds = genreIds,
    mediaType = mediaType,
)

internal fun List<Movie>.toMovieListItems(): ImmutableList<MovieListItem> =
    map { it.toListItem() }.toImmutableList()

internal fun List<WatchlistItem>.toWatchlistItemUi(): ImmutableList<WatchlistItemUi> =
    map { it.toUi() }.toImmutableList()

internal fun WatchlistItem.toUi(): WatchlistItemUi = WatchlistItemUi(
    movie = movie.toListItem(),
    status = status,
    favorite = favorite,
    userRating = userRating,
    watchedDate = watchedDate?.toString(),
    notes = notes,
    addedAtMillis = addedAtMillis,
)

internal fun List<WatchlistItemUi>.filteredBy(filter: WatchlistFilter): List<WatchlistItemUi> = when (filter) {
    WatchlistFilter.ALL -> this
    WatchlistFilter.MOVIES -> filter { it.movie.mediaType == MediaType.MOVIE }
    WatchlistFilter.SERIES -> filter { it.movie.mediaType == MediaType.TV }
    WatchlistFilter.FAVORITES -> filter { it.favorite }
    WatchlistFilter.WATCHING -> filter { it.status == WatchlistStatus.WATCHING }
    WatchlistFilter.COMPLETED -> filter { it.status == WatchlistStatus.COMPLETED }
    WatchlistFilter.PLAN_TO_WATCH -> filter { it.status == WatchlistStatus.PLAN_TO_WATCH }
}

internal fun List<WatchlistItemUi>.sortedBy(sort: WatchlistSort): List<WatchlistItemUi> = when (sort) {
    WatchlistSort.RECENTLY_ADDED -> sortedByDescending { it.addedAtMillis }
    WatchlistSort.RATING -> sortedWith(compareByDescending<WatchlistItemUi> { it.userRating ?: it.movie.rating }.thenBy { it.movie.title })
    WatchlistSort.YEAR -> sortedWith(compareByDescending<WatchlistItemUi> { it.movie.releaseYear?.toIntOrNull() ?: 0 }.thenBy { it.movie.title })
    WatchlistSort.TITLE -> sortedBy { it.movie.title.lowercase() }
}

internal fun List<WatchlistItemUi>.favoriteGenreNames(limit: Int = 5): List<String> {
    val weightedGenreCounts = flatMap { item ->
        val weight = when {
            item.favorite -> 4
            (item.userRating ?: 0.0) >= 8.0 -> 3
            item.status == WatchlistStatus.COMPLETED -> 2
            else -> 1
        }
        item.movie.genreIds.map { genreId -> genreId to weight }
    }
        .groupingBy { it.first }
        .fold(0) { total, (_, weight) -> total + weight }

    return weightedGenreCounts.entries
        .sortedWith(compareByDescending<Map.Entry<Int, Int>> { it.value }.thenBy { it.key })
        .mapNotNull { MovieGenre.fromId(it.key)?.displayName }
        .take(limit)
}

internal fun MovieListItem.toDomainMovie(): Movie = Movie(
    id = MovieId(id),
    title = title,
    overview = overview,
    posterPath = posterUrl?.substringAfter(POSTER_BASE, missingDelimiterValue = posterUrl),
    backdropPath = backdropUrl?.substringAfter(BACKDROP_BASE, missingDelimiterValue = backdropUrl),
    releaseDate = releaseYear?.toIntOrNull()?.let { LocalDate.of(it, 1, 1) },
    voteAverage = rating,
    voteCount = voteCount,
    genreIds = genreIds,
    mediaType = mediaType,
)

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
