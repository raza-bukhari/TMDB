import com.tmdb.buildlogic.tmdbApiToken

plugins {
    alias(libs.plugins.tmdb.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.tmdb.core.network"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        // Empty when absent; the :app assemble path fails fast instead (see D-004/D-010).
        buildConfigField("String", "TMDB_API_TOKEN", "\"${tmdbApiToken()}\"")
        buildConfigField("String", "TMDB_BASE_URL", "\"https://api.themoviedb.org/3/\"")
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":domain"))

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    // implementation (not debugImplementation): referenced from main source behind a
    // BuildConfig.DEBUG guard so release compiles; logging never runs in release.
    implementation(libs.okhttp.logging)

    testImplementation(libs.mockwebserver3.junit4)
    testImplementation(libs.kotlinx.serialization.json)
}
