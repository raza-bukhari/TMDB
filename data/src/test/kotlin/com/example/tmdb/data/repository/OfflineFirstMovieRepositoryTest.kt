package com.example.tmdb.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.example.tmdb.core.common.DispatcherProvider
import com.example.tmdb.core.database.MovieCategories
import com.example.tmdb.core.database.MovieEntity
import com.example.tmdb.core.database.TmdbDatabase
import com.example.tmdb.core.network.TmdbApi
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.MovieCategory
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.appErrorOrNull
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
 * End-to-end data-layer integration: MockWebServer → Retrofit → Room → Flow.
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
            dispatchers = TestDispatchers(UnconfinedTestDispatcher()),
        )
    }

    @After
    fun tearDown() {
        server.close()
        db.close()
    }

    @Test
    fun `given an empty cache, when refreshing, then observers see empty then the fetched page in TMDB order`() = runTest {
        server.enqueue(MockResponse.Builder().code(200).body(fixture("popular_page_1.json")).build())

        repository.observeMovies(MovieCategory.POPULAR).test {
            assertEquals(emptyList<Nothing>(), awaitItem())

            val refresh = repository.refreshMovies(MovieCategory.POPULAR)
            assertEquals(500, refresh.getOrThrow().totalPages)

            val refreshed = awaitItem()
            assertEquals(listOf(MovieId(550), MovieId(603), MovieId(999999)), refreshed.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given a populated cache, when refresh fails, then cached movies remain and the error is typed`() = runTest {
        db.movieDao().insertAll(
            listOf(
                MovieEntity(
                    id = 1, category = MovieCategories.POPULAR, orderIndex = 0,
                    title = "Cached", overview = "", posterPath = null, backdropPath = null,
                    releaseDate = null, voteAverage = 5.0, voteCount = 1,
                ),
            ),
        )
        server.enqueue(MockResponse.Builder().code(401).body(fixture("error_401.json")).build())

        val result = repository.refreshMovies(MovieCategory.POPULAR)

        assertEquals(AppError.InvalidToken, result.appErrorOrNull())
        repository.observeMovies(MovieCategory.POPULAR).test {
            assertEquals(listOf(MovieId(1)), awaitItem().map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given a refreshed first page, when loading more, then page 2 appends after page 1 in order`() = runTest {
        server.enqueue(MockResponse.Builder().code(200).body(fixture("popular_page_1.json")).build())
        server.enqueue(MockResponse.Builder().code(200).body(fixture("top_rated_page_2.json")).build())

        repository.refreshMovies(MovieCategory.POPULAR).getOrThrow()
        val more = repository.loadMoreMovies(MovieCategory.POPULAR, page = 2).getOrThrow()

        assertEquals(2, more.page)
        repository.observeMovies(MovieCategory.POPULAR).test {
            // page 1 ids (orderIndex 0..2) then the page-2 id (orderIndex 20) — appended, not interleaved.
            assertEquals(
                listOf(MovieId(550), MovieId(603), MovieId(999999), MovieId(157336)),
                awaitItem().map { it.id },
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
