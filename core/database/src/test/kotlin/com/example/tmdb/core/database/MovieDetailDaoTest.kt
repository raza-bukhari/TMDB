package com.example.tmdb.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MovieDetailDaoTest {

    private lateinit var db: TmdbDatabase
    private lateinit var dao: MovieDetailDao

    private fun detail(id: Long, title: String = "Movie $id") = MovieDetailEntity(
        id = id,
        title = title,
        overview = "",
        tagline = null,
        posterPath = null,
        backdropPath = null,
        releaseDate = "1999-10-15",
        runtimeMinutes = 139,
        voteAverage = 8.4,
        voteCount = 100,
        genres = "Drama|Thriller",
    )

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TmdbDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.movieDetailDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `given an empty table, when observing an id, then null emits`() = runTest {
        assertNull(dao.observeById(550).first())
    }

    @Test
    fun `given an upserted detail, when observing its id, then it emits with fields intact`() = runTest {
        dao.upsert(detail(550, "Fight Club"))

        val loaded = dao.observeById(550).first()

        assertEquals("Fight Club", loaded?.title)
        assertEquals("Drama|Thriller", loaded?.genres)
    }

    @Test
    fun `given an existing detail, when upserting the same id, then it is replaced not duplicated`() = runTest {
        dao.upsert(detail(550, "Old Title"))
        dao.upsert(detail(550, "New Title"))

        assertEquals("New Title", dao.observeById(550).first()?.title)
    }
}
