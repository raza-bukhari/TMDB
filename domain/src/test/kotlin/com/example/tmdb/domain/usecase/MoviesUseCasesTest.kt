package com.example.tmdb.domain.usecase

import com.example.tmdb.domain.FakeRepo
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.AppException
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieCategory
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.MoviePage
import com.example.tmdb.domain.model.appErrorOrNull
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private val aMovie = Movie(
    id = MovieId(550),
    title = "Fight Club",
    overview = "An insomniac office worker…",
    posterPath = "/poster.jpg",
    backdropPath = null,
    releaseDate = LocalDate.of(1999, 10, 15),
    voteAverage = 8.4,
    voteCount = 26280,
)

class MoviesUseCasesTest {

    @Test
    fun `given cached movies, when observing a category, then the stream is exposed and category passes through`() = runTest {
        val repo = FakeRepo().apply { movies.value = listOf(aMovie) }

        val emitted = ObserveMoviesUseCase(repo)(MovieCategory.TOP_RATED).first()

        assertEquals(listOf(aMovie), emitted)
        assertEquals(MovieCategory.TOP_RATED, repo.lastCategory)
    }

    @Test
    fun `given refresh succeeds, when invoked, then paging bounds return and category passes through`() = runTest {
        val repo = FakeRepo().apply { refreshResult = Result.success(MoviePage(1, 10)) }

        val page = RefreshMoviesUseCase(repo)(MovieCategory.NOW_PLAYING).getOrThrow()

        assertEquals(MoviePage(1, 10), page)
        assertTrue(page.canLoadMore)
        assertEquals(MovieCategory.NOW_PLAYING, repo.lastCategory)
    }

    @Test
    fun `given the last page, when refreshing, then canLoadMore is false`() = runTest {
        val repo = FakeRepo().apply { refreshResult = Result.success(MoviePage(5, 5)) }

        assertFalse(RefreshMoviesUseCase(repo)(MovieCategory.POPULAR).getOrThrow().canLoadMore)
    }

    @Test
    fun `given a target page, when loading more, then category and page pass through`() = runTest {
        val repo = FakeRepo().apply { loadMoreResult = Result.success(MoviePage(3, 9)) }

        val page = LoadMoreMoviesUseCase(repo)(MovieCategory.POPULAR, page = 3).getOrThrow()

        assertEquals(MoviePage(3, 9), page)
        assertEquals(MovieCategory.POPULAR to 3, repo.lastLoadMore)
    }

    @Test
    fun `given a rate-limited refresh, when invoked, then the typed error surfaces`() = runTest {
        val repo = FakeRepo().apply { refreshResult = Result.failure(AppException(AppError.RateLimited)) }

        assertEquals(AppError.RateLimited, RefreshMoviesUseCase(repo)(MovieCategory.POPULAR).appErrorOrNull())
    }
}
