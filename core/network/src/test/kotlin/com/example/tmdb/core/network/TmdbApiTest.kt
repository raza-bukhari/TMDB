package com.example.tmdb.core.network

import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.appErrorOrNull
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class TmdbApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: TmdbApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(TmdbAuthInterceptor("test-token"))
                    .build(),
            )
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(TmdbApi::class.java)
    }

    @After
    fun tearDown() {
        server.close()
    }

    private fun fixture(name: String): String =
        checkNotNull(javaClass.classLoader?.getResource("fixtures/$name")) { "missing fixture $name" }.readText()

    private fun response(code: Int, body: String): MockResponse =
        MockResponse.Builder().code(code).body(body).build()

    @Test
    fun `given a 200 popular page, when fetching, then snake_case fields map and auth header is sent`() = runTest {
        server.enqueue(response(200, fixture("popular_page_1.json")))

        val page = api.popularMovies(page = 1)

        assertEquals(1, page.page)
        assertEquals(500, page.totalPages)
        assertEquals(10_000, page.totalResults)
        assertEquals(3, page.results.size)
        val fightClub = page.results.first()
        assertEquals(550L, fightClub.id)
        assertEquals("/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg", fightClub.posterPath)
        assertEquals("1999-10-15", fightClub.releaseDate)
        assertEquals(8.438, fightClub.voteAverage, 0.0001)
        assertNull(page.results.last().posterPath)

        val recorded = server.takeRequest()
        assertEquals("Bearer test-token", recorded.headers["Authorization"])
        assertTrue(recorded.target.startsWith("/movie/popular?page=1"))
    }

    @Test
    fun `given a 401, when calling through tmdbCall, then InvalidToken is returned`() = runTest {
        server.enqueue(response(401, fixture("error_401.json")))

        val result = tmdbCall { api.popularMovies() }

        assertEquals(AppError.InvalidToken, result.appErrorOrNull())
    }

    @Test
    fun `given a 404, when calling through tmdbCall, then NotFound is returned without retry`() = runTest {
        server.enqueue(response(404, """{"status_code":34,"status_message":"not found"}"""))

        val result = tmdbCall { api.popularMovies() }

        assertEquals(AppError.NotFound, result.appErrorOrNull())
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `given rate limiting then success, when calling through tmdbCall, then it retries and succeeds`() = runTest {
        server.enqueue(response(429, fixture("error_429.json")))
        server.enqueue(response(429, fixture("error_429.json")))
        server.enqueue(response(200, fixture("popular_page_1.json")))

        val result = tmdbCall { api.popularMovies() }

        assertTrue(result.isSuccess)
        assertEquals(3, server.requestCount)
    }

    @Test
    fun `given persistent rate limiting, when retries are exhausted, then RateLimited is returned`() = runTest {
        repeat(3) { server.enqueue(response(429, fixture("error_429.json"))) }

        val result = tmdbCall { api.popularMovies() }

        assertEquals(AppError.RateLimited, result.appErrorOrNull())
        assertEquals(3, server.requestCount)
    }

    @Test
    fun `given malformed json, when calling through tmdbCall, then Unknown is returned`() = runTest {
        server.enqueue(response(200, "{ this is not json"))

        val result = tmdbCall { api.popularMovies() }

        assertTrue(result.appErrorOrNull() is AppError.Unknown)
    }
}
