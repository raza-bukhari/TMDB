package com.example.tmdb.feature.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.tmdb.core.testing.FakeMovieRepository
import com.example.tmdb.core.testing.MainDispatcherRule
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.AppException
import com.example.tmdb.domain.model.ExternalRatings
import com.example.tmdb.domain.model.MediaVideo
import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.TvEpisode
import com.example.tmdb.domain.model.TvSeason
import com.example.tmdb.domain.usecase.AddMovieToWatchlistUseCase
import com.example.tmdb.domain.usecase.GetExternalRatingsUseCase
import com.example.tmdb.domain.usecase.GetMediaVideosUseCase
import com.example.tmdb.domain.usecase.GetTvSeasonUseCase
import com.example.tmdb.domain.usecase.ObserveMovieDetailUseCase
import com.example.tmdb.domain.usecase.ObserveWatchlistKeysUseCase
import com.example.tmdb.domain.usecase.ObserveWatchlistItemsUseCase
import com.example.tmdb.domain.usecase.RefreshMovieDetailUseCase
import com.example.tmdb.domain.usecase.RemoveMovieFromWatchlistUseCase
import com.example.tmdb.domain.usecase.UpdateUserActivityUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class MovieDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMovieRepository()

    private val aDetail = MovieDetail(
        id = MovieId(550),
        title = "Fight Club",
        overview = "An insomniac…",
        tagline = "Mischief. Mayhem. Soap.",
        posterPath = "/poster.jpg",
        backdropPath = null,
        releaseDate = null,
        runtimeMinutes = 139,
        voteAverage = 8.4,
        voteCount = 26280,
        genres = listOf("Drama"),
        imdbId = "tt0137523",
    )

    private fun viewModel(mediaType: String = "MOVIE") = MovieDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("movieId" to 550L, "mediaType" to mediaType)),
        observeMovieDetail = ObserveMovieDetailUseCase(repository),
        refreshMovieDetail = RefreshMovieDetailUseCase(repository),
        getExternalRatings = GetExternalRatingsUseCase(repository),
        getMediaVideos = GetMediaVideosUseCase(repository),
        getTvSeason = GetTvSeasonUseCase(repository),
        observeWatchlistItems = ObserveWatchlistItemsUseCase(repository),
        observeWatchlistKeys = ObserveWatchlistKeysUseCase(repository),
        addMovieToWatchlist = AddMovieToWatchlistUseCase(repository),
        removeMovieFromWatchlist = RemoveMovieFromWatchlistUseCase(repository),
        updateUserActivity = UpdateUserActivityUseCase(repository),
    )

    @Test
    fun `given refresh populates the cache, when the screen subscribes, then Loading becomes Detail`() = runTest {
        repository.onDetailRefreshCachePopulation = { aDetail }

        viewModel().uiState.test {
            assertEquals(MovieDetailContent.Loading, awaitItem().content)

            val detail = expectMostRecentItemAfter { it.content is MovieDetailContent.Detail }
            assertEquals("Fight Club", (detail.content as MovieDetailContent.Detail).detail.title)
            assertEquals("2h 19m", (detail.content as MovieDetailContent.Detail).detail.runtime)
        }
    }

    @Test
    fun `given no cache and a 404, when the screen subscribes, then content is a typed error`() = runTest {
        repository.refreshResult = Result.failure(AppException(AppError.NotFound))

        viewModel().uiState.test {
            val state = expectMostRecentItemAfter { it.content is MovieDetailContent.Error }
            assertEquals(AppError.NotFound, (state.content as MovieDetailContent.Error).error)
        }
    }

    @Test
    fun `given a failed first load, when retry succeeds, then content recovers to Detail`() = runTest {
        repository.refreshResult = Result.failure(AppException(AppError.Offline))
        val viewModel = viewModel()

        viewModel.uiState.test {
            expectMostRecentItemAfter { it.content is MovieDetailContent.Error }

            repository.refreshResult = Result.success(Unit)
            repository.onDetailRefreshCachePopulation = { aDetail }
            viewModel.onRetryClicked()

            val state = expectMostRecentItemAfter { it.content is MovieDetailContent.Detail }
            assertTrue(state.content is MovieDetailContent.Detail)
        }
    }

    @Test
    fun `given a cached detail and a failing refresh, when the screen subscribes, then cache wins over the error`() = runTest {
        repository.detailFlow.value = aDetail
        repository.refreshResult = Result.failure(AppException(AppError.Offline))

        viewModel().uiState.test {
            val state = expectMostRecentItemAfter { !it.isRefreshing }
            assertTrue(state.content is MovieDetailContent.Detail)
        }
    }

    @Test
    fun `given detail has imdb id, when external ratings load, then detail shows imdb and rotten tomatoes`() = runTest {
        repository.externalRatingsResult = Result.success(
            ExternalRatings(imdb = "8.8", rottenTomatoes = "81%", metascore = "66"),
        )
        repository.onDetailRefreshCachePopulation = { aDetail }

        viewModel().uiState.test {
            val state = expectMostRecentItemAfter {
                (it.content as? MovieDetailContent.Detail)?.detail?.externalRatings?.hasAny == true
            }
            val ratings = (state.content as MovieDetailContent.Detail).detail.externalRatings
            assertEquals("8.8/10", ratings.imdb)
            assertEquals("81%", ratings.rottenTomatoes)
            assertEquals("66/100", ratings.metascore)
        }
        assertEquals(listOf("tt0137523"), repository.externalRatingCalls)
    }

    @Test
    fun `given a YouTube trailer exists, when videos load, then detail exposes trailer url`() = runTest {
        repository.onDetailRefreshCachePopulation = { aDetail }
        repository.videosResult = Result.success(
            listOf(
                MediaVideo(
                    id = "trailer-1",
                    name = "Official Trailer",
                    key = "abc123",
                    site = "YouTube",
                    type = "Trailer",
                    official = true,
                ),
            ),
        )

        viewModel().uiState.test {
            val state = expectMostRecentItemAfter {
                (it.content as? MovieDetailContent.Detail)?.detail?.trailerUrl != null
            }

            assertEquals(
                "https://www.youtube.com/watch?v=abc123",
                (state.content as MovieDetailContent.Detail).detail.trailerUrl,
            )
        }
    }

    @Test
    fun `given tv detail has seasons, when first season loads, then detail exposes episodes`() = runTest {
        repository.onDetailRefreshCachePopulation = {
            aDetail.copy(
                title = "Breaking Bad",
                seasons = listOf(
                    TvSeason(
                        id = 10,
                        name = "Season 1",
                        overview = "",
                        posterPath = "/season.jpg",
                        airDate = LocalDate.of(2008, 1, 20),
                        seasonNumber = 1,
                        episodeCount = 7,
                        voteAverage = 8.5,
                    ),
                ),
                numberOfSeasons = 5,
                numberOfEpisodes = 62,
            )
        }
        repository.tvSeasonResult = Result.success(
            TvSeason(
                id = 10,
                name = "Season 1",
                overview = "",
                posterPath = "/season.jpg",
                airDate = LocalDate.of(2008, 1, 20),
                seasonNumber = 1,
                episodeCount = 7,
                voteAverage = 8.5,
                episodes = listOf(
                    TvEpisode(
                        id = 101,
                        name = "Pilot",
                        overview = "A chemistry teacher changes lanes.",
                        stillPath = "/pilot.jpg",
                        airDate = LocalDate.of(2008, 1, 20),
                        seasonNumber = 1,
                        episodeNumber = 1,
                        runtimeMinutes = 58,
                        voteAverage = 8.3,
                        voteCount = 100,
                    ),
                ),
            ),
        )

        viewModel(mediaType = "TV").uiState.test {
            val state = expectMostRecentItemAfter {
                (it.content as? MovieDetailContent.Detail)?.detail?.episodes?.isNotEmpty() == true
            }

            val detail = (state.content as MovieDetailContent.Detail).detail
            assertEquals("Breaking Bad", detail.title)
            assertEquals("Pilot", detail.episodes.first().title)
            assertEquals(5, detail.seasonCount)
            assertEquals(62, detail.episodeCount)
        }
    }

    @Test
    fun `given detail is visible, when watchlist toggled, then saved state is reflected`() = runTest {
        repository.onDetailRefreshCachePopulation = { aDetail }
        val viewModel = viewModel()

        viewModel.uiState.test {
            expectMostRecentItemAfter { it.content is MovieDetailContent.Detail }

            viewModel.onWatchlistToggle()

            val saved = expectMostRecentItemAfter {
                (it.content as? MovieDetailContent.Detail)?.detail?.isWatchlisted == true
            }
            assertEquals(true, (saved.content as MovieDetailContent.Detail).detail.isWatchlisted)
        }
    }

    /** Awaits items until [predicate] holds, failing the test on timeout. */
    private suspend fun app.cash.turbine.ReceiveTurbine<MovieDetailUiState>.expectMostRecentItemAfter(
        predicate: (MovieDetailUiState) -> Boolean,
    ): MovieDetailUiState {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }
}
