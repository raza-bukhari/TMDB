package com.example.tmdb.feature.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.tmdb.core.designsystem.theme.TMDBTheme
import com.example.tmdb.domain.model.AppError
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SearchScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val results = SearchContent.Results(
        movies = persistentListOf(
            SearchResultItem(27205, "Inception", null, 8.4),
            SearchResultItem(603, "The Matrix", null, 8.2),
        ),
        isAppending = false,
        canLoadMore = false,
    )

    @Test
    fun givenResults_whenRendered_thenGridAndTitlesAreVisible() {
        composeRule.setContent {
            TMDBTheme {
                SearchScreenContent(
                    state = SearchUiState(query = "in", content = results),
                    onQueryChanged = {},
                    onRetryClick = {},
                    onLoadMoreRequested = {},
                    onMovieClick = {},
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithTag(SearchTestTags.GRID).assertIsDisplayed()
        composeRule.onNodeWithText("Inception").assertIsDisplayed()
        composeRule.onNodeWithText("The Matrix").assertIsDisplayed()
    }

    @Test
    fun givenInput_whenTextEntered_thenQueryCallbackFires() {
        var typed = ""
        composeRule.setContent {
            TMDBTheme {
                SearchScreenContent(
                    state = SearchUiState(),
                    onQueryChanged = { typed = it },
                    onRetryClick = {},
                    onLoadMoreRequested = {},
                    onMovieClick = {},
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithTag(SearchTestTags.INPUT).performTextInput("dune")

        assertEquals("dune", typed)
    }

    @Test
    fun givenResults_whenCardClicked_thenMovieIdPropagates() {
        var clicked: Long? = null
        composeRule.setContent {
            TMDBTheme {
                SearchScreenContent(
                    state = SearchUiState(query = "in", content = results),
                    onQueryChanged = {},
                    onRetryClick = {},
                    onLoadMoreRequested = {},
                    onMovieClick = { clicked = it },
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithText("Inception").performClick()

        assertEquals(27205L, clicked)
    }

    @Test
    fun givenError_whenRetryClicked_thenRetryCallbackFires() {
        var retried = false
        composeRule.setContent {
            TMDBTheme {
                SearchScreenContent(
                    state = SearchUiState(query = "x", content = SearchContent.Error(AppError.Offline)),
                    onQueryChanged = {},
                    onRetryClick = { retried = true },
                    onLoadMoreRequested = {},
                    onMovieClick = {},
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithText("Retry").performClick()

        assertEquals(true, retried)
    }
}
