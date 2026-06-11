package com.example.tmdb.domain.usecase

import com.example.tmdb.domain.FakeRepo
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.AppException
import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.appErrorOrNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private val aDetail = MovieDetail(
    id = MovieId(550),
    title = "Fight Club",
    overview = "An insomniac…",
    tagline = "Mischief. Mayhem. Soap.",
    posterPath = "/poster.jpg",
    backdropPath = null,
    releaseDate = null,
    runtimeMinutes = 139,
    voteAverage = 8.4,
    voteCount = 26280,
    genres = listOf("Drama"),
)

class MovieDetailUseCasesTest {

    @Test
    fun `given an uncached movie, when observing detail, then null emits and the id is passed through`() = runTest {
        val repository = FakeRepo()
        val observe = ObserveMovieDetailUseCase(repository)

        assertNull(observe(MovieId(550)).first())
        assertEquals(MovieId(550), repository.lastDetailId)
    }

    @Test
    fun `given a cached movie, when observing detail, then it emits`() = runTest {
        val repository = FakeRepo().apply { detail.value = aDetail }

        assertEquals(aDetail, ObserveMovieDetailUseCase(repository)(MovieId(550)).first())
    }

    @Test
    fun `given refresh fails with NotFound, when refreshing detail, then the typed error surfaces`() = runTest {
        val repository = FakeRepo().apply {
            detailResult = Result.failure(AppException(AppError.NotFound))
        }

        val result = RefreshMovieDetailUseCase(repository)(MovieId(1))

        assertEquals(AppError.NotFound, result.appErrorOrNull())
    }
}
