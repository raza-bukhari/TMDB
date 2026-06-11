package com.example.tmdb.feature.search

import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SearchUiMapperTest {

    private fun movie(posterPath: String?) = Movie(
        id = MovieId(27205),
        title = "Inception",
        overview = "",
        posterPath = posterPath,
        backdropPath = null,
        releaseDate = null,
        voteAverage = 8.4,
        voteCount = 1,
    )

    @Test
    fun `given a poster path, when mapping to a search item, then the w342 url is built`() {
        assertEquals(
            "https://image.tmdb.org/t/p/w342/poster.jpg",
            movie("/poster.jpg").toSearchItem().posterUrl,
        )
    }

    @Test
    fun `given no poster path, when mapping, then posterUrl stays null`() {
        assertNull(movie(null).toSearchItem().posterUrl)
    }
}
