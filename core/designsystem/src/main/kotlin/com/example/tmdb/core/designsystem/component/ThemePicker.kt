package com.example.tmdb.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.tmdb.core.designsystem.theme.AppTheme
import com.example.tmdb.core.designsystem.theme.swatch

object ThemePickerTestTags {
    const val PICKER = "theme_picker"
    fun option(theme: AppTheme) = "theme_option_${theme.name}"
}

/**
 * Vertical list of selectable themes, grouped by family (General / TMDB / Catppuccin),
 * each row showing a colour swatch, name, and a check on the active theme.
 */
@Composable
fun ThemePicker(
    selected: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag(ThemePickerTestTags.PICKER),
    ) {
        AppTheme.entries
            .groupBy { it.group }
            .forEach { (group, themes) ->
                Text(
                    text = group,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, start = 2.dp),
                )
                themes.forEach { theme ->
                    ThemeRow(
                        theme = theme,
                        selected = theme == selected,
                        onClick = { onThemeSelected(theme) },
                    )
                }
            }
    }
}

@Composable
private fun ThemeRow(
    theme: AppTheme,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(ThemePickerTestTags.option(theme)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            SwatchDots(theme.swatch)
            Text(
                text = theme.displayName,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun SwatchDots(colors: List<Color>, modifier: Modifier = Modifier) {
    // Slightly overlapping circles previewing the theme's background, surface, and accent.
    val outline = MaterialTheme.colorScheme.outline
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy((-7).dp)) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(BorderStroke(1.dp, outline.copy(alpha = 0.4f)), CircleShape),
            )
        }
    }
}
