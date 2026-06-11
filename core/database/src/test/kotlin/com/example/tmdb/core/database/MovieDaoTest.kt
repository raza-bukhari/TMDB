package com.example.tmdb.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MovieDaoTest {

    private lateinit var db: TmdbDatabase
    private lateinit var dao: MovieDao

    private fun entity(id: Long, order: Int, category: String = MovieCategories.POPULAR) = MovieEntity(
        id = id,
        category = category,
        orderIndex = order,
        title = "Movie $id",
        overview = "",
        posterPath = null,
        backdropPath = null,
        releaseDate = "1999-10-15",
        voteAverage = 7.0,
        voteCount = 100,
    )

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TmdbDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.movieDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `given inserted movies, when observing a category, then they emit ordered by orderIndex`() = runTest {
        dao.insertAll(listOf(entity(2, order = 1), entity(1, order = 0)))

        val movies = dao.observeByCategory(MovieCategories.POPULAR).first()

        assertEquals(listOf(1L, 2L), movies.map { it.id })
    }

    @Test
    fun `given a cached category, when replacing it, then stale rows are gone and order is new`() = runTest {
        dao.insertAll(listOf(entity(1, order = 0), entity(2, order = 1)))

        dao.replaceCategory(MovieCategories.POPULAR, listOf(entity(3, order = 0), entity(1, order = 1)))

        val movies = dao.observeByCategory(MovieCategories.POPULAR).first()
        assertEquals(listOf(3L, 1L), movies.map { it.id })
    }

    @Test
    fun `given two categories, when replacing one, then the other is untouched`() = runTest {
        dao.insertAll(listOf(entity(1, order = 0), entity(1, order = 0, category = "top_rated")))

        dao.replaceCategory(MovieCategories.POPULAR, emptyList())

        assertEquals(0, dao.observeByCategory(MovieCategories.POPULAR).first().size)
        assertEquals(1, dao.observeByCategory("top_rated").first().size)
    }
}
