package com.example.tmdb.feature.movies

import com.example.tmdb.domain.model.MediaType
import com.example.tmdb.domain.model.WatchlistStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MovieFiltersTest {

    @Test
    fun `given series filter, when matching search result, then only tv items match`() {
        val movie = item(id = 1, mediaType = MediaType.MOVIE)
        val series = item(id = 2, mediaType = MediaType.TV)
        val filters = MovieFilters(mediaType = MediaType.TV)

        assertFalse(movie.matches(query = "", filters = filters))
        assertTrue(series.matches(query = "", filters = filters))
    }

    @Test
    fun `given local activity, when favorite genres calculated, then favorites and high ratings are weighted`() {
        val items = listOf(
            watchlistItem(
                movie = item(id = 1, mediaType = MediaType.MOVIE, genreIds = listOf(18, 53)),
                favorite = true,
                userRating = null,
            ),
            watchlistItem(
                movie = item(id = 2, mediaType = MediaType.TV, genreIds = listOf(878, 18)),
                favorite = false,
                userRating = 9.0,
            ),
            watchlistItem(
                movie = item(id = 3, mediaType = MediaType.MOVIE, genreIds = listOf(35)),
                favorite = false,
                userRating = null,
                status = WatchlistStatus.COMPLETED,
            ),
        )

        assertEquals(listOf("Drama", "Thriller", "Sci-Fi"), items.favoriteGenreNames(limit = 3))
    }

    private fun item(id: Long, mediaType: MediaType): MovieListItem = MovieListItem(
        id = id,
        title = "Title $id",
        overview = "Overview",
        posterUrl = null,
        backdropUrl = null,
        rating = 8.0,
        voteCount = 10,
        releaseYear = "2024",
        genreIds = emptyList(),
        mediaType = mediaType,
    )

    private fun item(id: Long, mediaType: MediaType, genreIds: List<Int>): MovieListItem =
        item(id = id, mediaType = mediaType).copy(genreIds = genreIds)

    private fun watchlistItem(
        movie: MovieListItem,
        favorite: Boolean,
        userRating: Double?,
        status: WatchlistStatus = WatchlistStatus.PLAN_TO_WATCH,
    ): WatchlistItemUi = WatchlistItemUi(
        movie = movie,
        status = status,
        favorite = favorite,
        userRating = userRating,
        watchedDate = null,
        notes = "",
        addedAtMillis = 0L,
    )
}
