package com.example.tmdb.feature.movies

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.tmdb.core.designsystem.component.StateTestTags
import com.example.tmdb.core.designsystem.theme.TMDBTheme
import com.example.tmdb.core.designsystem.theme.AppTheme
import com.example.tmdb.domain.model.HomeList
import com.example.tmdb.domain.model.MediaType
import com.example.tmdb.domain.model.WatchlistStatus
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MoviesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val fightClub = movie(550, "Fight Club")
    private val matrix = movie(603, "The Matrix")
    private val movies = persistentListOf(fightClub, matrix)

    @Composable
    private fun Screen(
        state: MoviesUiState,
        onTrendingWindowSelected: (TrendingWindow) -> Unit = {},
        onRetryClick: () -> Unit = {},
        onRefresh: () -> Unit = {},
        onMovieClick: (Long, MediaType) -> Unit = { _, _ -> },
        onSearchQueryChanged: (String) -> Unit = {},
        onTabSelected: (MoviesTab) -> Unit = {},
        onWatchlistToggle: (MovieListItem) -> Unit = {},
    ) {
        TMDBTheme(appTheme = AppTheme.SYSTEM) {
            MoviesScreenContent(
                state = state,
                selectedTheme = AppTheme.SYSTEM,
                onThemeSelected = {},
                searchResults = null,
                discoverResults = null,
                onSearchQueryChanged = onSearchQueryChanged,
                onDiscoverQueryChanged = {},
                onDiscoverFiltersChanged = {},
                onDiscoverFiltersReset = {},
                onTabSelected = onTabSelected,
                onTabBack = { false },
                onWatchlistFilterSelected = {},
                onWatchlistSortSelected = {},
                onWatchlistToggle = onWatchlistToggle,
                onTrendingWindowSelected = onTrendingWindowSelected,
                onRetryClick = onRetryClick,
                onRefresh = onRefresh,
                onMovieClick = onMovieClick,
            )
        }
    }

    @Test
    fun givenHomeSections_whenRendered_thenTitlesAreVisible() {
        composeRule.setContent {
            Screen(loadedState())
        }

        composeRule.onNodeWithTag(MoviesTestTags.HOME).assertIsDisplayed()
        composeRule.onNodeWithText("Fight Club").assertIsDisplayed()
        composeRule.onNodeWithText("The Matrix").assertIsDisplayed()
    }

    @Test
    fun givenHomeScreen_whenRendered_thenBottomNavigationIsVisible() {
        composeRule.setContent {
            Screen(loadedState())
        }

        composeRule.onNodeWithTag(MoviesTestTags.BOTTOM_NAV).assertIsDisplayed()
        composeRule.onNodeWithText("Home").assertIsDisplayed()
        composeRule.onNodeWithText("Discover").assertIsDisplayed()
        composeRule.onNodeWithText("Watchlist").assertIsDisplayed()
        composeRule.onNodeWithText("Profile").assertIsDisplayed()
    }

    @Test
    fun givenDiscoverTabClicked_whenRendered_thenCallbackReceivesDiscoverTab() {
        var selected: MoviesTab? = null
        composeRule.setContent {
            Screen(loadedState(), onTabSelected = { selected = it })
        }

        composeRule.onNodeWithText("Discover").performClick()

        assertEquals(MoviesTab.DISCOVER, selected)
    }

    @Test
    fun givenDiscoverTabSelected_whenRendered_thenDiscoverContentIsVisible() {
        composeRule.setContent {
            Screen(loadedState(selectedTab = MoviesTab.DISCOVER))
        }

        composeRule.onNodeWithTag(MoviesTestTags.DISCOVER).assertIsDisplayed()
        composeRule.onNodeWithText("Discover").assertIsDisplayed()
    }

    @Test
    fun givenHomeMovie_whenCardClicked_thenCallbackReceivesMovieId() {
        var clicked: Long? = null
        composeRule.setContent {
            Screen(loadedState(), onMovieClick = { id, _ -> clicked = id })
        }

        composeRule.onNodeWithTag(MoviesTestTags.movieCard(550)).performClick()

        assertEquals(550L, clicked)
    }

    @Test
    fun givenWatchlistTabSelected_whenSavedMoviesExist_thenSavedMovieIsVisible() {
        composeRule.setContent {
            Screen(
                loadedState(
                    selectedTab = MoviesTab.WATCHLIST,
                    watchlistMovies = persistentListOf(matrix),
                ),
            )
        }

        composeRule.onNodeWithTag(MoviesTestTags.WATCHLIST).assertIsDisplayed()
        composeRule.onNodeWithText("The Matrix").assertIsDisplayed()
    }

    @Test
    fun givenTrendingToggle_whenWeekClicked_thenCallbackReceivesWeeklyWindow() {
        var selected: TrendingWindow? = null
        composeRule.setContent {
            Screen(loadedState(), onTrendingWindowSelected = { selected = it })
        }

        composeRule.onNodeWithTag(MoviesTestTags.TRENDING_WEEK).performClick()

        assertEquals(TrendingWindow.THIS_WEEK, selected)
    }

    @Test
    fun givenInlineSearch_whenTyped_thenQueryCallbackFires() {
        var query = ""
        composeRule.setContent {
            Screen(loadedState(), onSearchQueryChanged = { query = it })
        }

        composeRule.onNodeWithTag(MoviesTestTags.SEARCH_FIELD).performTextInput("matrix")

        assertEquals("matrix", query)
    }

    @Test
    fun givenSearchQuery_whenRendered_thenSearchHeaderAppearsInline() {
        composeRule.setContent {
            Screen(loadedState(searchQuery = "matrix"))
        }

        composeRule.onNodeWithTag(MoviesTestTags.SEARCH_FIELD).assertIsDisplayed()
    }

    @Test
    fun givenErrorWithoutSections_whenRetryClicked_thenRetryCallbackFires() {
        var retried = false
        composeRule.setContent {
            Screen(
                MoviesUiState(isLoading = false, errorMessage = "You're offline. Connect and try again."),
                onRetryClick = { retried = true },
            )
        }

        composeRule.onNodeWithTag(StateTestTags.ERROR).assertIsDisplayed()
        composeRule.onNodeWithTag(StateTestTags.ERROR_RETRY).performClick()

        assertEquals(true, retried)
    }

    @Test
    fun givenLoadingHome_whenRendered_thenSpinnerIsVisible() {
        composeRule.setContent {
            Screen(MoviesUiState(isLoading = true))
        }

        composeRule.onNodeWithTag(StateTestTags.LOADING).assertIsDisplayed()
    }

    private fun loadedState(
        searchQuery: String = "",
        selectedTab: MoviesTab = MoviesTab.HOME,
        watchlistMovies: kotlinx.collections.immutable.ImmutableList<MovieListItem> = persistentListOf(),
    ) = MoviesUiState(
        selectedTab = selectedTab,
        searchQuery = searchQuery,
        isLoading = false,
        hero = fightClub,
        sections = persistentListOf(
            HomeSectionUi(
                list = HomeList.TRENDING_TODAY,
                title = "Trending",
                subtitle = "Movies people are watching today",
                movies = movies,
            ),
        ),
        watchlistMovies = watchlistMovies,
        watchlistItems = watchlistMovies.map { it.toTestWatchlistItem() }.toImmutableList(),
    )

    private fun movie(id: Long, title: String) = MovieListItem(
        id = id,
        title = title,
        overview = "Overview for $title",
        posterUrl = null,
        backdropUrl = null,
        rating = 8.4,
        voteCount = 100,
        releaseYear = "1999",
    )

    private fun MovieListItem.toTestWatchlistItem() = WatchlistItemUi(
        movie = this,
        status = WatchlistStatus.PLAN_TO_WATCH,
        favorite = false,
        userRating = null,
        watchedDate = null,
        notes = "",
        addedAtMillis = 0,
    )
}
