plugins {
    alias(libs.plugins.tmdb.kotlin.jvm)
}

dependencies {
    implementation(project(":domain"))
    implementation(libs.junit)
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.androidx.paging.common)
}
