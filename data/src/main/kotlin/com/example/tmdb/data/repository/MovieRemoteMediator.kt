package com.example.tmdb.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.example.tmdb.core.database.MovieDao
import com.example.tmdb.core.database.MovieEntity
import com.example.tmdb.core.database.MovieRemoteKeysEntity
import com.example.tmdb.core.network.TmdbApi
import com.example.tmdb.core.network.dto.MovieDto
import com.example.tmdb.core.network.tmdbCall
import com.example.tmdb.data.mapper.toEntity
import com.example.tmdb.domain.model.MovieCategory

@OptIn(ExperimentalPagingApi::class)
internal class MovieRemoteMediator(
    private val category: MovieCategory,
    private val api: TmdbApi,
    private val dao: MovieDao,
) : RemoteMediator<Int, MovieEntity>() {

    private val categoryKey = category.name.lowercase()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MovieEntity>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextPage = remoteKeys?.nextPage
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                    nextPage
                }
            }

            val response = tmdbCall {
                when (category) {
                    MovieCategory.POPULAR -> api.popularMovies(page)
                    MovieCategory.TOP_RATED -> api.topRatedMovies(page)
                    MovieCategory.NOW_PLAYING -> api.nowPlayingMovies(page)
                }
            }.getOrThrow()

            val endOfPaginationReached = response.page >= response.totalPages

            val prevPage = if (page == 1) null else page - 1
            val nextPage = if (endOfPaginationReached) null else page + 1

            val entities = response.results.mapIndexed { index, dto ->
                dto.toEntity(categoryKey, (page - 1) * state.config.pageSize + index)
            }

            val keys = response.results.map {
                MovieRemoteKeysEntity(id = it.id, category = categoryKey, prevPage = prevPage, nextPage = nextPage)
            }

            if (loadType == LoadType.REFRESH) {
                dao.clearAndInsert(categoryKey, entities, keys)
            } else {
                dao.insertAll(entities)
                dao.insertRemoteKeys(keys)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (cancellation: kotlin.coroutines.cancellation.CancellationException) {
            throw cancellation
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, MovieEntity>): MovieRemoteKeysEntity? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { movie ->
            dao.getRemoteKey(movie.id, categoryKey)
        }
    }
}
