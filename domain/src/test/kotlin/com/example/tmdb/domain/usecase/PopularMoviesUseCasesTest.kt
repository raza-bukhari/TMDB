package com.example.tmdb.domain.usecase

import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.AppException
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.appErrorOrNull
import com.example.tmdb.domain.repository.MovieRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private val aMovie = Movie(
    id = MovieId(550),
    title = "Fight Club",
    overview = "An insomniac office worker…",
    posterPath = "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
    backdropPath = "/fCayJrkfRaCRCTh8GqN30f8oyQF.jpg",
    releaseDate = LocalDate.of(1999, 10, 15),
    voteAverage = 8.4,
    voteCount = 26280,
)

private class FakeMovieRepository(
    var refreshResult: Result<Unit> = Result.success(Unit),
) : MovieRepository {
    val movies = MutableStateFlow<List<Movie>>(emptyList())
    val detail = MutableStateFlow<MovieDetail?>(null)
    var refreshCalls = 0

    override fun observePopularMovies(): Flow<List<Movie>> = movies

    override suspend fun refreshPopularMovies(): Result<Unit> {
        refreshCalls++
        return refreshResult
    }

    override fun observeMovieDetail(id: MovieId): Flow<MovieDetail?> = detail

    override suspend fun refreshMovieDetail(id: MovieId): Result<Unit> {
        refreshCalls++
        return refreshResult
    }
}

class PopularMoviesUseCasesTest {

    @Test
    fun `given cached movies, when observing, then the repository stream is exposed unchanged`() = runTest {
        val repository = FakeMovieRepository().apply { movies.value = listOf(aMovie) }
        val observe = ObservePopularMoviesUseCase(repository)

        val emitted = observe().first()

        assertEquals(listOf(aMovie), emitted)
    }

    @Test
    fun `given refresh succeeds, when invoked, then success is returned and repository called once`() = runTest {
        val repository = FakeMovieRepository()
        val refresh = RefreshPopularMoviesUseCase(repository)

        val result = refresh()

        assertTrue(result.isSuccess)
        assertEquals(1, repository.refreshCalls)
    }

    @Test
    fun `given repository fails with rate limit, when refreshing, then typed error is readable from result`() = runTest {
        val repository = FakeMovieRepository(
            refreshResult = Result.failure(AppException(AppError.RateLimited)),
        )
        val refresh = RefreshPopularMoviesUseCase(repository)

        val result = refresh()

        assertEquals(AppError.RateLimited, result.appErrorOrNull())
    }

    @Test
    fun `given a foreign exception in a result, when reading the error, then it maps to Unknown`() {
        val result: Result<Unit> = Result.failure(IllegalStateException("boom"))

        val error = result.appErrorOrNull()

        assertTrue(error is AppError.Unknown)
    }
}
