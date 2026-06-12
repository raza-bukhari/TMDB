package com.example.tmdb.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * A selectable colour theme. Beyond the TMDB brand we ship the four official
 * [Catppuccin](https://catppuccin.com) flavours — the most widely-ported pastel
 * theme system — giving the user multiple light and dark options.
 *
 * @property isDark fixed brightness, or `null` to follow the system setting.
 */
enum class AppTheme(
    val displayName: String,
    val group: String,
    val isDark: Boolean?,
) {
    SYSTEM("System default", "General", null),
    TMDB_LIGHT("TMDB Light", "TMDB", false),
    TMDB_DARK("TMDB Dark", "TMDB", true),
    CATPPUCCIN_LATTE("Latte", "Catppuccin", false),
    CATPPUCCIN_FRAPPE("Frappé", "Catppuccin", true),
    CATPPUCCIN_MACCHIATO("Macchiato", "Catppuccin", true),
    CATPPUCCIN_MOCHA("Mocha", "Catppuccin", true),
    ;

    /** Short pill label for the quick top-bar cycle. */
    val shortLabel: String
        get() = when (this) {
            SYSTEM -> "Auto"
            TMDB_LIGHT -> "Light"
            TMDB_DARK -> "Dark"
            else -> displayName
        }

    fun next(): AppTheme = entries[(ordinal + 1) % entries.size]

    companion object {
        fun fromNameOrDefault(name: String?): AppTheme =
            entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}

/**
 * The concrete [ColorScheme] for a theme. `null` for [AppTheme.SYSTEM], which is
 * resolved against the live system setting inside `TMDBTheme`.
 */
internal val AppTheme.staticScheme: ColorScheme?
    get() = when (this) {
        AppTheme.SYSTEM -> null
        AppTheme.TMDB_LIGHT -> TmdbLightScheme
        AppTheme.TMDB_DARK -> TmdbDarkScheme
        AppTheme.CATPPUCCIN_LATTE -> Latte.scheme(dark = false)
        AppTheme.CATPPUCCIN_FRAPPE -> Frappe.scheme(dark = true)
        AppTheme.CATPPUCCIN_MACCHIATO -> Macchiato.scheme(dark = true)
        AppTheme.CATPPUCCIN_MOCHA -> Mocha.scheme(dark = true)
    }

/** Three representative colours (background, surface card, accent) for the picker swatch. */
val AppTheme.swatch: List<Color>
    get() {
        val scheme = staticScheme ?: TmdbDarkScheme
        return listOf(scheme.background, scheme.surfaceVariant, scheme.primary)
    }

// ---------------------------------------------------------------------------
// TMDB brand schemes (role colours defined in Color.kt)
// ---------------------------------------------------------------------------

internal val TmdbLightScheme: ColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
)

internal val TmdbDarkScheme: ColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
)

// ---------------------------------------------------------------------------
// Catppuccin — official palette (https://github.com/catppuccin/palette)
// ---------------------------------------------------------------------------

/** The subset of a Catppuccin flavour we map onto Material 3 roles. */
private class CatppuccinFlavor(
    val base: Color,
    val mantle: Color,
    val crust: Color,
    val text: Color,
    val subtext1: Color,
    val surface0: Color,
    val surface1: Color,
    val surface2: Color,
    val overlay0: Color,
    val mauve: Color,
    val pink: Color,
    val blue: Color,
    val red: Color,
) {
    /** Map the flavour onto a Material 3 scheme; accent text flips with brightness. */
    fun scheme(dark: Boolean): ColorScheme {
        val onAccent = if (dark) crust else base
        val baseScheme = if (dark) darkColorScheme() else lightColorScheme()
        return baseScheme.copy(
                primary = mauve,
                onPrimary = onAccent,
                primaryContainer = surface1,
                onPrimaryContainer = text,
                inversePrimary = mauve,
                secondary = pink,
                onSecondary = onAccent,
                secondaryContainer = surface0,
                onSecondaryContainer = text,
                tertiary = blue,
                onTertiary = onAccent,
                tertiaryContainer = surface0,
                onTertiaryContainer = text,
                background = base,
                onBackground = text,
                surface = base,
                onSurface = text,
                surfaceVariant = surface0,
                onSurfaceVariant = subtext1,
                surfaceTint = mauve,
                surfaceContainerLowest = crust,
                surfaceContainerLow = mantle,
                surfaceContainer = mantle,
                surfaceContainerHigh = surface0,
                surfaceContainerHighest = surface1,
                inverseSurface = text,
                inverseOnSurface = base,
                error = red,
                onError = onAccent,
                errorContainer = surface0,
                onErrorContainer = red,
                outline = overlay0,
                outlineVariant = surface1,
                scrim = crust,
        )
    }
}

private val Latte = CatppuccinFlavor(
    base = Color(0xFFEFF1F5), mantle = Color(0xFFE6E9EF), crust = Color(0xFFDCE0E8),
    text = Color(0xFF4C4F69), subtext1 = Color(0xFF5C5F77),
    surface0 = Color(0xFFCCD0DA), surface1 = Color(0xFFBCC0CC), surface2 = Color(0xFFACB0BE),
    overlay0 = Color(0xFF9CA0B0),
    mauve = Color(0xFF8839EF), pink = Color(0xFFEA76CB), blue = Color(0xFF1E66F5), red = Color(0xFFD20F39),
)

private val Frappe = CatppuccinFlavor(
    base = Color(0xFF303446), mantle = Color(0xFF292C3C), crust = Color(0xFF232634),
    text = Color(0xFFC6D0F5), subtext1 = Color(0xFFB5BFE2),
    surface0 = Color(0xFF414559), surface1 = Color(0xFF51576D), surface2 = Color(0xFF626880),
    overlay0 = Color(0xFF737994),
    mauve = Color(0xFFCA9EE6), pink = Color(0xFFF4B8E4), blue = Color(0xFF8CAAEE), red = Color(0xFFE78284),
)

private val Macchiato = CatppuccinFlavor(
    base = Color(0xFF24273A), mantle = Color(0xFF1E2030), crust = Color(0xFF181926),
    text = Color(0xFFCAD3F5), subtext1 = Color(0xFFB8C0E0),
    surface0 = Color(0xFF363A4F), surface1 = Color(0xFF494D64), surface2 = Color(0xFF5B6078),
    overlay0 = Color(0xFF6E738D),
    mauve = Color(0xFFC6A0F6), pink = Color(0xFFF5BDE6), blue = Color(0xFF8AADF4), red = Color(0xFFED8796),
)

private val Mocha = CatppuccinFlavor(
    base = Color(0xFF1E1E2E), mantle = Color(0xFF181825), crust = Color(0xFF11111B),
    text = Color(0xFFCDD6F4), subtext1 = Color(0xFFBAC2DE),
    surface0 = Color(0xFF313244), surface1 = Color(0xFF45475A), surface2 = Color(0xFF585B70),
    overlay0 = Color(0xFF6C7086),
    mauve = Color(0xFFCBA6F7), pink = Color(0xFFF5C2E7), blue = Color(0xFF89B4FA), red = Color(0xFFF38BA8),
)
