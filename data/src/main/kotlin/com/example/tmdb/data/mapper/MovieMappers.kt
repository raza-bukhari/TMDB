package com.example.tmdb.data.mapper

import com.example.tmdb.core.database.MovieDetailEntity
import com.example.tmdb.core.database.MovieEntity
import com.example.tmdb.core.database.WatchlistMovieEntity
import com.example.tmdb.core.network.dto.CastMemberDto
import com.example.tmdb.core.network.dto.CrewMemberDto
import com.example.tmdb.core.network.dto.MovieDetailDto
import com.example.tmdb.core.network.dto.MovieDto
import com.example.tmdb.core.network.dto.OmdbResponseDto
import com.example.tmdb.core.network.dto.PagedResponseDto
import com.example.tmdb.core.network.dto.WatchProviderDto
import com.example.tmdb.domain.model.CastMember
import com.example.tmdb.domain.model.CrewMember
import com.example.tmdb.domain.model.ExternalRatings
import com.example.tmdb.domain.model.MediaType
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieDetail
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.SearchResults
import com.example.tmdb.domain.model.WatchProvider
import java.time.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val GENRE_SEPARATOR = "|"

private const val ID_SEPARATOR = ","

private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

internal fun MovieDto.toEntity(category: String, orderIndex: Int): MovieEntity = MovieEntity(
    id = id,
    category = category,
    orderIndex = orderIndex,
    title = displayTitle,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = displayDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
    genreIds = genreIds.joinToString(ID_SEPARATOR),
)

internal fun MovieDetailDto.toEntity(): MovieDetailEntity = MovieDetailEntity(
    id = id,
    title = displayTitle,
    overview = overview,
    tagline = tagline?.takeIf { it.isNotBlank() },
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = displayDate,
    runtimeMinutes = runtime ?: episodeRunTime.firstOrNull(),
    voteAverage = voteAverage,
    voteCount = voteCount,
    genres = genres.map { it.name }.filter { it.isNotBlank() }.joinToString(GENRE_SEPARATOR),
    castJson = credits?.cast?.take(10)?.let { json.encodeToString(it) },
    crewJson = credits?.crew?.let { json.encodeToString(it) },
    similarMoviesJson = similar?.results?.take(10)?.let { json.encodeToString(it) },
    providersJson = watchProviders?.results?.get("US")?.flatrate?.let { json.encodeToString(it) },
    certification = releaseDates?.results?.find { it.iso31661 == "US" }?.releaseDates?.find { it.certification.isNotBlank() }?.certification,
    imdbId = imdbId?.takeIf { it.isNotBlank() },
)

internal fun MovieDetailEntity.toDomain(): MovieDetail {
    val castDtos = castJson?.let { runCatching { json.decodeFromString<List<CastMemberDto>>(it) }.getOrNull() } ?: emptyList()
    val crewDtos = crewJson?.let { runCatching { json.decodeFromString<List<CrewMemberDto>>(it) }.getOrNull() } ?: emptyList()
    val similarDtos = similarMoviesJson?.let { runCatching { json.decodeFromString<List<MovieDto>>(it) }.getOrNull() } ?: emptyList()
    val providerDtos = providersJson?.let { runCatching { json.decodeFromString<List<WatchProviderDto>>(it) }.getOrNull() } ?: emptyList()

    return MovieDetail(
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
        cast = castDtos.map { it.toDomain() },
        directors = crewDtos.filter { it.job == "Director" }.map { it.name },
        producers = crewDtos.filter { it.job == "Producer" || it.job == "Executive Producer" }.map { it.name },
        certification = certification,
        similarMovies = similarDtos.map { it.toDomain() },
        watchProviders = providerDtos.map { it.toDomain() },
        imdbId = imdbId,
    )
}

internal fun OmdbResponseDto.toDomain(): ExternalRatings {
    fun String?.clean(): String? = this?.takeIf { it.isNotBlank() && it != "N/A" }

    return ExternalRatings(
        imdb = imdbRating.clean(),
        rottenTomatoes = ratings.firstOrNull { it.source == "Rotten Tomatoes" }?.value.clean(),
        metascore = metascore.clean(),
    )
}

internal fun CastMemberDto.toDomain(): CastMember = CastMember(
    id = id,
    name = name,
    character = character,
    profilePath = profilePath,
    order = order
)

internal fun WatchProviderDto.toDomain(): WatchProvider = WatchProvider(
    id = providerId,
    name = providerName,
    logoPath = logoPath
)

// Search results skip Room entirely (D-006), so DTOs map straight to domain.
internal fun MovieDto.toDomain(): Movie = Movie(
    id = MovieId(id),
    title = displayTitle,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = parseDate(displayDate),
    voteAverage = voteAverage,
    voteCount = voteCount,
    genreIds = genreIds,
    mediaType = when (mediaType) {
        "tv" -> MediaType.TV
        else -> MediaType.MOVIE
    },
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
    genreIds = genreIds.split(ID_SEPARATOR).mapNotNull { it.trim().toIntOrNull() },
)

internal fun WatchlistMovieEntity.toDomain(): Movie = Movie(
    id = MovieId(id),
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = parseDate(releaseDate),
    voteAverage = voteAverage,
    voteCount = voteCount,
)

internal fun Movie.toWatchlistEntity(addedAtMillis: Long): WatchlistMovieEntity = WatchlistMovieEntity(
    id = id.value,
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate?.toString(),
    voteAverage = voteAverage,
    voteCount = voteCount,
    addedAtMillis = addedAtMillis,
)

internal fun MovieDetail.toWatchlistEntity(addedAtMillis: Long): WatchlistMovieEntity = WatchlistMovieEntity(
    id = id.value,
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate?.toString(),
    voteAverage = voteAverage,
    voteCount = voteCount,
    addedAtMillis = addedAtMillis,
)

private val MovieDto.displayTitle: String
    get() = title.ifBlank { name }

private val MovieDto.displayDate: String?
    get() = releaseDate ?: firstAirDate

private val MovieDetailDto.displayTitle: String
    get() = title.ifBlank { name }

private val MovieDetailDto.displayDate: String?
    get() = releaseDate ?: firstAirDate

// TMDB sends "" for unknown dates; anything unparseable degrades to null rather than crashing.
private fun parseDate(raw: String?): LocalDate? =
    raw?.takeIf { it.isNotBlank() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
