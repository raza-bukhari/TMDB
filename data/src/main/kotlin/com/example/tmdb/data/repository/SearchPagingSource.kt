package com.example.tmdb.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.tmdb.core.network.TmdbApi
import com.example.tmdb.core.network.tmdbCall
import com.example.tmdb.data.mapper.toDomain
import com.example.tmdb.domain.model.Movie

internal class SearchPagingSource(
    private val api: TmdbApi,
    private val query: String,
) : PagingSource<Int, Movie>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: 1
        return try {
            val response = tmdbCall { api.searchMulti(query, page) }.getOrThrow()
            LoadResult.Page(
                data = response.results
                    .filter { it.mediaType == null || it.mediaType == "movie" || it.mediaType == "tv" }
                    .map { it.toDomain() },
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.page >= response.totalPages) null else response.page + 1
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
