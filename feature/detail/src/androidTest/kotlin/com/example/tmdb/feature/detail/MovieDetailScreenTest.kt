package com.example.tmdb.feature.detail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.tmdb.core.designsystem.theme.TMDBTheme
import com.example.tmdb.domain.model.AppError
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MovieDetailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val detail = MovieDetailUi(
        id = 550,
        title = "Fight Club",
        tagline = "Mischief. Mayhem. Soap.",
        overview = "An insomniac office worker…",
        posterUrl = null,
        backdropUrl = null,
        releaseYear = "1999",
        runtime = "2h 19m",
        rating = 8.4,
        genres = persistentListOf("Drama", "Thriller"),
    )

    @Test
    fun givenDetailContent_whenDisplayed_thenTitleTaglineAndMetaAreVisible() {
        composeRule.setContent {
            TMDBTheme {
                MovieDetailScreenContent(
                    state = MovieDetailUiState(content = MovieDetailContent.Detail(detail)),
                    onBackClick = {},
                    onRetryClick = {},
                )
            }
        }

        composeRule.onNodeWithText("Fight Club").assertIsDisplayed()
        composeRule.onNodeWithText("Mischief. Mayhem. Soap.").assertIsDisplayed()
        composeRule.onNodeWithText("2h 19m").assertIsDisplayed()
        composeRule.onNodeWithText("Drama · Thriller").assertIsDisplayed()
    }

    @Test
    fun givenErrorContent_whenRetryClicked_thenCallbackFires() {
        var retried = false
        composeRule.setContent {
            TMDBTheme {
                MovieDetailScreenContent(
                    state = MovieDetailUiState(content = MovieDetailContent.Error(AppError.Offline)),
                    onBackClick = {},
                    onRetryClick = { retried = true },
                )
            }
        }

        composeRule.onNodeWithText("Retry").performClick()
        assertTrue(retried)
    }

    @Test
    fun givenAnyContent_whenBackClicked_thenCallbackFires() {
        var back = false
        composeRule.setContent {
            TMDBTheme {
                MovieDetailScreenContent(
                    state = MovieDetailUiState(content = MovieDetailContent.Loading),
                    onBackClick = { back = true },
                    onRetryClick = {},
                )
            }
        }

        composeRule.onNodeWithTag(DetailTestTags.BACK).performClick()
        assertTrue(back)
    }
}
