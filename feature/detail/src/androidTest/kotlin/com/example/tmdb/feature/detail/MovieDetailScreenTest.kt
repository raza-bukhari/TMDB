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
        status = null,
        seasonCount = null,
        episodeCount = null,
        rating = 8.4,
        certification = "R",
        genres = persistentListOf("Drama", "Thriller"),
        seasons = persistentListOf(),
        selectedSeasonNumber = null,
        episodes = persistentListOf(),
        lastEpisode = null,
        nextEpisode = null,
        cast = persistentListOf(),
        directors = persistentListOf("David Fincher"),
        producers = persistentListOf(),
        similarMovies = persistentListOf(),
        watchProviders = persistentListOf(),
    )

    @Test
    fun givenDetailContent_whenDisplayed_thenTitleTaglineAndMetaAreVisible() {
        composeRule.setContent {
            TMDBTheme {
                MovieDetailScreenContent(
                    state = MovieDetailUiState(content = MovieDetailContent.Detail(detail)),
                    onBackClick = {},
                    onMovieClick = {},
                    onPersonClick = {},
                    onVideoClick = { _, _, _ -> },
                    onRetryClick = {},
                    onWatchlistToggle = {},
                    onActivityStatusSelected = {},
                    onFavoriteToggle = {},
                    onUserRatingSelected = {},
                    onNotesChanged = {},
                    onSeasonSelected = {},
                    onEpisodeSelected = {},
                    onEpisodeDismissed = {},
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
                    onMovieClick = {},
                    onPersonClick = {},
                    onVideoClick = { _, _, _ -> },
                    onRetryClick = { retried = true },
                    onWatchlistToggle = {},
                    onActivityStatusSelected = {},
                    onFavoriteToggle = {},
                    onUserRatingSelected = {},
                    onNotesChanged = {},
                    onSeasonSelected = {},
                    onEpisodeSelected = {},
                    onEpisodeDismissed = {},
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
                    onMovieClick = {},
                    onPersonClick = {},
                    onVideoClick = { _, _, _ -> },
                    onRetryClick = {},
                    onWatchlistToggle = {},
                    onActivityStatusSelected = {},
                    onFavoriteToggle = {},
                    onUserRatingSelected = {},
                    onNotesChanged = {},
                    onSeasonSelected = {},
                    onEpisodeSelected = {},
                    onEpisodeDismissed = {},
                )
            }
        }

        composeRule.onNodeWithTag(DetailTestTags.BACK).performClick()
        assertTrue(back)
    }

    @Test
    fun givenVideos_whenDisplayed_thenVideoCarouselCardIsShown() {
        composeRule.setContent {
            TMDBTheme {
                MovieDetailScreenContent(
                    state = MovieDetailUiState(
                        content = MovieDetailContent.Detail(
                            detail.copy(
                                videos = persistentListOf(
                                    VideoUi(key = "abc123", name = "Official Trailer", type = "Trailer"),
                                ),
                            ),
                        ),
                    ),
                    onBackClick = {},
                    onMovieClick = {},
                    onPersonClick = {},
                    onVideoClick = { _, _, _ -> },
                    onRetryClick = {},
                    onWatchlistToggle = {},
                    onActivityStatusSelected = {},
                    onFavoriteToggle = {},
                    onUserRatingSelected = {},
                    onNotesChanged = {},
                    onSeasonSelected = {},
                    onEpisodeSelected = {},
                    onEpisodeDismissed = {},
                )
            }
        }

        composeRule.onNodeWithTag(DetailTestTags.videoCard("abc123")).assertIsDisplayed()
    }
}
