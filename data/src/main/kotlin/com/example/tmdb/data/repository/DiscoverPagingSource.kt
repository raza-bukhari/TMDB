package com.example.tmdb.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.tmdb.core.network.TmdbApi
import com.example.tmdb.core.network.tmdbCall
import com.example.tmdb.data.mapper.toDomain
import com.example.tmdb.domain.model.DiscoverMovieFilters
import com.example.tmdb.domain.model.DiscoverMovieSort
import com.example.tmdb.domain.model.MediaType
import com.example.tmdb.domain.model.Movie

internal class DiscoverPagingSource(
    private val api: TmdbApi,
    private val filters: DiscoverMovieFilters,
) : PagingSource<Int, Movie>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: 1
        return try {
            val response = when (filters.mediaType) {
                MediaType.MOVIE -> tmdbCall { moviePage(page) }.getOrThrow().toDomainPage(MediaType.MOVIE)
                MediaType.TV -> tmdbCall { tvPage(page) }.getOrThrow().toDomainPage(MediaType.TV)
                null -> {
                    val movies = tmdbCall { moviePage(page) }.getOrThrow().toDomainPage(MediaType.MOVIE)
                    val series = tmdbCall { tvPage(page) }.getOrThrow().toDomainPage(MediaType.TV)
                    DiscoverPage(
                        page = page,
                        results = movies.results + series.results,
                        totalPages = maxOf(movies.totalPages, series.totalPages),
                    )
                }
            }
            LoadResult.Page(
                data = response.results,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (page >= response.totalPages) null else page + 1,
            )
        } catch (cancellation: kotlin.coroutines.cancellation.CancellationException) {
            throw cancellation
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun moviePage(page: Int) = api.discoverMovies(
        page = page,
        sortBy = filters.sortBy.apiValue,
        voteAverageGte = filters.minRating.takeIf { it > 0.0 },
        releaseDateGte = filters.fromYear?.let { "$it-01-01" },
        releaseDateLte = filters.toYear?.let { "$it-12-31" },
        withGenres = filters.genres
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = "|") { it.id.toString() },
        withOriginalLanguage = filters.language,
        runtimeGte = filters.runtimeGte,
        runtimeLte = filters.runtimeLte,
        watchProviderId = filters.watchProviderId,
        watchRegion = filters.watchProviderId?.let { filters.watchRegion },
        watchMonetizationTypes = filters.watchMonetizationTypes,
    )

    private suspend fun tvPage(page: Int) = api.discoverTv(
        page = page,
        sortBy = filters.sortBy.tvApiValue,
        voteAverageGte = filters.minRating.takeIf { it > 0.0 },
        firstAirDateGte = filters.fromYear?.let { "$it-01-01" },
        firstAirDateLte = filters.toYear?.let { "$it-12-31" },
        withGenres = filters.genres
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = "|") { it.id.toString() },
        withOriginalLanguage = filters.language,
        runtimeGte = filters.runtimeGte,
        runtimeLte = filters.runtimeLte,
        watchProviderId = filters.watchProviderId,
        watchRegion = filters.watchProviderId?.let { filters.watchRegion },
        watchMonetizationTypes = filters.watchMonetizationTypes,
    )

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

private data class DiscoverPage(
    val page: Int,
    val results: List<Movie>,
    val totalPages: Int,
)

private fun com.example.tmdb.core.network.dto.PagedResponseDto<com.example.tmdb.core.network.dto.MovieDto>.toDomainPage(
    mediaType: MediaType,
): DiscoverPage = DiscoverPage(
    page = page,
    results = results.map { it.toDomain().copy(mediaType = mediaType) },
    totalPages = totalPages,
)

private val DiscoverMovieSort.tvApiValue: String
    get() = when (this) {
        DiscoverMovieSort.POPULARITY_DESC -> "popularity.desc"
        DiscoverMovieSort.RATING_DESC -> "vote_average.desc"
        DiscoverMovieSort.RELEASE_DESC -> "first_air_date.desc"
        DiscoverMovieSort.RELEASE_ASC -> "first_air_date.asc"
        DiscoverMovieSort.TITLE_ASC -> "name.asc"
    }
