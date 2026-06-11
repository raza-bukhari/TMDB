package com.example.tmdb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tmdb.core.designsystem.theme.TMDBTheme
import com.example.tmdb.core.navigation.MoviesRoute
import com.example.tmdb.feature.movies.MoviesScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TMDBTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = MoviesRoute) {
                    composable<MoviesRoute> {
                        MoviesScreen(
                            // Detail destination lands in Milestone 2.
                            onMovieClick = {},
                        )
                    }
                }
            }
        }
    }
}
