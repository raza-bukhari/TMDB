package com.example.tmdb.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * A selectable colour theme. Beyond the TMDB brand we ship a curated set of the
 * most widely-ported open-source colour systems — Catppuccin, Nord, Dracula,
 * Gruvbox, Solarized, Tokyo Night and Rosé Pine — giving the user a rich variety
 * of light and dark options.
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

    NORD("Nord", "Nord", true),

    DRACULA("Dracula", "Dracula", true),

    GRUVBOX_LIGHT("Gruvbox Light", "Gruvbox", false),
    GRUVBOX_DARK("Gruvbox Dark", "Gruvbox", true),

    SOLARIZED_LIGHT("Solarized Light", "Solarized", false),
    SOLARIZED_DARK("Solarized Dark", "Solarized", true),

    TOKYO_NIGHT("Tokyo Night", "Tokyo Night", true),

    ROSE_PINE_DAWN("Dawn", "Rosé Pine", false),
    ROSE_PINE("Rosé Pine", "Rosé Pine", true),
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
        AppTheme.NORD -> Nord.scheme(dark = true)
        AppTheme.DRACULA -> Dracula.scheme(dark = true)
        AppTheme.GRUVBOX_LIGHT -> GruvboxLight.scheme(dark = false)
        AppTheme.GRUVBOX_DARK -> GruvboxDark.scheme(dark = true)
        AppTheme.SOLARIZED_LIGHT -> SolarizedLight.scheme(dark = false)
        AppTheme.SOLARIZED_DARK -> SolarizedDark.scheme(dark = true)
        AppTheme.TOKYO_NIGHT -> TokyoNight.scheme(dark = true)
        AppTheme.ROSE_PINE_DAWN -> RosePineDawn.scheme(dark = false)
        AppTheme.ROSE_PINE -> RosePine.scheme(dark = true)
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
// Open-source palettes mapped onto Material 3 roles.
//
// Each palette supplies a small, well-defined set of tokens — a three-step
// background ramp (base/mantle/crust), two foreground tones, a three-step
// surface ramp, an outline and four accents — which [ThemePalette.scheme]
// projects onto a full Material 3 [ColorScheme]. Hex values are the official
// ones published by each project.
// ---------------------------------------------------------------------------

/** A palette's tokens, projected onto Material 3 roles by [scheme]. */
private class ThemePalette(
    val base: Color,      // primary background
    val mantle: Color,    // low container / slightly off background
    val crust: Color,     // deepest edge, used for scrim
    val text: Color,      // primary foreground
    val subtext: Color,   // muted foreground
    val surface0: Color,  // elevated surface (low)
    val surface1: Color,  // elevated surface (mid)
    val surface2: Color,  // elevated surface (high)
    val overlay: Color,   // outline / muted divider
    val accent: Color,    // primary accent
    val accent2: Color,   // secondary accent
    val accent3: Color,   // tertiary accent
    val error: Color,
) {
    /** Map the palette onto a Material 3 scheme; accent text flips with brightness. */
    fun scheme(dark: Boolean): ColorScheme {
        val onAccent = if (dark) crust else base
        val baseScheme = if (dark) darkColorScheme() else lightColorScheme()
        return baseScheme.copy(
            primary = accent,
            onPrimary = onAccent,
            primaryContainer = surface1,
            onPrimaryContainer = text,
            inversePrimary = accent,
            secondary = accent2,
            onSecondary = onAccent,
            secondaryContainer = surface0,
            onSecondaryContainer = text,
            tertiary = accent3,
            onTertiary = onAccent,
            tertiaryContainer = surface0,
            onTertiaryContainer = text,
            background = base,
            onBackground = text,
            surface = base,
            onSurface = text,
            surfaceVariant = surface0,
            onSurfaceVariant = subtext,
            surfaceTint = accent,
            surfaceContainerLowest = crust,
            surfaceContainerLow = mantle,
            surfaceContainer = mantle,
            surfaceContainerHigh = surface0,
            surfaceContainerHighest = surface1,
            inverseSurface = text,
            inverseOnSurface = base,
            error = error,
            onError = onAccent,
            errorContainer = surface0,
            onErrorContainer = error,
            outline = overlay,
            outlineVariant = surface1,
            scrim = crust,
        )
    }
}

// --- Catppuccin (https://github.com/catppuccin/palette) --------------------

private val Latte = ThemePalette(
    base = Color(0xFFEFF1F5), mantle = Color(0xFFE6E9EF), crust = Color(0xFFDCE0E8),
    text = Color(0xFF4C4F69), subtext = Color(0xFF5C5F77),
    surface0 = Color(0xFFCCD0DA), surface1 = Color(0xFFBCC0CC), surface2 = Color(0xFFACB0BE),
    overlay = Color(0xFF9CA0B0),
    accent = Color(0xFF8839EF), accent2 = Color(0xFFEA76CB), accent3 = Color(0xFF1E66F5), error = Color(0xFFD20F39),
)

private val Frappe = ThemePalette(
    base = Color(0xFF303446), mantle = Color(0xFF292C3C), crust = Color(0xFF232634),
    text = Color(0xFFC6D0F5), subtext = Color(0xFFB5BFE2),
    surface0 = Color(0xFF414559), surface1 = Color(0xFF51576D), surface2 = Color(0xFF626880),
    overlay = Color(0xFF737994),
    accent = Color(0xFFCA9EE6), accent2 = Color(0xFFF4B8E4), accent3 = Color(0xFF8CAAEE), error = Color(0xFFE78284),
)

private val Macchiato = ThemePalette(
    base = Color(0xFF24273A), mantle = Color(0xFF1E2030), crust = Color(0xFF181926),
    text = Color(0xFFCAD3F5), subtext = Color(0xFFB8C0E0),
    surface0 = Color(0xFF363A4F), surface1 = Color(0xFF494D64), surface2 = Color(0xFF5B6078),
    overlay = Color(0xFF6E738D),
    accent = Color(0xFFC6A0F6), accent2 = Color(0xFFF5BDE6), accent3 = Color(0xFF8AADF4), error = Color(0xFFED8796),
)

private val Mocha = ThemePalette(
    base = Color(0xFF1E1E2E), mantle = Color(0xFF181825), crust = Color(0xFF11111B),
    text = Color(0xFFCDD6F4), subtext = Color(0xFFBAC2DE),
    surface0 = Color(0xFF313244), surface1 = Color(0xFF45475A), surface2 = Color(0xFF585B70),
    overlay = Color(0xFF6C7086),
    accent = Color(0xFFCBA6F7), accent2 = Color(0xFFF5C2E7), accent3 = Color(0xFF89B4FA), error = Color(0xFFF38BA8),
)

// --- Nord (https://www.nordtheme.com) --------------------------------------

private val Nord = ThemePalette(
    base = Color(0xFF2E3440), mantle = Color(0xFF2B303B), crust = Color(0xFF272C36),
    text = Color(0xFFECEFF4), subtext = Color(0xFFD8DEE9),
    surface0 = Color(0xFF3B4252), surface1 = Color(0xFF434C5E), surface2 = Color(0xFF4C566A),
    overlay = Color(0xFF616E88),
    accent = Color(0xFF88C0D0), accent2 = Color(0xFFB48EAD), accent3 = Color(0xFF81A1C1), error = Color(0xFFBF616A),
)

// --- Dracula (https://draculatheme.com) ------------------------------------

private val Dracula = ThemePalette(
    base = Color(0xFF282A36), mantle = Color(0xFF21222C), crust = Color(0xFF191A21),
    text = Color(0xFFF8F8F2), subtext = Color(0xFFC8C8DC),
    surface0 = Color(0xFF343746), surface1 = Color(0xFF44475A), surface2 = Color(0xFF565872),
    overlay = Color(0xFF6272A4),
    accent = Color(0xFFBD93F9), accent2 = Color(0xFFFF79C6), accent3 = Color(0xFF8BE9FD), error = Color(0xFFFF5555),
)

// --- Gruvbox (https://github.com/morhetz/gruvbox) --------------------------

private val GruvboxLight = ThemePalette(
    base = Color(0xFFFBF1C7), mantle = Color(0xFFF2E5BC), crust = Color(0xFFEBDBB2),
    text = Color(0xFF3C3836), subtext = Color(0xFF504945),
    surface0 = Color(0xFFEBDBB2), surface1 = Color(0xFFD5C4A1), surface2 = Color(0xFFBDAE93),
    overlay = Color(0xFFA89984),
    accent = Color(0xFFAF3A03), accent2 = Color(0xFF8F3F71), accent3 = Color(0xFF427B58), error = Color(0xFF9D0006),
)

private val GruvboxDark = ThemePalette(
    base = Color(0xFF282828), mantle = Color(0xFF1D2021), crust = Color(0xFF1B1B1B),
    text = Color(0xFFEBDBB2), subtext = Color(0xFFD5C4A1),
    surface0 = Color(0xFF3C3836), surface1 = Color(0xFF504945), surface2 = Color(0xFF665C54),
    overlay = Color(0xFF928374),
    accent = Color(0xFFFE8019), accent2 = Color(0xFFD3869B), accent3 = Color(0xFF8EC07C), error = Color(0xFFFB4934),
)

// --- Solarized (https://ethanschoonover.com/solarized) ---------------------

private val SolarizedLight = ThemePalette(
    base = Color(0xFFFDF6E3), mantle = Color(0xFFF2EBD7), crust = Color(0xFFEEE8D5),
    text = Color(0xFF586E75), subtext = Color(0xFF657B83),
    surface0 = Color(0xFFEEE8D5), surface1 = Color(0xFFDDD6C1), surface2 = Color(0xFFCEC7B0),
    overlay = Color(0xFF93A1A1),
    accent = Color(0xFF268BD2), accent2 = Color(0xFFD33682), accent3 = Color(0xFF2AA198), error = Color(0xFFDC322F),
)

private val SolarizedDark = ThemePalette(
    base = Color(0xFF002B36), mantle = Color(0xFF022B34), crust = Color(0xFF00212B),
    text = Color(0xFF93A1A1), subtext = Color(0xFF839496),
    surface0 = Color(0xFF073642), surface1 = Color(0xFF0E4655), surface2 = Color(0xFF14576A),
    overlay = Color(0xFF586E75),
    accent = Color(0xFF268BD2), accent2 = Color(0xFFD33682), accent3 = Color(0xFF2AA198), error = Color(0xFFDC322F),
)

// --- Tokyo Night (https://github.com/enkia/tokyo-night-vscode-theme) -------

private val TokyoNight = ThemePalette(
    base = Color(0xFF1A1B26), mantle = Color(0xFF16161E), crust = Color(0xFF13131A),
    text = Color(0xFFC0CAF5), subtext = Color(0xFFA9B1D6),
    surface0 = Color(0xFF24283B), surface1 = Color(0xFF292E42), surface2 = Color(0xFF3B4261),
    overlay = Color(0xFF565F89),
    accent = Color(0xFF7AA2F7), accent2 = Color(0xFFBB9AF7), accent3 = Color(0xFF7DCFFF), error = Color(0xFFF7768E),
)

// --- Rosé Pine (https://rosepinetheme.com) ---------------------------------

private val RosePineDawn = ThemePalette(
    base = Color(0xFFFAF4ED), mantle = Color(0xFFFFFAF3), crust = Color(0xFFF2E9E1),
    text = Color(0xFF575279), subtext = Color(0xFF797593),
    surface0 = Color(0xFFF2E9E1), surface1 = Color(0xFFDFDAD9), surface2 = Color(0xFFCECACD),
    overlay = Color(0xFF9893A5),
    accent = Color(0xFF907AA9), accent2 = Color(0xFFB4637A), accent3 = Color(0xFF56949F), error = Color(0xFFB4637A),
)

private val RosePine = ThemePalette(
    base = Color(0xFF191724), mantle = Color(0xFF1F1D2E), crust = Color(0xFF161420),
    text = Color(0xFFE0DEF4), subtext = Color(0xFF908CAA),
    surface0 = Color(0xFF26233A), surface1 = Color(0xFF403D52), surface2 = Color(0xFF524F67),
    overlay = Color(0xFF6E6A86),
    accent = Color(0xFFC4A7E7), accent2 = Color(0xFFEB6F92), accent3 = Color(0xFF9CCFD8), error = Color(0xFFEB6F92),
)
