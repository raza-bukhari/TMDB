package com.example.tmdb.core.designsystem

import androidx.compose.ui.tooling.preview.Preview

/**
 * Multi-config preview: light, dark, and large-font (1.5x) renderings in one annotation.
 * Apply to screen-level composable previews so theming/scaling regressions surface in the IDE.
 */
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 1.5f)
annotation class ThemePreviews
