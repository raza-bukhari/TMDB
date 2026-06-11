package com.example.tmdb.feature.search

import app.cash.turbine.test
import com.example.tmdb.core.testing.FakeMovieRepository
import com.example.tmdb.core.testing.MainDispatcherRule
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.AppException
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.SearchResults
import com.example.tmdb.domain.usecase.SearchMoviesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMovieRepository()
    private fun viewModel() = SearchViewModel(SearchMoviesUseCase(repository))

    private fun movie(id: Long, title: String) = Movie(
        id = MovieId(id),
        title = title,
        overview = "",
        posterPath = null,
        backdropPath = null,
        releaseDate = null,
        voteAverage = 7.0,
        voteCount = 10,
    )

    @Test
    fun `given a blank query, when observed, then content stays Idle and no search runs`() = runTest {
        viewModel().uiState.test {
            assertEquals(SearchContent.Idle, awaitItem().content)
            advanceTimeBy(500)
            expectNoEvents()
        }
        assertTrue(repository.searchCalls.isEmpty())
    }

    @Test
    fun `given rapid keystrokes, when typing, then debounce collapses them into one search`() = runTest {
        repository.onSearch = { _, page ->
            Result.success(SearchResults(listOf(movie(1, "Inception")), page, totalPages = 1))
        }
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(SearchContent.Idle, awaitItem().content)
            vm.onQueryChanged("i")
            vm.onQueryChanged("in")
            vm.onQueryChanged("inc")
            advanceTimeBy(301) // cross the 300ms debounce once

            val results = expectMostRecentItemAfter { it.content is SearchContent.Results }
            assertEquals(1, (results.content as SearchContent.Results).movies.size)
            cancelAndIgnoreRemainingEvents()
        }
        // Only the final settled query triggers a network call.
        assertEquals(listOf("inc" to 1), repository.searchCalls)
    }

    @Test
    fun `given a query with no matches, when searched, then content is NoResults`() = runTest {
        repository.onSearch = { _, page -> Result.success(SearchResults(emptyList(), page, page)) }
        val vm = viewModel()

        vm.uiState.test {
            awaitItem()
            vm.onQueryChanged("zzzz")
            advanceTimeBy(301)
            assertEquals(SearchContent.NoResults, expectMostRecentItemAfter { it.content is SearchContent.NoResults }.content)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given a failing search, when searched, then content is a typed Error`() = runTest {
        repository.onSearch = { _, _ -> Result.failure(AppException(AppError.Offline)) }
        val vm = viewModel()

        vm.uiState.test {
            awaitItem()
            vm.onQueryChanged("matrix")
            advanceTimeBy(301)
            val error = expectMostRecentItemAfter { it.content is SearchContent.Error }
            assertEquals(AppError.Offline, (error.content as SearchContent.Error).error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given more pages, when load more requested, then the next page appends`() = runTest {
        repository.onSearch = { _, page ->
            Result.success(
                SearchResults(
                    movies = listOf(movie(page * 10L, "Movie $page")),
                    page = page,
                    totalPages = 3,
                ),
            )
        }
        val vm = viewModel()

        vm.uiState.test {
            awaitItem()
            vm.onQueryChanged("space")
            advanceTimeBy(301)
            expectMostRecentItemAfter { it.content is SearchContent.Results }

            vm.onLoadMoreRequested()
            val appended = expectMostRecentItemAfter {
                (it.content as? SearchContent.Results)?.movies?.size == 2
            }
            val results = appended.content as SearchContent.Results
            assertEquals(listOf("Movie 1", "Movie 2"), results.movies.map { it.title })
            assertTrue(results.canLoadMore)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf("space" to 1, "space" to 2), repository.searchCalls)
    }

    @Test
    fun `given the query is cleared, when emptied, then content returns to Idle`() = runTest {
        repository.onSearch = { _, page ->
            Result.success(SearchResults(listOf(movie(1, "Inception")), page, page))
        }
        val vm = viewModel()

        vm.uiState.test {
            awaitItem()
            vm.onQueryChanged("inception")
            advanceTimeBy(301)
            expectMostRecentItemAfter { it.content is SearchContent.Results }

            vm.onQueryChanged("")
            advanceTimeBy(301)
            assertEquals(SearchContent.Idle, expectMostRecentItemAfter { it.content is SearchContent.Idle }.content)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private suspend fun app.cash.turbine.ReceiveTurbine<SearchUiState>.expectMostRecentItemAfter(
        predicate: (SearchUiState) -> Boolean,
    ): SearchUiState {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }
}
