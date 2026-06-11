package com.example.tmdb.core.network

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
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class CategoryMoviesApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: TmdbApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(
                Json { ignoreUnknownKeys = true; coerceInputValues = true }
                    .asConverterFactory("application/json".toMediaType()),
            )
            .build()
            .create(TmdbApi::class.java)
    }

    @After
    fun tearDown() {
        server.close()
    }

    private fun fixture(name: String): String =
        checkNotNull(javaClass.classLoader?.getResource("fixtures/$name")) { "missing fixture $name" }.readText()

    @Test
    fun `given a page number, when fetching top rated, then the page query and endpoint are correct`() = runTest {
        server.enqueue(MockResponse.Builder().code(200).body(fixture("popular_page_1.json")).build())

        api.topRatedMovies(page = 3)

        assertTrue(server.takeRequest().target.startsWith("/movie/top_rated?page=3"))
    }

    @Test
    fun `given a now-playing payload with an extra dates block, when fetching, then unknown keys are ignored`() = runTest {
        server.enqueue(MockResponse.Builder().code(200).body(fixture("now_playing_page_1.json")).build())

        val page = api.nowPlayingMovies(page = 2)

        assertEquals(2, page.page)
        assertEquals(42, page.totalPages)
        assertEquals("Kung Fu Panda 4", page.results.single().title)
        assertTrue(server.takeRequest().target.startsWith("/movie/now_playing?page=2"))
    }
}
