package com.example.tmdb.feature.movies

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview

/**
 * Milestone 0 placeholder. Replaced by the stateful Popular Movies screen
 * (UiState + ViewModel) in Milestone 1.
 */
@Composable
fun MoviesScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("movies_screen"),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "TMDB Movies",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MoviesScreenPreview() {
    MoviesScreen()
}
