package com.tmdb.buildlogic

import java.util.Properties
import org.gradle.api.Project

/**
 * TMDB v4 read access token, resolved from `local.properties` (key `TMDB_API_TOKEN`)
 * with an environment-variable fallback for CI. Empty string when absent —
 * callers decide whether that is fatal (the :app assemble path is).
 */
fun Project.tmdbApiToken(): String {
    val localProperties = rootProject.file("local.properties")
    val fromFile = if (localProperties.exists()) {
        Properties()
            .apply { localProperties.inputStream().use(::load) }
            .getProperty("TMDB_API_TOKEN")
    } else {
        null
    }
    return fromFile ?: System.getenv("TMDB_API_TOKEN") ?: ""
}
