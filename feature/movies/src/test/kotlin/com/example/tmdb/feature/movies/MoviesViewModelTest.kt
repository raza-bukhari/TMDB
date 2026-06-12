package com.example.tmdb.feature.movies

import app.cash.turbine.test
import com.example.tmdb.core.testing.FakeMovieRepository
import com.example.tmdb.core.testing.MainDispatcherRule
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.AppException
import com.example.tmdb.domain.model.HomeList
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.MediaType
import com.example.tmdb.domain.usecase.AddMovieToWatchlistUseCase
import com.example.tmdb.domain.usecase.DiscoverMoviesUseCase
import com.example.tmdb.domain.usecase.GetHomeListUseCase
import com.example.tmdb.domain.usecase.ObserveWatchlistIdsUseCase
import com.example.tmdb.domain.usecase.ObserveWatchlistItemsUseCase
import com.example.tmdb.domain.usecase.ObserveWatchlistUseCase
import com.example.tmdb.domain.usecase.RemoveMovieFromWatchlistUseCase
import com.example.tmdb.domain.usecase.SearchMoviesUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class MoviesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMovieRepository()

    private fun viewModel() = MoviesViewModel(
        getHomeList = GetHomeListUseCase(repository),
        searchMovies = SearchMoviesUseCase(repository),
        discoverMovies = DiscoverMoviesUseCase(repository),
        observeWatchlist = ObserveWatchlistUseCase(repository),
        observeWatchlistItems = ObserveWatchlistItemsUseCase(repository),
        observeWatchlistIds = ObserveWatchlistIdsUseCase(repository),
        addMovieToWatchlist = AddMovieToWatchlistUseCase(repository),
        removeMovieFromWatchlist = RemoveMovieFromWatchlistUseCase(repository),
    )

    private fun movie(id: Long, title: String = "Movie $id") = Movie(
        id = MovieId(id),
        title = title,
        overview = "Overview $id",
        posterPath = "/p$id.jpg",
        backdropPath = "/b$id.jpg",
        releaseDate = null,
        voteAverage = 7.5,
        voteCount = 10,
    )

    @Test
    fun `given home lists load, when observing state, then hero and sections are populated`() = runTest {
        repository.onHomeList = { list -> Result.success(listOf(movie(id = list.ordinal.toLong() + 1))) }

        viewModel().uiState.test {
            val loaded = expectMostRecentItemAfter { !it.isLoading && it.sections.isNotEmpty() }

            assertEquals("Movie 1", loaded.hero?.title)
            assertEquals(listOf("Trending", "What's Popular", "In Theaters", "Top Rated", "Coming Soon"), loaded.sections.map { it.title })
            assertFalse(loaded.isRefreshing)
        }
    }

    @Test
    fun `given weekly trending selected, when home reloads, then weekly trending list is requested`() = runTest {
        repository.onHomeList = { list -> Result.success(listOf(movie(id = list.ordinal.toLong() + 1))) }
        val viewModel = viewModel()

        viewModel.uiState.test {
            expectMostRecentItemAfter { !it.isLoading }

            viewModel.onTrendingWindowSelected(TrendingWindow.THIS_WEEK)

            val weekly = expectMostRecentItemAfter { it.trendingWindow == TrendingWindow.THIS_WEEK && !it.isRefreshing }
            assertEquals("Movies people talked about this week", weekly.sections.first().subtitle)
        }
        assertEquals(HomeList.TRENDING_THIS_WEEK, repository.homeListCalls.last { it.name.startsWith("TRENDING") })
    }

    @Test
    fun `given home load fails, when observing state, then a typed error message is shown`() = runTest {
        repository.onHomeList = { Result.failure(AppException(AppError.Offline)) }

        viewModel().uiState.test {
            val failed = expectMostRecentItemAfter { !it.isLoading && it.errorMessage != null }
            assertEquals("You're offline. Connect and try again.", failed.errorMessage)
        }
    }

    @Test
    fun `given a movie click, when collecting effects, then NavigateToDetail is emitted once`() = runTest {
        val viewModel = viewModel()

        viewModel.effects.test {
            viewModel.onMovieClicked(550, MediaType.TV)
            assertEquals(MoviesEffect.NavigateToDetail(550, MediaType.TV), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `given another bottom tab selected, when observing state, then tab changes and search clears`() = runTest {
        val viewModel = viewModel()

        viewModel.onSearchQueryChanged("matrix")
        viewModel.onTabSelected(MoviesTab.DISCOVER)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(MoviesTab.DISCOVER, state.selectedTab)
            assertEquals("", state.searchQuery)
        }
    }

    @Test
    fun `given discover controls change, when observing state, then query and filters update`() = runTest {
        val viewModel = viewModel()
        val filters = MovieFilters(sort = MovieSort.RATING_DESC, minRating = 7f)

        viewModel.onDiscoverQueryChanged("matrix")
        viewModel.onDiscoverFiltersChanged(filters)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("matrix", state.discoverQuery)
            assertEquals(filters, state.discoverFilters)

            viewModel.onDiscoverFiltersReset()

            val reset = awaitItem()
            assertEquals(MovieFilters(), reset.discoverFilters)
        }
    }

    @Test
    fun `given movie not saved, when watchlist toggled, then repository saves it`() = runTest {
        val viewModel = viewModel()
        val item = movie(550, "Fight Club").toListItem()

        viewModel.uiState.test {
            viewModel.onWatchlistToggle(item)

            val state = expectMostRecentItemAfter { MovieId(550) in it.watchlistIds }
            assertEquals(listOf(550L), state.watchlistMovies.map { it.id })
            cancelAndIgnoreRemainingEvents()
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
