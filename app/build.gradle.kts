import com.tmdb.buildlogic.tmdbApiToken

plugins {
    alias(libs.plugins.tmdb.android.application)
    alias(libs.plugins.tmdb.android.compose)
}

android {
    namespace = "com.example.tmdb"

    defaultConfig {
        applicationId = "com.example.tmdb"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
}

// D-004/D-010: assembling the app without a TMDB token fails fast with an actionable
// message; library builds and all tests stay runnable (-PskipTokenCheck=true to override).
val checkTmdbToken = tasks.register("checkTmdbToken") {
    // Locals only: capturing script-level vals breaks the configuration cache.
    val tokenPresent = project.tmdbApiToken().isNotBlank()
    val skipTokenCheck = providers.gradleProperty("skipTokenCheck").orNull?.toBoolean() ?: false
    doFirst {
        check(tokenPresent || skipTokenCheck) {
            "TMDB_API_TOKEN is missing. Add TMDB_API_TOKEN=<your TMDB v4 read access token> " +
                "to local.properties (create one at https://www.themoviedb.org/settings/api), " +
                "set the TMDB_API_TOKEN env var, or pass -PskipTokenCheck=true."
        }
    }
}
tasks.named("preBuild") { dependsOn(checkTmdbToken) }

dependencies {
    implementation(project(":feature:movies"))
    implementation(project(":feature:detail"))
    implementation(project(":feature:search"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:navigation"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":domain"))
    implementation(project(":data"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    testImplementation(libs.junit)
    testImplementation(libs.konsist)
    testImplementation(platform(libs.koin.bom))
    testImplementation(libs.koin.test)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
