plugins {
    alias(libs.plugins.tmdb.android.library)
    alias(libs.plugins.tmdb.android.compose)
}

android {
    namespace = "com.example.tmdb.core.designsystem"
}

dependencies {
    // WindowCompat for reactive system-bar icon appearance in TMDBTheme.
    implementation(libs.androidx.core.ktx)
    // Star glyph for the rating badge.
    implementation(libs.androidx.compose.material.icons.core)
}
