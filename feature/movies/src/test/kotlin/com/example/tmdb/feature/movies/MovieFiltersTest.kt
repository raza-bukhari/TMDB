package com.example.tmdb.feature.movies

import com.example.tmdb.domain.model.MediaType
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
}
