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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tmdb.core.designsystem.theme.AppTheme
import com.example.tmdb.core.designsystem.theme.TMDBTheme
import com.example.tmdb.core.navigation.MovieDetailRoute
import kotlinx.coroutines.launch
import com.example.tmdb.core.navigation.MoviesRoute
import com.example.tmdb.core.navigation.PersonRoute
import com.example.tmdb.core.navigation.VideoPlayerRoute
import com.example.tmdb.domain.model.MediaType
import com.example.tmdb.feature.detail.MovieDetailScreen
import com.example.tmdb.feature.movies.MoviesScreen
import com.example.tmdb.feature.person.PersonScreen
import com.example.tmdb.feature.videoplayer.VideoPlayerScreen

private const val NAV_ANIM_MS = 300

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // App-level theme preference, persisted across launches via DataStore.
            val context = LocalContext.current
            val themePreferences = remember { ThemePreferences(context) }
            val scope = rememberCoroutineScope()
            val appTheme by themePreferences.theme.collectAsStateWithLifecycle(initialValue = AppTheme.SYSTEM)

            TMDBTheme(appTheme = appTheme) {
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
                            onMovieClick = { movieId, mediaType ->
                                navController.navigate(MovieDetailRoute(movieId, mediaType.name))
                            },
                            selectedTheme = appTheme,
                            onThemeSelected = { theme -> scope.launch { themePreferences.setTheme(theme) } },
                        )
                    }
                    composable<MovieDetailRoute> {
                        MovieDetailScreen(
                            onBackClick = { navController.popBackStack() },
                            onMovieClick = { movieId -> navController.navigate(MovieDetailRoute(movieId, MediaType.MOVIE.name)) },
                            onPersonClick = { personId -> navController.navigate(PersonRoute(personId)) },
                            onVideoClick = { movieId, mediaType, startKey ->
                                navController.navigate(VideoPlayerRoute(movieId, mediaType, startKey))
                            },
                        )
                    }
                    composable<VideoPlayerRoute> {
                        VideoPlayerScreen(onBackClick = { navController.popBackStack() })
                    }
                    composable<PersonRoute> {
                        PersonScreen(
                            onBackClick = { navController.popBackStack() },
                            onMediaClick = { mediaId, mediaType ->
                                navController.navigate(MovieDetailRoute(mediaId, mediaType.name))
                            },
                        )
                    }
                }
              }
            }
        }
    }
}
