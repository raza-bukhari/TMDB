package com.example.tmdb.domain.usecase

import com.example.tmdb.domain.FakeRepo
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private val aMovie = Movie(
    id = MovieId(27205),
    title = "Inception",
    overview = "Dom Cobb is a skilled thief…",
    posterPath = "/inception.jpg",
    backdropPath = null,
    releaseDate = null,
    voteAverage = 8.4,
    voteCount = 36000,
)

class SearchMoviesUseCaseTest {

    @Test
    fun `given a query, when searching, then query passes through`() = runTest {
        val repository = FakeRepo().apply {
            onSearch = { listOf(aMovie) }
        }

        SearchMoviesUseCase(repository)("inception").first()

        assertEquals("inception", repository.lastSearch)
    }
}
