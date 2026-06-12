package com.example.tmdb.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.example.tmdb.core.common.DispatcherProvider
import com.example.tmdb.core.database.TmdbDatabase
import com.example.tmdb.core.network.OmdbApi
import com.example.tmdb.core.network.TmdbApi
import com.example.tmdb.domain.model.HomeList
import com.example.tmdb.domain.model.UserMediaActivity
import com.example.tmdb.domain.model.WatchlistStatus
import com.example.tmdb.domain.model.MovieId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * End-to-end data-layer integration: MockWebServer -> Retrofit -> repository -> Room/domain.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class OfflineFirstMovieRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var db: TmdbDatabase
    private lateinit var repository: OfflineFirstMovieRepository

    private class TestDispatchers(dispatcher: CoroutineDispatcher) : DispatcherProvider {
        override val io = dispatcher
        override val default = dispatcher
        override val main = dispatcher
    }

    private fun fixture(name: String): String =
        checkNotNull(javaClass.classLoader?.getResource("fixtures/$name")) { "missing fixture $name" }.readText()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(
                Json { ignoreUnknownKeys = true; coerceInputValues = true }
                    .asConverterFactory("application/json".toMediaType()),
            )
            .build()
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TmdbDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = OfflineFirstMovieRepository(
            api = retrofit.create(TmdbApi::class.java),
            omdbApi = retrofit.create(OmdbApi::class.java),
            dao = db.movieDao(),
            detailDao = db.movieDetailDao(),
            watchlistDao = db.watchlistDao(),
            dispatchers = TestDispatchers(UnconfinedTestDispatcher()),
        )
    }

    @After
    fun tearDown() {
        server.close()
        db.close()
    }

    @Test
    fun `given a trending request, when loading home list, then movies map in TMDB order`() = runTest {
        server.enqueue(MockResponse.Builder().code(200).body(fixture("popular_page_1.json")).build())

        val movies = repository.homeList(HomeList.TRENDING_TODAY).getOrThrow()

        assertEquals(listOf(MovieId(550), MovieId(603), MovieId(999999)), movies.map { it.id })
        val requestUrl = server.takeRequest().url
        assertEquals("/trending/movie/day?page=1", "${requestUrl.encodedPath}?${requestUrl.encodedQuery}")
    }

    @Test
    fun `given movie detail includes imdb id, when refreshing detail, then cached detail exposes imdb id`() = runTest {
        server.enqueue(MockResponse.Builder().code(200).body(fixture("movie_detail_550.json")).build())

        repository.refreshMovieDetail(MovieId(550)).getOrThrow()

        repository.observeMovieDetail(MovieId(550)).test {
            assertEquals("tt0137523", awaitItem()?.imdbId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given a movie is added to watchlist, when observing watchlist, then it is emitted newest first`() = runTest {
        server.enqueue(MockResponse.Builder().code(200).body(fixture("popular_page_1.json")).build())
        val movie = repository.homeList(HomeList.POPULAR).getOrThrow().first()

        repository.addToWatchlist(movie).getOrThrow()

        repository.observeWatchlist().test {
            assertEquals(listOf(MovieId(550)), awaitItem().map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
        repository.observeWatchlistIds().test {
            assertEquals(setOf(MovieId(550)), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given watchlist activity is updated, when observing rich watchlist, then metadata persists`() = runTest {
        server.enqueue(MockResponse.Builder().code(200).body(fixture("popular_page_1.json")).build())
        val movie = repository.homeList(HomeList.POPULAR).getOrThrow().first()
        repository.addToWatchlist(movie).getOrThrow()

        repository.updateUserActivity(
            UserMediaActivity(
                mediaId = movie.id,
                status = WatchlistStatus.COMPLETED,
                favorite = true,
                userRating = 9.0,
                notes = "Rewatch with friends.",
            ),
        ).getOrThrow()

        repository.observeWatchlistItems().test {
            val item = awaitItem().first()
            assertEquals(WatchlistStatus.COMPLETED, item.status)
            assertEquals(true, item.favorite)
            assertEquals(9.0, item.userRating)
            assertEquals("Rewatch with friends.", item.notes)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
