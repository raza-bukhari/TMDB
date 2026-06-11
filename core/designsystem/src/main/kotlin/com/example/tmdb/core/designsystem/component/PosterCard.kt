package com.example.tmdb.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tmdb.core.designsystem.theme.TMDBTheme

/**
 * Movie poster card chrome. Image loading is supplied via [poster] so this
 * module stays free of network/image dependencies.
 */
@Composable
fun PosterCard(
    title: String,
    rating: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    poster: @Composable () -> Unit,
) {
    Card(onClick = onClick, modifier = modifier) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f),
            ) {
                poster()
                RatingBadge(
                    rating = rating,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp),
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 4.dp),
                )
            }
        }
    }
}

@Composable
fun RatingBadge(
    rating: Double,
    modifier: Modifier = Modifier,
) {
    Text(
        // TMDB vote_average is 0..10 with noisy precision; one decimal reads best.
        text = "★ %.1f".format(rating),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.small,
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Preview(name = "light", showBackground = true)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(name = "large font", showBackground = true, fontScale = 1.5f)
@Composable
private fun PosterCardPreview() {
    TMDBTheme {
        PosterCard(
            title = "Fight Club",
            rating = 8.438,
            onClick = {},
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    }
}
