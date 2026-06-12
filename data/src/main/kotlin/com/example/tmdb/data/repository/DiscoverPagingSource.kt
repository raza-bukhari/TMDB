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
            val response = tmdbCall {
                when (filters.mediaType) {
                    MediaType.MOVIE -> api.discoverMovies(
                        page = page,
                        sortBy = filters.sortBy.apiValue,
                        voteAverageGte = filters.minRating.takeIf { it > 0.0 },
                        releaseDateGte = filters.fromYear?.let { "$it-01-01" },
                        releaseDateLte = filters.toYear?.let { "$it-12-31" },
                        withGenres = filters.genres
                            .takeIf { it.isNotEmpty() }
                            ?.joinToString(separator = "|") { it.id.toString() },
                    )
                    MediaType.TV -> api.discoverTv(
                        page = page,
                        sortBy = filters.sortBy.tvApiValue,
                        voteAverageGte = filters.minRating.takeIf { it > 0.0 },
                        firstAirDateGte = filters.fromYear?.let { "$it-01-01" },
                        firstAirDateLte = filters.toYear?.let { "$it-12-31" },
                        withGenres = filters.genres
                            .takeIf { it.isNotEmpty() }
                            ?.joinToString(separator = "|") { it.id.toString() },
                    )
                }
            }.getOrThrow()
            LoadResult.Page(
                data = response.results.map { it.toDomain().copy(mediaType = filters.mediaType) },
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.page >= response.totalPages) null else response.page + 1,
            )
        } catch (cancellation: kotlin.coroutines.cancellation.CancellationException) {
            throw cancellation
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

private val DiscoverMovieSort.tvApiValue: String
    get() = when (this) {
        DiscoverMovieSort.POPULARITY_DESC -> "popularity.desc"
        DiscoverMovieSort.RATING_DESC -> "vote_average.desc"
        DiscoverMovieSort.RELEASE_DESC -> "first_air_date.desc"
        DiscoverMovieSort.RELEASE_ASC -> "first_air_date.asc"
        DiscoverMovieSort.TITLE_ASC -> "name.asc"
    }
