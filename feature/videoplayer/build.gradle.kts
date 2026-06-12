plugins {
    alias(libs.plugins.tmdb.android.feature)
}

android {
    namespace = "com.example.tmdb.feature.videoplayer"
}

dependencies {
    // YouTube IFrame player engine. TMDB only exposes YouTube videos, which Media3
    // cannot play; the ViewModel stays engine-agnostic so a Media3 engine can be
    // dropped in for direct-URL sources later.
    implementation(libs.youtube.player)
    implementation(libs.androidx.compose.material.icons.core)
}
