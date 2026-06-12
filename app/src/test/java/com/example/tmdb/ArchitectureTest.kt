package com.example.tmdb

import com.lemonappdev.konsist.api.Konsist
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Build-breaking architecture guardrails. These scan the whole repo from disk,
 * so they live here only for convenience (D-003).
 */
class ArchitectureTest {

    private val sep: String = File.separator

    private fun mainSourceFilesUnder(modulePath: String) =
        Konsist.scopeFromProject().files.filter {
            it.path.contains("$sep$modulePath$sep" + "src" + "$sep" + "main")
        }

    @Test
    fun `domain is pure Kotlin with no Android imports`() {
        val offending = mainSourceFilesUnder("domain")
            .flatMap { file -> file.imports.map { file.path to it.name } }
            .filter { (_, import) ->
                import.startsWith("android.") || (import.startsWith("androidx.") && !import.startsWith("androidx.paging."))
            }
        assertTrue("Android imports found in :domain: $offending", offending.isEmpty())
    }

    @Test
    fun `feature modules do not depend on each other`() {
        val featureRegex = Regex("${Regex.escape(sep)}feature$sep([^$sep]+)$sep")
        val offending = Konsist.scopeFromProject().files
            .mapNotNull { file ->
                val ownFeature = featureRegex.find(file.path)?.groupValues?.get(1) ?: return@mapNotNull null
                val foreign = file.imports
                    .map { it.name }
                    .filter { it.startsWith("com.example.tmdb.feature.") && !it.startsWith("com.example.tmdb.feature.$ownFeature.") }
                if (foreign.isEmpty()) null else file.path to foreign
            }
        assertTrue("Cross-feature imports found: $offending", offending.isEmpty())
    }

    @Test
    fun `presentation and domain never import data-layer machinery`() {
        val forbidden = listOf("retrofit2.", "okhttp3.", "androidx.room.", "com.example.tmdb.core.network.", "com.example.tmdb.core.database.")
        val scanned = Konsist.scopeFromProject().files.filter { file ->
            file.path.contains("${sep}feature$sep") || file.path.contains("$sep" + "domain" + "$sep")
        }
        val offending = scanned
            .flatMap { file -> file.imports.map { file.path to it.name } }
            .filter { (_, import) -> forbidden.any { import.startsWith(it) } }
        assertTrue("Data-layer imports leaked into presentation/domain: $offending", offending.isEmpty())
    }

    @Test
    fun `data layer does not import compose or viewmodel`() {
        val forbidden = listOf("androidx.compose.", "androidx.lifecycle.")
        val offending = mainSourceFilesUnder("data")
            .flatMap { file -> file.imports.map { file.path to it.name } }
            .filter { (_, import) -> forbidden.any { import.startsWith(it) } }
        assertTrue("Presentation imports leaked into :data: $offending", offending.isEmpty())
    }
}
