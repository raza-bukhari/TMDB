package com.example.tmdb.data.mapper

import com.example.tmdb.core.database.MovieEntity
import com.example.tmdb.core.network.dto.MovieDto
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieId
import java.time.LocalDate

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

internal fun MovieEntity.toDomain(): Movie = Movie(
    id = MovieId(id),
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    // TMDB sends "" for unknown dates; anything unparseable degrades to null rather than crashing.
    releaseDate = releaseDate
        ?.takeIf { it.isNotBlank() }
        ?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
    voteAverage = voteAverage,
    voteCount = voteCount,
)
