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

class SearchMoviesApiTest {

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
    fun `given a results payload, when searching, then page fields and items parse`() = runTest {
        server.enqueue(MockResponse.Builder().code(200).body(fixture("search_movie_inception.json")).build())

        val page = api.searchMovies(query = "inception", page = 1)

        assertEquals(1, page.page)
        assertEquals(3, page.totalPages)
        assertEquals(listOf("Inception", "Inception: The Cobol Job"), page.results.map { it.title })
    }

    @Test
    fun `given a query with spaces, when searching, then it is url-encoded`() = runTest {
        server.enqueue(MockResponse.Builder().code(200).body(fixture("search_movie_empty.json")).build())

        api.searchMovies(query = "the dark knight", page = 2)

        val target = server.takeRequest().target
        assertTrue("unexpected target: $target", target.contains("query=the%20dark%20knight"))
        assertTrue("unexpected target: $target", target.contains("page=2"))
    }

    @Test
    fun `given an empty result set, when searching, then results are empty not null`() = runTest {
        server.enqueue(MockResponse.Builder().code(200).body(fixture("search_movie_empty.json")).build())

        val page = api.searchMovies(query = "zzzzz")

        assertEquals(0, page.results.size)
        assertEquals(1, page.totalPages)
    }
}
