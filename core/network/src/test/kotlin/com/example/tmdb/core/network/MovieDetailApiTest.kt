package com.example.tmdb.core.network

import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.appErrorOrNull
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class MovieDetailApiTest {

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
    fun `given a 200 detail payload, when fetching, then nested genres and runtime map`() = runTest {
        server.enqueue(MockResponse.Builder().code(200).body(fixture("movie_detail_550.json")).build())

        val detail = api.movieDetail(550)

        assertEquals(550L, detail.id)
        assertEquals("Mischief. Mayhem. Soap.", detail.tagline)
        assertEquals(139, detail.runtime)
        assertEquals(listOf("Drama", "Thriller", "Comedy"), detail.genres.map { it.name })
        assertEquals("/movie/550", server.takeRequest().target)
    }

    @Test
    fun `given a 404 for an unknown id, when calling through tmdbCall, then NotFound is returned`() = runTest {
        server.enqueue(
            MockResponse.Builder()
                .code(404)
                .body("""{"success":false,"status_code":34,"status_message":"The resource you requested could not be found."}""")
                .build(),
        )

        val result = tmdbCall { api.movieDetail(999_999_999) }

        assertEquals(AppError.NotFound, result.appErrorOrNull())
    }
}
