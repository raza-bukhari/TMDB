package com.example.tmdb.core.designsystem.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Primary entry point: render content under the user-selected [AppTheme]. */
@Composable
fun TMDBTheme(
    appTheme: AppTheme,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val colorScheme: ColorScheme = appTheme.staticScheme
        ?: if (systemDark) TmdbDarkScheme else TmdbLightScheme
    val darkIcons = !colorScheme.isDarkScheme()
    ApplyTheme(colorScheme, lightSystemBarIcons = darkIcons, content = content)
}

@Composable
fun TMDBTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val appTheme = when (themeMode) {
        ThemeMode.SYSTEM -> AppTheme.SYSTEM
        ThemeMode.LIGHT -> AppTheme.TMDB_LIGHT
        ThemeMode.DARK -> AppTheme.TMDB_DARK
    }
    TMDBTheme(appTheme = appTheme, content = content)
}

@Composable
fun TMDBTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    TMDBTheme(appTheme = if (darkTheme) AppTheme.TMDB_DARK else AppTheme.TMDB_LIGHT, content = content)
}

@Composable
private fun ApplyTheme(
    colorScheme: ColorScheme,
    lightSystemBarIcons: Boolean,
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            // Edge-to-edge: bars are transparent, so icon colour must follow the theme.
            controller.isAppearanceLightStatusBars = lightSystemBarIcons
            controller.isAppearanceLightNavigationBars = lightSystemBarIcons
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}

/** Heuristic: a scheme is dark when its surface is darker than its onSurface. */
private fun ColorScheme.isDarkScheme(): Boolean = surface.luminanceApprox() < onSurface.luminanceApprox()

private fun androidx.compose.ui.graphics.Color.luminanceApprox(): Float =
    0.299f * red + 0.587f * green + 0.114f * blue
