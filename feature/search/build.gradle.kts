plugins {
    alias(libs.plugins.tmdb.android.feature)
}

android {
    namespace = "com.example.tmdb.feature.search"
}

dependencies {
    implementation(libs.androidx.compose.material.icons.core)
}
