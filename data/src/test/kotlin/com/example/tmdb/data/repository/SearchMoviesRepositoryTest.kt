package com.example.tmdb.data.repository

import androidx.paging.PagingSource
import com.example.tmdb.core.network.TmdbApi
import com.example.tmdb.domain.model.DiscoverMovieFilters
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.MediaType
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.asAppError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
class SearchMoviesRepositoryTest {

    private val server = MockWebServer()

    private fun fixture(name: String): String =
        checkNotNull(javaClass.classLoader?.getResource("fixtures/$name")) { "missing fixture $name" }.readText()

    private val api: TmdbApi by lazy {
        server.start()
        Retrofit.Builder()
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

    @Test
    fun `given a results payload, when searching, then domain movies and paging keys map`() = runTest {
        server.enqueue(MockResponse.Builder().code(200).body(fixture("search_movie_inception.json")).build())

        val result = SearchPagingSource(api, "inception").load(
            PagingSource.LoadParams.Refresh(key = 1, loadSize = 20, placeholdersEnabled = false),
        ) as PagingSource.LoadResult.Page

        assertEquals(MovieId(27205), result.data.first().id)
        assertEquals("Inception", result.data.first().title)
        assertEquals(null, result.prevKey)
        assertEquals(2, result.nextKey)
    }

    @Test
    fun `given a 429, when searching, then the error is RateLimited`() = runTest {
        repeat(3) {
            server.enqueue(
                MockResponse.Builder().code(429).body("""{"status_code":25,"status_message":"rate limited"}""").build(),
            )
        }

        val result = SearchPagingSource(api, "anything").load(
            PagingSource.LoadParams.Refresh(key = 1, loadSize = 20, placeholdersEnabled = false),
        ) as PagingSource.LoadResult.Error

        assertEquals(AppError.RateLimited, result.throwable.asAppError())
    }

    @Test
    fun `given default discover filters, when loading, then movies and series are returned`() = runTest {
        server.enqueue(MockResponse.Builder().code(200).body(fixture("popular_page_1.json")).build())
        server.enqueue(
            MockResponse.Builder().code(200).body(
                """
                {
                  "page": 1,
                  "results": [
                    {
                      "backdrop_path": "/tv-backdrop.jpg",
                      "first_air_date": "2008-01-20",
                      "genre_ids": [18],
                      "id": 1396,
                      "name": "Breaking Bad",
                      "overview": "A chemistry teacher changes lanes.",
                      "poster_path": "/tv-poster.jpg",
                      "vote_average": 8.9,
                      "vote_count": 15000
                    }
                  ],
                  "total_pages": 1,
                  "total_results": 1
                }
                """.trimIndent(),
            ).build(),
        )

        val result = DiscoverPagingSource(api, DiscoverMovieFilters()).load(
            PagingSource.LoadParams.Refresh(key = 1, loadSize = 20, placeholdersEnabled = false),
        ) as PagingSource.LoadResult.Page

        assertEquals(listOf(MediaType.MOVIE, MediaType.MOVIE, MediaType.MOVIE, MediaType.TV), result.data.map { it.mediaType })
        assertEquals("Breaking Bad", result.data.last().title)
        assertEquals("/discover/movie", server.takeRequest().url.encodedPath)
        assertEquals("/discover/tv", server.takeRequest().url.encodedPath)
    }
}
