package com.example.tmdb.domain.usecase

import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.AppException
import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.SearchResults
import com.example.tmdb.domain.model.appErrorOrNull
import com.example.tmdb.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.emptyFlow
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

private class FakeDetailRepository(
    var refreshResult: Result<Unit> = Result.success(Unit),
) : MovieRepository {
    val detail = MutableStateFlow<MovieDetail?>(null)
    var lastRequestedId: MovieId? = null

    override fun observePopularMovies() = emptyFlow<Nothing>()
    override suspend fun refreshPopularMovies(): Result<Unit> = Result.success(Unit)
    override suspend fun searchMovies(query: String, page: Int): Result<SearchResults> =
        Result.success(SearchResults(emptyList(), page, page))

    override fun observeMovieDetail(id: MovieId): Flow<MovieDetail?> {
        lastRequestedId = id
        return detail
    }

    override suspend fun refreshMovieDetail(id: MovieId): Result<Unit> {
        lastRequestedId = id
        return refreshResult
    }
}

class MovieDetailUseCasesTest {

    @Test
    fun `given an uncached movie, when observing detail, then null emits and the id is passed through`() = runTest {
        val repository = FakeDetailRepository()
        val observe = ObserveMovieDetailUseCase(repository)

        assertNull(observe(MovieId(550)).first())
        assertEquals(MovieId(550), repository.lastRequestedId)
    }

    @Test
    fun `given a cached movie, when observing detail, then it emits`() = runTest {
        val repository = FakeDetailRepository().apply { detail.value = aDetail }

        assertEquals(aDetail, ObserveMovieDetailUseCase(repository)(MovieId(550)).first())
    }

    @Test
    fun `given refresh fails with NotFound, when refreshing detail, then the typed error surfaces`() = runTest {
        val repository = FakeDetailRepository(
            refreshResult = Result.failure(AppException(AppError.NotFound)),
        )

        val result = RefreshMovieDetailUseCase(repository)(MovieId(1))

        assertEquals(AppError.NotFound, result.appErrorOrNull())
    }
}
