package com.example.tmdb.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tmdb.core.designsystem.theme.TMDBTheme

object StateTestTags {
    const val LOADING = "state_loading"
    const val ERROR = "state_error"
    const val ERROR_RETRY = "state_error_retry"
    const val EMPTY = "state_empty"
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(StateTestTags.LOADING),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag(StateTestTags.ERROR),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onRetryClick,
            modifier = Modifier.testTag(StateTestTags.ERROR_RETRY),
        ) {
            Text(text = "Retry")
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag(StateTestTags.EMPTY),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(name = "large font", showBackground = true, fontScale = 1.5f)
@Preview(name = "small width", showBackground = true, widthDp = 320)
@Composable
private fun ErrorStatePreview() {
    TMDBTheme {
        ErrorState(message = "Something went wrong. Check your connection.", onRetryClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingStatePreview() {
    TMDBTheme {
        LoadingState()
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    TMDBTheme {
        EmptyState(message = "Nothing here yet.")
    }
}
