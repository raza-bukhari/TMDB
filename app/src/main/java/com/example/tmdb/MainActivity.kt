package com.example.tmdb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tmdb.core.designsystem.theme.TMDBTheme
import com.example.tmdb.feature.movies.MoviesScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TMDBTheme {
                // Placeholder entry point; the NavHost over :core:navigation routes lands in Milestone 1.
                MoviesScreen()
            }
        }
    }
}
