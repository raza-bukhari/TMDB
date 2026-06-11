package com.example.tmdb.feature.movies

import app.cash.turbine.test
import com.example.tmdb.core.testing.FakeMovieRepository
import com.example.tmdb.core.testing.MainDispatcherRule
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.AppException
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieCategory
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.MoviePage
import com.example.tmdb.domain.usecase.LoadMoreMoviesUseCase
import com.example.tmdb.domain.usecase.ObserveMoviesUseCase
import com.example.tmdb.domain.usecase.RefreshMoviesUseCase
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
        observeMovies = ObserveMoviesUseCase(repository),
        refreshMovies = RefreshMoviesUseCase(repository),
        loadMoreMovies = LoadMoreMoviesUseCase(repository),
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

    /** Configures refresh to seed [category]'s cache with [movies] and report [totalPages]. */
    private fun seedRefresh(category: MovieCategory, movies: List<Movie>, totalPages: Int = 1) {
        repository.onRefresh = { c ->
            if (c == category) repository.moviesFlow(c).value = movies
            Result.success(MoviePage(page = 1, totalPages = totalPages))
        }
    }

    @Test
    fun `given refresh succeeds with movies, when observing state, then content moves Loading to Movies`() = runTest {
        seedRefresh(MovieCategory.POPULAR, listOf(movie(550, "Fight Club")))
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(MoviesContent.Loading, awaitItem().content)
            val loaded = expectMostRecentItemAfter { it.content is MoviesContent.Movies && !it.isRefreshing }
            assertEquals(
                MovieListItem(550, "Fight Club", "https://image.tmdb.org/t/p/w342/p550.jpg", 7.5),
                (loaded.content as MoviesContent.Movies).movies.single(),
            )
        }
    }

    @Test
    fun `given empty cache and refresh fails offline, when observing state, then content is a typed Error`() = runTest {
        repository.onRefresh = { Result.failure(AppException(AppError.Offline)) }
        val viewModel = viewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItemAfter { it.content is MoviesContent.Error }
            assertEquals(MoviesContent.Error(AppError.Offline), state.content)
        }
    }

    @Test
    fun `given cached movies and refresh fails, when observing state, then stale movies stay visible`() = runTest {
        repository.moviesFlow(MovieCategory.POPULAR).value = listOf(movie(1, "Cached"))
        repository.onRefresh = { Result.failure(AppException(AppError.RateLimited)) }
        val viewModel = viewModel()

        viewModel.uiState.test {
            val state = expectMostRecentItemAfter { it.content is MoviesContent.Movies && !it.isRefreshing }
            val movies = state.content as MoviesContent.Movies
            assertEquals("Cached", movies.movies.first().title)
            // Non-blocking signal that the refresh behind the cache failed.
            assertEquals(AppError.RateLimited, movies.staleError)
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
    fun `given a different tab is selected, when its cache populates, then that category's movies render and it is refreshed`() = runTest {
        seedRefresh(MovieCategory.POPULAR, listOf(movie(1, "Popular")))
        repository.onRefresh = { c ->
            repository.moviesFlow(c).value = when (c) {
                MovieCategory.POPULAR -> listOf(movie(1, "Popular"))
                MovieCategory.TOP_RATED -> listOf(movie(2, "Top Rated"))
                MovieCategory.NOW_PLAYING -> listOf(movie(3, "Now Playing"))
            }
            Result.success(MoviePage(1, 1))
        }
        val viewModel = viewModel()

        viewModel.uiState.test {
            expectMostRecentItemAfter { it.content is MoviesContent.Movies && it.selectedCategory == MovieCategory.POPULAR }

            viewModel.onCategorySelected(MovieCategory.TOP_RATED)

            val state = expectMostRecentItemAfter {
                it.selectedCategory == MovieCategory.TOP_RATED && it.content is MoviesContent.Movies
            }
            assertEquals("Top Rated", (state.content as MoviesContent.Movies).movies.single().title)
        }
        assertTrue(MovieCategory.TOP_RATED in repository.refreshedCategories)
    }

    @Test
    fun `given more pages available, when load more requested, then the next page is fetched`() = runTest {
        repository.onRefresh = { c ->
            repository.moviesFlow(c).value = listOf(movie(1))
            Result.success(MoviePage(page = 1, totalPages = 3))
        }
        repository.onLoadMore = { c, page ->
            repository.moviesFlow(c).value = repository.moviesFlow(c).value + movie(page * 10L)
            Result.success(MoviePage(page = page, totalPages = 3))
        }
        val viewModel = viewModel()

        viewModel.uiState.test {
            val first = expectMostRecentItemAfter {
                (it.content as? MoviesContent.Movies)?.canLoadMore == true
            }
            assertEquals(1, (first.content as MoviesContent.Movies).movies.size)

            viewModel.onLoadMoreRequested()

            val appended = expectMostRecentItemAfter {
                (it.content as? MoviesContent.Movies)?.movies?.size == 2
            }
            assertTrue(appended.content is MoviesContent.Movies)
        }
        assertEquals(listOf(MovieCategory.POPULAR to 2), repository.loadMoreCalls)
    }

    @Test
    fun `given the last page, when load more requested, then no fetch happens`() = runTest {
        repository.onRefresh = { c ->
            repository.moviesFlow(c).value = listOf(movie(1))
            Result.success(MoviePage(page = 1, totalPages = 1))
        }
        val viewModel = viewModel()

        viewModel.uiState.test {
            expectMostRecentItemAfter { it.content is MoviesContent.Movies && !it.isRefreshing }
            viewModel.onLoadMoreRequested()
            expectNoEvents()
        }
        assertTrue(repository.loadMoreCalls.isEmpty())
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
