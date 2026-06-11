package com.example.tmdb.feature.movies

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.tmdb.core.designsystem.component.StateTestTags
import com.example.tmdb.core.designsystem.theme.TMDBTheme
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.MovieCategory
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MoviesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val movies = persistentListOf(
        MovieListItem(550, "Fight Club", null, 8.4),
        MovieListItem(603, "The Matrix", null, 8.2),
    )

    @Composable
    private fun Screen(
        state: MoviesUiState,
        onCategorySelected: (MovieCategory) -> Unit = {},
        onRetryClick: () -> Unit = {},
        onMovieClick: (Long) -> Unit = {},
        onLoadMoreRequested: () -> Unit = {},
        onSearchClick: () -> Unit = {},
    ) {
        TMDBTheme {
            MoviesScreenContent(
                state = state,
                onCategorySelected = onCategorySelected,
                onRetryClick = onRetryClick,
                onMovieClick = onMovieClick,
                onLoadMoreRequested = onLoadMoreRequested,
                onSearchClick = onSearchClick,
            )
        }
    }

    @Test
    fun givenMoviesContent_whenRendered_thenGridAndTitlesAreVisible() {
        composeRule.setContent {
            Screen(MoviesUiState(content = MoviesContent.Movies(movies)))
        }

        composeRule.onNodeWithTag(MoviesTestTags.GRID).assertIsDisplayed()
        composeRule.onNodeWithText("Fight Club").assertIsDisplayed()
        composeRule.onNodeWithText("The Matrix").assertIsDisplayed()
    }

    @Test
    fun givenMoviesContent_whenCardClicked_thenCallbackReceivesMovieId() {
        var clicked: Long? = null
        composeRule.setContent {
            Screen(MoviesUiState(content = MoviesContent.Movies(movies)), onMovieClick = { clicked = it })
        }

        composeRule.onNodeWithTag(MoviesTestTags.movieCard(550)).performClick()

        assertEquals(550L, clicked)
    }

    @Test
    fun givenTabs_whenTopRatedSelected_thenCallbackReceivesCategory() {
        var selected: MovieCategory? = null
        composeRule.setContent {
            Screen(MoviesUiState(content = MoviesContent.Movies(movies)), onCategorySelected = { selected = it })
        }

        composeRule.onNodeWithTag(MoviesTestTags.tab(MovieCategory.TOP_RATED)).performClick()

        assertEquals(MovieCategory.TOP_RATED, selected)
    }

    @Test
    fun givenAppendingMovies_whenRendered_thenAppendSpinnerIsVisible() {
        composeRule.setContent {
            Screen(MoviesUiState(content = MoviesContent.Movies(movies, isAppending = true, canLoadMore = true)))
        }

        composeRule.onNodeWithTag(MoviesTestTags.APPEND_SPINNER).assertIsDisplayed()
    }

    @Test
    fun givenMoviesScreen_whenSearchClicked_thenSearchCallbackFires() {
        var searched = false
        composeRule.setContent {
            Screen(MoviesUiState(content = MoviesContent.Movies(movies)), onSearchClick = { searched = true })
        }

        composeRule.onNodeWithTag(MoviesTestTags.SEARCH).performClick()

        assertEquals(true, searched)
    }

    @Test
    fun givenErrorContent_whenRetryClicked_thenRetryCallbackFires() {
        var retried = false
        composeRule.setContent {
            Screen(MoviesUiState(content = MoviesContent.Error(AppError.Offline)), onRetryClick = { retried = true })
        }

        composeRule.onNodeWithTag(StateTestTags.ERROR).assertIsDisplayed()
        composeRule.onNodeWithTag(StateTestTags.ERROR_RETRY).performClick()

        assertEquals(true, retried)
    }

    @Test
    fun givenLoadingContent_whenRendered_thenSpinnerIsVisible() {
        composeRule.setContent {
            Screen(MoviesUiState(content = MoviesContent.Loading))
        }

        composeRule.onNodeWithTag(StateTestTags.LOADING).assertIsDisplayed()
    }
}
