package com.example.tmdb.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.example.tmdb.core.common.DispatcherProvider
import com.example.tmdb.core.database.TmdbDatabase
import com.example.tmdb.core.network.OmdbApi
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class MovieDetailRepositoryTest {

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
            dispatchers = TestDispatchers(UnconfinedTestDispatcher()),
        )
    }

    @After
    fun tearDown() {
        server.close()
        db.close()
    }

    @Test
    fun `given an uncached movie, when refreshing detail, then observers see null then the cached detail`() = runTest {
        server.enqueue(MockResponse.Builder().code(200).body(fixture("movie_detail_550.json")).build())

        repository.observeMovieDetail(MovieId(550)).test {
            assertNull(awaitItem())

            val refresh = repository.refreshMovieDetail(MovieId(550))
            assertEquals(true, refresh.isSuccess)

            val detail = awaitItem()
            assertEquals("Fight Club", detail?.title)
            assertEquals(listOf("Drama", "Thriller", "Comedy"), detail?.genres)
            assertEquals(139, detail?.runtimeMinutes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given TMDB returns 404, when refreshing detail, then the error is typed and nothing is cached`() = runTest {
        server.enqueue(
            MockResponse.Builder().code(404).body("""{"status_code":34,"status_message":"not found"}""").build(),
        )

        val result = repository.refreshMovieDetail(MovieId(42))

        assertEquals(AppError.NotFound, result.appErrorOrNull())
        repository.observeMovieDetail(MovieId(42)).test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
