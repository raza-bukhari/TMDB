plugins {
    alias(libs.plugins.tmdb.android.feature)
}

android {
    namespace = "com.example.tmdb.feature.detail"
}

dependencies {
    implementation(libs.androidx.compose.material.icons.core)

    // SavedStateHandle.toRoute needs the Android SavedState runtime in unit tests.
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
