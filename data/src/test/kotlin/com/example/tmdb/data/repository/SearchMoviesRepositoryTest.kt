package com.example.tmdb.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.tmdb.core.common.DispatcherProvider
import com.example.tmdb.core.database.TmdbDatabase
import com.example.tmdb.core.network.TmdbApi
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.appErrorOrNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SearchMoviesRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var db: TmdbDatabase
    private lateinit var repository: OfflineFirstMovieRepository

    // Single dispatcher shared with runTest so tmdbCall's backoff delays use one virtual clock.
    private val testDispatcher = UnconfinedTestDispatcher()

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
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(
                Json { ignoreUnknownKeys = true; coerceInputValues = true }
                    .asConverterFactory("application/json".toMediaType()),
            )
            .build()
            .create(TmdbApi::class.java)
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TmdbDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = OfflineFirstMovieRepository(
            api = api,
            dao = db.movieDao(),
            detailDao = db.movieDetailDao(),
            dispatchers = TestDispatchers(testDispatcher),
        )
    }

    @After
    fun tearDown() {
        server.close()
        db.close()
    }

    @Test
    fun `given a results payload, when searching, then domain movies and paging fields map`() = runTest(testDispatcher) {
        server.enqueue(MockResponse.Builder().code(200).body(fixture("search_movie_inception.json")).build())

        val results = repository.searchMovies("inception").getOrThrow()

        assertEquals(MovieId(27205), results.movies.first().id)
        assertEquals("Inception", results.movies.first().title)
        assertEquals(1, results.page)
        assertEquals(3, results.totalPages)
        assertTrue(results.canLoadMore)
    }

    @Test
    fun `given a 429, when searching, then the error is RateLimited and nothing touches the cache`() = runTest(testDispatcher) {
        // Initial call + 2 backoff retries (tmdbCall MAX_RETRIES) all see 429.
        repeat(3) {
            server.enqueue(
                MockResponse.Builder().code(429).body("""{"status_code":25,"status_message":"rate limited"}""").build(),
            )
        }

        val result = repository.searchMovies("anything")

        assertEquals(AppError.RateLimited, result.appErrorOrNull())
    }
}
