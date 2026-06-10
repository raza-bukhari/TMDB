plugins {
    `kotlin-dsl`
}

group = "com.tmdb.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "tmdb.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "tmdb.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "tmdb.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "tmdb.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("kotlinJvm") {
            id = "tmdb.kotlin.jvm"
            implementationClass = "KotlinJvmConventionPlugin"
        }
    }
}
