package com.example.tmdb.data.mapper

import com.example.tmdb.core.database.MovieDetailEntity
import com.example.tmdb.core.database.MovieEntity
import com.example.tmdb.core.network.dto.MovieDetailDto
import com.example.tmdb.core.network.dto.MovieDto
import com.example.tmdb.core.network.dto.PagedResponseDto
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.SearchResults
import java.time.LocalDate

private const val GENRE_SEPARATOR = "|"

internal fun MovieDto.toEntity(category: String, orderIndex: Int): MovieEntity = MovieEntity(
    id = id,
    category = category,
    orderIndex = orderIndex,
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
)

internal fun MovieDetailDto.toEntity(): MovieDetailEntity = MovieDetailEntity(
    id = id,
    title = title,
    overview = overview,
    tagline = tagline?.takeIf { it.isNotBlank() },
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    runtimeMinutes = runtime,
    voteAverage = voteAverage,
    voteCount = voteCount,
    genres = genres.map { it.name }.filter { it.isNotBlank() }.joinToString(GENRE_SEPARATOR),
)

internal fun MovieDetailEntity.toDomain(): MovieDetail = MovieDetail(
    id = MovieId(id),
    title = title,
    overview = overview,
    tagline = tagline,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = parseDate(releaseDate),
    runtimeMinutes = runtimeMinutes,
    voteAverage = voteAverage,
    voteCount = voteCount,
    genres = genres.split(GENRE_SEPARATOR).filter { it.isNotBlank() },
)

// Search results skip Room entirely (D-006), so DTOs map straight to domain.
internal fun MovieDto.toDomain(): Movie = Movie(
    id = MovieId(id),
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = parseDate(releaseDate),
    voteAverage = voteAverage,
    voteCount = voteCount,
)

internal fun PagedResponseDto<MovieDto>.toSearchResults(): SearchResults = SearchResults(
    movies = results.map { it.toDomain() },
    page = page,
    totalPages = totalPages,
)

internal fun MovieEntity.toDomain(): Movie = Movie(
    id = MovieId(id),
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = parseDate(releaseDate),
    voteAverage = voteAverage,
    voteCount = voteCount,
)

// TMDB sends "" for unknown dates; anything unparseable degrades to null rather than crashing.
private fun parseDate(raw: String?): LocalDate? =
    raw?.takeIf { it.isNotBlank() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
