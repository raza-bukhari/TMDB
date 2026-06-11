plugins {
    alias(libs.plugins.tmdb.kotlin.jvm)
    alias(libs.plugins.kover)
}

dependencies {
    implementation(project(":core:common"))
}

// The contract holds :domain to an 80% line-coverage floor.
kover {
    reports {
        filters {
            excludes { classes("*Module*") } // Koin wiring, no logic
        }
        verify { rule { minBound(80) } }
    }
}
