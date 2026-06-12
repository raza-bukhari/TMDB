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

    @GET("movie/top_rated")
    suspend fun topRatedMovies(
        @Query("page") page: Int = 1,
    ): PagedResponseDto<MovieDto>

    @GET("movie/now_playing")
    suspend fun nowPlayingMovies(
        @Query("page") page: Int = 1,
    ): PagedResponseDto<MovieDto>

    @GET("movie/upcoming")
    suspend fun upcomingMovies(
        @Query("page") page: Int = 1,
    ): PagedResponseDto<MovieDto>

    @GET("trending/movie/{time_window}")
    suspend fun trendingMovies(
        @Path("time_window") timeWindow: String,
        @Query("page") page: Int = 1,
    ): PagedResponseDto<MovieDto>

    @GET("movie/{movie_id}")
    suspend fun movieDetail(
        @Path("movie_id") movieId: Long,
        @Query("append_to_response") appendToResponse: String = "credits,release_dates,similar,watch/providers",
    ): MovieDetailDto

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
    ): PagedResponseDto<MovieDto>
}
