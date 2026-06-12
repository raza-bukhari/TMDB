package com.example.tmdb.core.network

import com.example.tmdb.core.network.dto.MovieDetailDto
import com.example.tmdb.core.network.dto.MovieDto
import com.example.tmdb.core.network.dto.PagedResponseDto
import com.example.tmdb.core.network.dto.PersonCombinedCreditsDto
import com.example.tmdb.core.network.dto.PersonDto
import com.example.tmdb.core.network.dto.TvEpisodeDto
import com.example.tmdb.core.network.dto.TvSeasonDto
import com.example.tmdb.core.network.dto.VideosResponseDto
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

    @GET("tv/{series_id}")
    suspend fun tvDetail(
        @Path("series_id") seriesId: Long,
        @Query("append_to_response") appendToResponse: String = "credits,content_ratings,similar,watch/providers",
    ): MovieDetailDto

    @GET("movie/{movie_id}/videos")
    suspend fun movieVideos(
        @Path("movie_id") movieId: Long,
    ): VideosResponseDto

    @GET("tv/{series_id}/videos")
    suspend fun tvVideos(
        @Path("series_id") seriesId: Long,
    ): VideosResponseDto

    @GET("tv/{series_id}/season/{season_number}")
    suspend fun tvSeason(
        @Path("series_id") seriesId: Long,
        @Path("season_number") seasonNumber: Int,
    ): TvSeasonDto

    @GET("tv/{series_id}/season/{season_number}/episode/{episode_number}")
    suspend fun tvEpisode(
        @Path("series_id") seriesId: Long,
        @Path("season_number") seasonNumber: Int,
        @Path("episode_number") episodeNumber: Int,
    ): TvEpisodeDto

    @GET("person/{person_id}")
    suspend fun personDetail(
        @Path("person_id") personId: Long,
    ): PersonDto

    @GET("person/{person_id}/combined_credits")
    suspend fun personCombinedCredits(
        @Path("person_id") personId: Long,
    ): PersonCombinedCreditsDto

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
    ): PagedResponseDto<MovieDto>

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false,
    ): PagedResponseDto<MovieDto>

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("vote_average.gte") voteAverageGte: Double? = null,
        @Query("primary_release_date.gte") releaseDateGte: String? = null,
        @Query("primary_release_date.lte") releaseDateLte: String? = null,
        @Query("with_genres") withGenres: String? = null,
        @Query("with_original_language") withOriginalLanguage: String? = null,
        @Query("with_runtime.gte") runtimeGte: Int? = null,
        @Query("with_runtime.lte") runtimeLte: Int? = null,
        @Query("with_watch_providers") watchProviderId: Int? = null,
        @Query("watch_region") watchRegion: String? = null,
        @Query("with_watch_monetization_types") watchMonetizationTypes: String? = null,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("include_video") includeVideo: Boolean = false,
    ): PagedResponseDto<MovieDto>

    @GET("discover/tv")
    suspend fun discoverTv(
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("vote_average.gte") voteAverageGte: Double? = null,
        @Query("first_air_date.gte") firstAirDateGte: String? = null,
        @Query("first_air_date.lte") firstAirDateLte: String? = null,
        @Query("with_genres") withGenres: String? = null,
        @Query("with_original_language") withOriginalLanguage: String? = null,
        @Query("with_runtime.gte") runtimeGte: Int? = null,
        @Query("with_runtime.lte") runtimeLte: Int? = null,
        @Query("with_watch_providers") watchProviderId: Int? = null,
        @Query("watch_region") watchRegion: String? = null,
        @Query("with_watch_monetization_types") watchMonetizationTypes: String? = null,
        @Query("include_adult") includeAdult: Boolean = false,
    ): PagedResponseDto<MovieDto>
}
