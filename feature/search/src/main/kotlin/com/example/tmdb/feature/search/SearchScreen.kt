package com.example.tmdb.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.example.tmdb.core.designsystem.component.EmptyState
import com.example.tmdb.core.designsystem.component.ErrorState
import com.example.tmdb.core.designsystem.component.LoadingState
import com.example.tmdb.core.designsystem.component.RatingBadge
import com.example.tmdb.domain.model.AppError
import com.example.tmdb.domain.model.asAppError
import org.koin.androidx.compose.koinViewModel

object SearchTestTags {
    const val FIELD = "search_field"
    const val BACK = "search_back"
    const val CLEAR = "search_clear"
    const val LIST = "search_list"
    const val APPEND_SPINNER = "search_append_spinner"
    fun resultItem(id: Long) = "search_result_$id"
}

@Composable
fun SearchScreen(
    onMovieClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SearchViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pagingItems = viewModel.pagingData.collectAsLazyPagingItems()

    SearchScreenContent(
        state = state,
        pagingItems = pagingItems,
        onBackClick = onBackClick,
        onQueryChanged = viewModel::onQueryChanged,
        onMovieClick = onMovieClick,
        onRetryClick = { pagingItems.retry() },
        modifier = modifier,
    )
}

@Composable
internal fun SearchScreenContent(
    state: SearchUiState,
    pagingItems: LazyPagingItems<SearchResultItem>,
    onBackClick: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onMovieClick: (Long) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            SearchBar(
                query = state.query,
                onQueryChanged = onQueryChanged,
                onBackClick = onBackClick,
            )

            SearchBody(
                query = state.query,
                pagingItems = pagingItems,
                onMovieClick = onMovieClick,
                onRetryClick = onRetryClick,
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(8.dp),
    ) {
        IconButton(onClick = onBackClick, modifier = Modifier.testTag(SearchTestTags.BACK)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            placeholder = { Text("Search movies...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChanged("") },
                        modifier = Modifier.testTag(SearchTestTags.CLEAR),
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.weight(1f).testTag(SearchTestTags.FIELD),
        )
    }
}

@Composable
private fun SearchBody(
    query: String,
    pagingItems: LazyPagingItems<SearchResultItem>,
    onMovieClick: (Long) -> Unit,
    onRetryClick: () -> Unit,
) {
    if (query.isBlank()) {
        EmptyState(message = "Start typing to search movies.")
        return
    }

    val refreshLoadState = pagingItems.loadState.refresh

    when {
        refreshLoadState is LoadState.Loading && pagingItems.itemCount == 0 -> {
            LoadingState()
        }
        refreshLoadState is LoadState.Error && pagingItems.itemCount == 0 -> {
            ErrorState(
                message = refreshLoadState.error.asAppError().toUserMessage(),
                onRetryClick = onRetryClick
            )
        }
        pagingItems.itemCount == 0 && refreshLoadState is LoadState.NotLoading -> {
            EmptyState(message = "No results found for \"$query\".")
        }
        else -> {
            SearchList(
                pagingItems = pagingItems,
                onMovieClick = onMovieClick,
            )
        }
    }
}

@Composable
private fun SearchList(
    pagingItems: LazyPagingItems<SearchResultItem>,
    onMovieClick: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag(SearchTestTags.LIST),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey { it.id },
            contentType = pagingItems.itemContentType { "movie" }
        ) { index ->
            pagingItems[index]?.let { item ->
                SearchResultRow(
                    item = item,
                    onClick = { onMovieClick(item.id) },
                    modifier = Modifier.testTag(SearchTestTags.resultItem(item.id)),
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        if (pagingItems.loadState.append is LoadState.Loading) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag(SearchTestTags.APPEND_SPINNER),
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    item: SearchResultItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AsyncImage(
            model = item.posterUrl,
            contentDescription = null,
            modifier = Modifier.height(100.dp).padding(vertical = 4.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            RatingBadge(rating = item.rating, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

private fun AppError.toUserMessage(): String = when (this) {
    AppError.Offline -> "You're offline. Connect and try again."
    AppError.InvalidToken -> "TMDB rejected the API token."
    AppError.NotFound -> "Resource not found."
    AppError.RateLimited -> "Too many requests. Please wait."
    is AppError.Unknown -> "Something went wrong."
}
