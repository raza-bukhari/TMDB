plugins {
    alias(libs.plugins.tmdb.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.tmdb.core.database"
}

dependencies {
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
