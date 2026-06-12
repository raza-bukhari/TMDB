plugins {
    alias(libs.plugins.tmdb.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.tmdb.core.database"
}

dependencies {
    implementation(libs.koin.android)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
