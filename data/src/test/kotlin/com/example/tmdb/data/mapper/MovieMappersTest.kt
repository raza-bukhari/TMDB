package com.example.tmdb.data.mapper

import com.example.tmdb.core.network.dto.MovieDto
import com.example.tmdb.domain.model.MovieId
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MovieMappersTest {

    private val dto = MovieDto(
        id = 550,
        title = "Fight Club",
        overview = "An insomniac…",
        posterPath = "/poster.jpg",
        backdropPath = "/backdrop.jpg",
        releaseDate = "1999-10-15",
        voteAverage = 8.4,
        voteCount = 26280,
    )

    @Test
    fun `given a dto, when mapping to entity, then category and order are stamped and fields copied`() {
        val entity = dto.toEntity(category = "popular", orderIndex = 7)

        assertEquals(550L, entity.id)
        assertEquals("popular", entity.category)
        assertEquals(7, entity.orderIndex)
        assertEquals("Fight Club", entity.title)
        assertEquals("/poster.jpg", entity.posterPath)
        assertEquals("1999-10-15", entity.releaseDate)
    }

    @Test
    fun `given an entity, when mapping to domain, then ids and dates are strongly typed`() {
        val movie = dto.toEntity("popular", 0).toDomain()

        assertEquals(MovieId(550), movie.id)
        assertEquals(LocalDate.of(1999, 10, 15), movie.releaseDate)
        assertEquals(8.4, movie.voteAverage, 0.0001)
    }

    @Test
    fun `given blank or malformed release dates, when mapping to domain, then date degrades to null`() {
        assertNull(dto.copy(releaseDate = "").toEntity("popular", 0).toDomain().releaseDate)
        assertNull(dto.copy(releaseDate = null).toEntity("popular", 0).toDomain().releaseDate)
        assertNull(dto.copy(releaseDate = "not-a-date").toEntity("popular", 0).toDomain().releaseDate)
    }
}
