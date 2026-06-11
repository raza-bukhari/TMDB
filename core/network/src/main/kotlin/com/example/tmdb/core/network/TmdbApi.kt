package com.example.tmdb.core.network

import com.example.tmdb.core.network.dto.MovieDetailDto
import com.example.tmdb.core.network.dto.MovieDto
import com.example.tmdb.core.network.dto.PagedResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {

    @GET("movie/popular")
    suspend fun popularMovies(
        @Query("page") page: Int = 1,
    ): PagedResponseDto<MovieDto>

    @GET("movie/{movie_id}")
    suspend fun movieDetail(
        @Path("movie_id") movieId: Long,
    ): MovieDetailDto
}
