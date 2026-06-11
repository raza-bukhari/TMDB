package com.example.tmdb.feature.movies

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.tmdb.core.designsystem.component.StateTestTags
import com.example.tmdb.core.designsystem.theme.TMDBTheme
import com.example.tmdb.domain.model.AppError
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

    @Test
    fun givenMoviesContent_whenRendered_thenGridAndTitlesAreVisible() {
        composeRule.setContent {
            TMDBTheme {
                MoviesScreenContent(
                    state = MoviesUiState(content = MoviesContent.Movies(movies)),
                    onRetryClick = {},
                    onMovieClick = {},
                    onSearchClick = {},
                )
            }
        }

        composeRule.onNodeWithTag(MoviesTestTags.GRID).assertIsDisplayed()
        composeRule.onNodeWithText("Fight Club").assertIsDisplayed()
        composeRule.onNodeWithText("The Matrix").assertIsDisplayed()
    }

    @Test
    fun givenMoviesContent_whenCardClicked_thenCallbackReceivesMovieId() {
        var clicked: Long? = null
        composeRule.setContent {
            TMDBTheme {
                MoviesScreenContent(
                    state = MoviesUiState(content = MoviesContent.Movies(movies)),
                    onRetryClick = {},
                    onMovieClick = { clicked = it },
                    onSearchClick = {},
                )
            }
        }

        composeRule.onNodeWithTag(MoviesTestTags.movieCard(550)).performClick()

        assertEquals(550L, clicked)
    }

    @Test
    fun givenMoviesScreen_whenSearchClicked_thenSearchCallbackFires() {
        var searched = false
        composeRule.setContent {
            TMDBTheme {
                MoviesScreenContent(
                    state = MoviesUiState(content = MoviesContent.Movies(movies)),
                    onRetryClick = {},
                    onMovieClick = {},
                    onSearchClick = { searched = true },
                )
            }
        }

        composeRule.onNodeWithTag(MoviesTestTags.SEARCH).performClick()

        assertEquals(true, searched)
    }

    @Test
    fun givenErrorContent_whenRetryClicked_thenRetryCallbackFires() {
        var retried = false
        composeRule.setContent {
            TMDBTheme {
                MoviesScreenContent(
                    state = MoviesUiState(content = MoviesContent.Error(AppError.Offline)),
                    onRetryClick = { retried = true },
                    onMovieClick = {},
                    onSearchClick = {},
                )
            }
        }

        composeRule.onNodeWithTag(StateTestTags.ERROR).assertIsDisplayed()
        composeRule.onNodeWithTag(StateTestTags.ERROR_RETRY).performClick()

        assertEquals(true, retried)
    }

    @Test
    fun givenLoadingContent_whenRendered_thenSpinnerIsVisible() {
        composeRule.setContent {
            TMDBTheme {
                MoviesScreenContent(
                    state = MoviesUiState(content = MoviesContent.Loading),
                    onRetryClick = {},
                    onMovieClick = {},
                    onSearchClick = {},
                )
            }
        }

        composeRule.onNodeWithTag(StateTestTags.LOADING).assertIsDisplayed()
    }
}
