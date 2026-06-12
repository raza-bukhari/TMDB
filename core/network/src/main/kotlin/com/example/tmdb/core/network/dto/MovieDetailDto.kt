package com.example.tmdb.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenreDto(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String = "",
)

@Serializable
data class MovieDetailDto(
    @SerialName("id") val id: Long,
    @SerialName("title") val title: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("overview") val overview: String = "",
    @SerialName("tagline") val tagline: String? = null,
    @SerialName("imdb_id") val imdbId: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    @SerialName("runtime") val runtime: Int? = null,
    @SerialName("episode_run_time") val episodeRunTime: List<Int> = emptyList(),
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    @SerialName("genres") val genres: List<GenreDto> = emptyList(),
    @SerialName("credits") val credits: CreditsDto? = null,
    @SerialName("release_dates") val releaseDates: ReleaseDatesResponseDto? = null,
    @SerialName("similar") val similar: PagedResponseDto<MovieDto>? = null,
    @SerialName("watch/providers") val watchProviders: WatchProvidersResponseDto? = null,
)
