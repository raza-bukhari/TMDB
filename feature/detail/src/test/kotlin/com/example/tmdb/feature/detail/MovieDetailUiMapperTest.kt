package com.example.tmdb.feature.detail

import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.MovieId
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MovieDetailUiMapperTest {

    @Test
    fun `given minutes variants, when formatting runtime, then hours and minutes render compactly`() {
        assertEquals("2h 19m", formatRuntime(139))
        assertEquals("1h", formatRuntime(60))
        assertEquals("45m", formatRuntime(45))
    }

    @Test
    fun `given a full detail, when mapping to ui, then urls year and genres derive correctly`() {
        val ui = MovieDetail(
            id = MovieId(550),
            title = "Fight Club",
            overview = "…",
            tagline = "Mischief.",
            posterPath = "/p.jpg",
            backdropPath = "/b.jpg",
            releaseDate = LocalDate.of(1999, 10, 15),
            runtimeMinutes = 139,
            voteAverage = 8.4,
            voteCount = 1,
            genres = listOf("Drama", "Thriller"),
        ).toUi()

        assertEquals("https://image.tmdb.org/t/p/w342/p.jpg", ui.posterUrl)
        assertEquals("https://image.tmdb.org/t/p/w780/b.jpg", ui.backdropUrl)
        assertEquals("1999", ui.releaseYear)
        assertEquals(listOf("Drama", "Thriller"), ui.genres.toList())
    }

    @Test
    fun `given missing optionals, when mapping to ui, then they stay null instead of inventing values`() {
        val ui = MovieDetail(
            id = MovieId(1),
            title = "X",
            overview = "",
            tagline = null,
            posterPath = null,
            backdropPath = null,
            releaseDate = null,
            runtimeMinutes = null,
            voteAverage = 0.0,
            voteCount = 0,
            genres = emptyList(),
        ).toUi()

        assertNull(ui.posterUrl)
        assertNull(ui.backdropUrl)
        assertNull(ui.releaseYear)
        assertNull(ui.runtime)
    }
}
