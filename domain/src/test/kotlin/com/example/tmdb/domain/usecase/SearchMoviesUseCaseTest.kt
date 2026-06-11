package com.example.tmdb.domain.usecase

import com.example.tmdb.domain.FakeRepo
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.AppException
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.SearchResults
import com.example.tmdb.domain.model.appErrorOrNull
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    fun `given a query, when searching, then query and page pass through and results return`() = runTest {
        val repository = FakeRepo().apply {
            searchResult = { _, _ -> Result.success(SearchResults(listOf(aMovie), page = 2, totalPages = 5)) }
        }

        val results = SearchMoviesUseCase(repository)("inception", page = 2).getOrThrow()

        assertEquals("inception" to 2, repository.lastSearch)
        assertEquals(listOf(aMovie), results.movies)
        assertTrue(results.canLoadMore)
    }

    @Test
    fun `given the last page, when reading canLoadMore, then it is false`() {
        assertFalse(SearchResults(emptyList(), page = 5, totalPages = 5).canLoadMore)
    }

    @Test
    fun `given a rate-limited repository, when searching, then the typed error surfaces`() = runTest {
        val repository = FakeRepo().apply {
            searchResult = { _, _ -> Result.failure(AppException(AppError.RateLimited)) }
        }

        assertEquals(AppError.RateLimited, SearchMoviesUseCase(repository)("x").appErrorOrNull())
    }
}
