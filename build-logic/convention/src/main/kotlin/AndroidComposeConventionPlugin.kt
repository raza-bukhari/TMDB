import com.android.build.api.dsl.CommonExtension
import com.tmdb.buildlogic.lib
import com.tmdb.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val android = extensions.findByType(CommonExtension::class.java)
                ?: error("tmdb.android.compose must be applied after an Android plugin (tmdb.android.application/library)")
            android.buildFeatures.compose = true

            with(dependencies) {
                val bom = platform(libs.lib("androidx-compose-bom"))
                add("implementation", bom)
                add("implementation", libs.lib("androidx-compose-ui"))
                add("implementation", libs.lib("androidx-compose-ui-graphics"))
                add("implementation", libs.lib("androidx-compose-ui-tooling-preview"))
                add("implementation", libs.lib("androidx-compose-material3"))
                add("debugImplementation", libs.lib("androidx-compose-ui-tooling"))
                add("debugImplementation", libs.lib("androidx-compose-ui-test-manifest"))
                add("androidTestImplementation", bom)
                add("androidTestImplementation", libs.lib("androidx-compose-ui-test-junit4"))
            }
        }
    }
}
