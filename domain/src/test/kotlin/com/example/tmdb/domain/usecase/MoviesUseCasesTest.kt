package com.example.tmdb.domain.usecase

import com.example.tmdb.domain.FakeRepo
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.AppException
import com.example.tmdb.domain.model.ExternalRatings
import com.example.tmdb.domain.model.HomeList
import com.example.tmdb.domain.model.Movie
import com.example.tmdb.domain.model.MovieCategory
import com.example.tmdb.domain.model.MovieId
import com.example.tmdb.domain.model.appErrorOrNull
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private val aMovie = Movie(
    id = MovieId(550),
    title = "Fight Club",
    overview = "An insomniac office worker…",
    posterPath = "/poster.jpg",
    backdropPath = null,
    releaseDate = LocalDate.of(1999, 10, 15),
    voteAverage = 8.4,
    voteCount = 26280,
)

class MoviesUseCasesTest {

    @Test
    fun `given cached movies, when observing a category, then category passes through`() = runTest {
        val repo = FakeRepo().apply { movies.value = listOf(aMovie) }

        ObserveMoviesUseCase(repo)(MovieCategory.TOP_RATED).first()

        assertEquals(MovieCategory.TOP_RATED, repo.lastCategory)
    }

    @Test
    fun `given a home list, when invoked, then list passes through and movies return`() = runTest {
        val repo = FakeRepo().apply { homeResult = Result.success(listOf(aMovie)) }

        val movies = GetHomeListUseCase(repo)(HomeList.TRENDING_THIS_WEEK).getOrThrow()

        assertEquals(listOf(aMovie), movies)
        assertEquals(HomeList.TRENDING_THIS_WEEK, repo.lastHomeList)
    }

    @Test
    fun `given a rate-limited home list, when invoked, then typed error surfaces`() = runTest {
        val repo = FakeRepo().apply { homeResult = Result.failure(AppException(AppError.RateLimited)) }

        assertEquals(AppError.RateLimited, GetHomeListUseCase(repo)(HomeList.POPULAR).appErrorOrNull())
    }

    @Test
    fun `given an imdb id, when external ratings load, then id passes through and ratings return`() = runTest {
        val ratings = ExternalRatings(imdb = "8.8", rottenTomatoes = "81%")
        val repo = FakeRepo().apply { externalRatingsResult = Result.success(ratings) }

        assertEquals(ratings, GetExternalRatingsUseCase(repo)("tt0137523").getOrThrow())
        assertEquals("tt0137523", repo.lastImdbId)
    }
}
