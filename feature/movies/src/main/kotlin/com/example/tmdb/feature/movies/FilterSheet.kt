package com.example.tmdb.feature.movies

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import kotlin.math.roundToInt

object FilterTestTags {
    const val SHEET = "filter_sheet"
    const val RESET = "filter_reset"
    fun sortChip(sort: MovieSort) = "filter_sort_${sort.name}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FilterSheet(
    filters: MovieFilters,
    onFiltersChanged: (MovieFilters) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(FilterTestTags.SHEET),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text("Sort & filter", style = MaterialTheme.typography.titleLarge)
                TextButton(
                    onClick = onReset,
                    enabled = !filters.isDefault,
                    modifier = Modifier.testTag(FilterTestTags.RESET),
                ) { Text("Reset") }
            }

            Section("Sort by") {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MovieSort.entries.forEach { sort ->
                        FilterChip(
                            selected = filters.sort == sort,
                            onClick = { onFiltersChanged(filters.copy(sort = sort)) },
                            label = { Text(sort.label) },
                            modifier = Modifier.testTag(FilterTestTags.sortChip(sort)),
                        )
                    }
                }
            }

            Section("Minimum rating: ${"%.1f".format(filters.minRating)}★") {
                Slider(
                    value = filters.minRating,
                    onValueChange = { onFiltersChanged(filters.copy(minRating = it)) },
                    valueRange = 0f..10f,
                    steps = 19, // half-point increments
                )
            }

            Section("Release years: ${filters.fromYear} – ${filters.toYear}") {
                RangeSlider(
                    value = filters.fromYear.toFloat()..filters.toYear.toFloat(),
                    onValueChange = { range ->
                        onFiltersChanged(
                            filters.copy(
                                fromYear = range.start.roundToInt(),
                                toYear = range.endInclusive.roundToInt(),
                            ),
                        )
                    },
                    valueRange = MIN_FILTER_YEAR.toFloat()..MAX_FILTER_YEAR.toFloat(),
                )
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Start,
        )
        content()
    }
}
