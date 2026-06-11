package com.example.tmdb.core.designsystem.theme

/** User-selectable theme preference, cycled from the top bar. */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    ;

    /** Next mode in the cycle: System → Light → Dark → System. */
    fun next(): ThemeMode = when (this) {
        SYSTEM -> LIGHT
        LIGHT -> DARK
        DARK -> SYSTEM
    }
}
