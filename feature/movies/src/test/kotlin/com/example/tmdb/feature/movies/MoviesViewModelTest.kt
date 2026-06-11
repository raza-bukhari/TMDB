package com.example.tmdb.feature.movies

import app.cash.turbine.test
import com.example.tmdb.core.testing.FakeMovieRepository
import com.example.tmdb.core.testing.MainDispatcherRule
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.AppException
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.usecase.ObservePopularMoviesUseCase
import com.example.tmdb.domain.usecase.RefreshPopularMoviesUseCase
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MoviesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMovieRepository()

    private fun viewModel() = MoviesViewModel(
        observePopularMovies = ObservePopularMoviesUseCase(repository),
        refreshPopularMovies = RefreshPopularMoviesUseCase(repository),
    )

    private fun movie(id: Long, title: String = "Movie $id") = Movie(
        id = MovieId(id),
        title = title,
        overview = "",
        posterPath = "/p$id.jpg",
        backdropPath = null,
        releaseDate = null,
        voteAverage = 7.5,
        voteCount = 10,
    )

    @Test
    fun `given refresh succeeds with movies, when observing state, then content moves Loading to Movies`() = runTest {
        repository.onRefreshCachePopulation = { listOf(movie(550, "Fight Club")) }
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(MoviesContent.Loading, awaitItem().content)
            val loaded = expectMostRecentItemAfter { it.content is MoviesContent.Movies && !it.isRefreshing }
            val movies = (loaded.content as MoviesContent.Movies).movies
            assertEquals(persistentListOf(MovieListItem(550, "Fight Club", "https://image.tmdb.org/t/p/w342/p550.jpg", 7.5)), movies)
        }
    }

    @Test
    fun `given empty cache and refresh fails offline, when observing state, then content is a typed Error`() = runTest {
        repository.refreshResult = Result.failure(AppException(AppError.Offline))
        val viewModel = viewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItemAfter { it.content is MoviesContent.Error }
            assertEquals(MoviesContent.Error(AppError.Offline), state.content)
        }
    }

    @Test
    fun `given cached movies and refresh fails, when observing state, then stale movies stay visible`() = runTest {
        repository.moviesFlow.value = listOf(movie(1, "Cached"))
        repository.refreshResult = Result.failure(AppException(AppError.RateLimited))
        val viewModel = viewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItemAfter { it.content is MoviesContent.Movies && !it.isRefreshing }
            assertEquals("Cached", (state.content as MoviesContent.Movies).movies.first().title)
        }
    }

    @Test
    fun `given empty cache and refresh succeeds with nothing, when observing state, then content is Empty`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItemAfter { it.content is MoviesContent.Empty }
            assertEquals(MoviesContent.Empty, state.content)
        }
    }

    @Test
    fun `given a retry click after failure, when refresh succeeds, then movies render`() = runTest {
        repository.refreshResult = Result.failure(AppException(AppError.Offline))
        val viewModel = viewModel()

        viewModel.uiState.test {
            expectMostRecentItemAfter { it.content is MoviesContent.Error }

            repository.refreshResult = Result.success(Unit)
            repository.onRefreshCachePopulation = { listOf(movie(2)) }
            viewModel.onRetryClicked()

            val state = expectMostRecentItemAfter { it.content is MoviesContent.Movies }
            assertEquals(2, repository.refreshCalls)
            assertTrue(state.content is MoviesContent.Movies)
        }
    }

    @Test
    fun `given a movie click, when collecting effects, then NavigateToDetail is emitted once`() = runTest {
        val viewModel = viewModel()

        viewModel.effects.test {
            viewModel.onMovieClicked(550)
            assertEquals(MoviesEffect.NavigateToDetail(550), awaitItem())
            expectNoEvents()
        }
    }
}

/** Skips intermediate emissions until [predicate] holds; fails the test if it never does. */
private suspend fun <T> app.cash.turbine.TurbineTestContext<T>.expectMostRecentItemAfter(
    predicate: (T) -> Boolean,
): T {
    while (true) {
        val item = awaitItem()
        if (predicate(item)) return item
    }
}
