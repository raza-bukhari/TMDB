package com.example.tmdb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tmdb.core.designsystem.theme.TMDBTheme
import com.example.tmdb.core.designsystem.theme.ThemeMode
import com.example.tmdb.core.navigation.MovieDetailRoute
import com.example.tmdb.core.navigation.MoviesRoute
import com.example.tmdb.core.navigation.SearchRoute
import com.example.tmdb.feature.detail.MovieDetailScreen
import com.example.tmdb.feature.movies.MoviesScreen
import com.example.tmdb.feature.search.SearchScreen

private const val NAV_ANIM_MS = 300

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // App-level theme preference; survives rotation. (Persistence via DataStore is future work.)
            var themeMode by rememberSaveable { mutableStateOf(ThemeMode.SYSTEM) }

            TMDBTheme(themeMode = themeMode) {
              // Themed background fills behind the transparent system bars (edge-to-edge).
              Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = MoviesRoute,
                    // Horizontal slide + fade between destinations.
                    enterTransition = {
                        slideInHorizontally(tween(NAV_ANIM_MS)) { it / 4 } + fadeIn(tween(NAV_ANIM_MS))
                    },
                    exitTransition = { fadeOut(tween(NAV_ANIM_MS)) },
                    popEnterTransition = { fadeIn(tween(NAV_ANIM_MS)) },
                    popExitTransition = {
                        slideOutHorizontally(tween(NAV_ANIM_MS)) { it / 4 } + fadeOut(tween(NAV_ANIM_MS))
                    },
                ) {
                    composable<MoviesRoute> {
                        MoviesScreen(
                            onMovieClick = { movieId -> navController.navigate(MovieDetailRoute(movieId)) },
                            onSearchClick = { navController.navigate(SearchRoute) },
                            themeMode = themeMode,
                            onToggleTheme = { themeMode = themeMode.next() },
                        )
                    }
                    composable<MovieDetailRoute> {
                        MovieDetailScreen(
                            onBackClick = { navController.popBackStack() },
                            onMovieClick = { movieId -> navController.navigate(MovieDetailRoute(movieId)) }
                        )
                    }
                    composable<SearchRoute> {
                        SearchScreen(
                            onMovieClick = { movieId -> navController.navigate(MovieDetailRoute(movieId)) },
                            onBackClick = { navController.popBackStack() },
                        )
                    }
                }
              }
            }
        }
    }
}
