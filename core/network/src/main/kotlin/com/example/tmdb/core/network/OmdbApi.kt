package com.example.tmdb.core.network

import com.example.tmdb.core.network.dto.OmdbResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * OMDb (omdbapi.com) — the only practical source for IMDb and Rotten Tomatoes
 * scores; TMDB exposes neither. The `apikey` query param is appended by an
 * interceptor; [OmdbConfig.isConfigured] gates calls when no key is set.
 */
interface OmdbApi {

    @GET(".")
    suspend fun ratingsByImdbId(
        @Query("i") imdbId: String,
    ): OmdbResponseDto
}

object OmdbConfig {
    val isConfigured: Boolean get() = BuildConfig.OMDB_API_KEY.isNotBlank()
}
