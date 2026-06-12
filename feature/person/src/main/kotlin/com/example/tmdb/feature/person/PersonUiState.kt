package com.example.tmdb.feature.person

import androidx.compose.runtime.Immutable
import com.example.tmdb.domain.model.MediaType
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.Person
import com.example.tmdb.domain.model.PersonCredits
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class PersonUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val person: PersonUi? = null,
    val credits: ImmutableList<PersonCreditUi> = kotlinx.collections.immutable.persistentListOf(),
)

@Immutable
data class PersonUi(
    val id: Long,
    val name: String,
    val biography: String,
    val profileUrl: String?,
    val knownFor: String?,
    val birthInfo: String?,
)

@Immutable
data class PersonCreditUi(
    val id: Long,
    val title: String,
    val posterUrl: String?,
    val year: String?,
    val rating: Double,
    val mediaType: MediaType,
)

private const val PROFILE_BASE = "https://image.tmdb.org/t/p/w342"
private const val POSTER_BASE = "https://image.tmdb.org/t/p/w342"

internal fun Person.toUi(): PersonUi = PersonUi(
    id = id,
    name = name,
    biography = biography,
    profileUrl = profilePath?.let { PROFILE_BASE + it },
    knownFor = knownForDepartment,
    birthInfo = listOfNotNull(birthday?.toString(), placeOfBirth).joinToString(" · ").takeIf { it.isNotBlank() },
)

internal fun PersonCredits.toUi(): ImmutableList<PersonCreditUi> =
    combined.map { it.toCreditUi() }.toImmutableList()

private fun Movie.toCreditUi(): PersonCreditUi = PersonCreditUi(
    id = id.value,
    title = title,
    posterUrl = posterPath?.let { POSTER_BASE + it },
    year = releaseDate?.year?.toString(),
    rating = voteAverage,
    mediaType = mediaType,
)
