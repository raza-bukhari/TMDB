package com.example.tmdb.feature.person

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.tmdb.core.designsystem.component.GlassSurface
import org.koin.androidx.compose.koinViewModel

@Composable
fun PersonScreen(
    onBackClick: () -> Unit,
    onMediaClick: (Long, com.example.tmdb.domain.model.MediaType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: PersonViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PersonScreenContent(
        state = state,
        onBackClick = onBackClick,
        onRetryClick = viewModel::onRetryClicked,
        onMediaClick = onMediaClick,
        modifier = modifier,
    )
}

@Composable
internal fun PersonScreenContent(
    state: PersonUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onMediaClick: (Long, com.example.tmdb.domain.model.MediaType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(state.errorMessage, style = MaterialTheme.typography.bodyLarge)
                    TextButton(onClick = onRetryClick) { Text("Retry") }
                    TextButton(onClick = onBackClick) { Text("Back") }
                }
            }
            state.person != null -> PersonContent(state.person, state.credits, onBackClick, onMediaClick)
        }
    }
}

@Composable
private fun PersonContent(
    person: PersonUi,
    credits: List<PersonCreditUi>,
    onBackClick: () -> Unit,
    onMediaClick: (Long, com.example.tmdb.domain.model.MediaType) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 28.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(onClick = onBackClick) { Text("Back") }
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = person.profileUrl,
                        contentDescription = person.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(118.dp)
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                        Text(person.name, style = MaterialTheme.typography.headlineSmall)
                        person.knownFor?.let { Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary) }
                        person.birthInfo?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
                if (person.biography.isNotBlank()) {
                    GlassSurface(contentPadding = PaddingValues(14.dp)) {
                        Text(
                            text = person.biography,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        item {
            Text(
                text = "Filmography",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(((credits.take(18).size + 2) / 3 * 210).dp)
                    .padding(horizontal = 16.dp),
            ) {
                items(credits.take(18), key = { "${it.mediaType}:${it.id}" }) { credit ->
                    PersonCreditCard(credit, onMediaClick)
                }
            }
        }
        item { Spacer(Modifier.navigationBarsPadding()) }
    }
}

@Composable
private fun PersonCreditCard(
    credit: PersonCreditUi,
    onMediaClick: (Long, com.example.tmdb.domain.model.MediaType) -> Unit,
) {
    Column(
        modifier = Modifier.clickable { onMediaClick(credit.id, credit.mediaType) },
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AsyncImage(
            model = credit.posterUrl,
            contentDescription = credit.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Text(credit.title, style = MaterialTheme.typography.labelMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(
            text = listOfNotNull(credit.year, credit.mediaType.name.lowercase().replaceFirstChar { it.titlecase() }).joinToString(" · "),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}
