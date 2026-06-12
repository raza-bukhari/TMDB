plugins {
    alias(libs.plugins.tmdb.android.feature)
    alias(libs.plugins.kover)
}

android {
    namespace = "com.example.tmdb.feature.movies"
}

dependencies {
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.paging.runtime)
}

// 80% floor on ViewModel/UiState logic; Composables are verified by UI tests, not Kover.
kover {
    reports {
        filters {
            excludes {
                annotatedBy("androidx.compose.runtime.Composable")
                classes("*Module*", "*ComposableSingletons*", "*\$*Preview*")
            }
        }
        verify { rule { minBound(80) } }
    }
}
