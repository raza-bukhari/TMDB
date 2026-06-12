package com.tmdb.buildlogic

import java.util.Properties
import org.gradle.api.Project

/**
 * TMDB v4 read access token, resolved from `local.properties` (key `TMDB_API_TOKEN`)
 * with an environment-variable fallback for CI. Empty string when absent —
 * callers decide whether that is fatal (the :app assemble path is).
 */
fun Project.tmdbApiToken(): String = secret("TMDB_API_TOKEN")

/**
 * Optional OMDb key (`OMDB_API_KEY` in local.properties or env) powering
 * IMDb/Rotten Tomatoes scores; the app degrades gracefully without it.
 */
fun Project.omdbApiKey(): String = secret("OMDB_API_KEY")

private fun Project.secret(key: String): String {
    val localProperties = rootProject.file("local.properties")
    val fromFile = if (localProperties.exists()) {
        Properties()
            .apply { localProperties.inputStream().use(::load) }
            .getProperty(key)
    } else {
        null
    }
    return fromFile ?: System.getenv(key) ?: ""
}
