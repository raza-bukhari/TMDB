plugins {
    alias(libs.plugins.tmdb.android.library)
}

android {
    namespace = "com.example.tmdb.data"
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))

    testImplementation(project(":core:testing"))
    testImplementation(libs.turbine)
}
