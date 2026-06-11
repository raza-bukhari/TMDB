package com.example.tmdb.data.mapper

import com.example.tmdb.core.network.dto.GenreDto
import com.example.tmdb.core.network.dto.MovieDetailDto
import com.example.tmdb.domain.model.MovieId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MovieDetailMappersTest {

    private val dto = MovieDetailDto(
        id = 550,
        title = "Fight Club",
        overview = "An insomniac…",
        tagline = "Mischief. Mayhem. Soap.",
        posterPath = "/poster.jpg",
        backdropPath = "/backdrop.jpg",
        releaseDate = "1999-10-15",
        runtime = 139,
        voteAverage = 8.438,
        voteCount = 26280,
        genres = listOf(GenreDto(18, "Drama"), GenreDto(53, "Thriller")),
    )

    @Test
    fun `given a detail dto, when round-tripping through the entity, then genres and runtime survive`() {
        val domain = dto.toEntity().toDomain()

        assertEquals(MovieId(550), domain.id)
        assertEquals(listOf("Drama", "Thriller"), domain.genres)
        assertEquals(139, domain.runtimeMinutes)
        assertEquals("Mischief. Mayhem. Soap.", domain.tagline)
    }

    @Test
    fun `given no genres and a blank tagline, when mapping, then domain gets empty list and null tagline`() {
        val domain = dto.copy(genres = emptyList(), tagline = "").toEntity().toDomain()

        assertEquals(emptyList<String>(), domain.genres)
        assertNull(domain.tagline)
    }

    @Test
    fun `given a null runtime, when mapping, then domain runtime is null`() {
        assertNull(dto.copy(runtime = null).toEntity().toDomain().runtimeMinutes)
    }
}
