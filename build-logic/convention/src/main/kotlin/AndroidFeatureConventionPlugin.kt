import com.tmdb.buildlogic.lib
import com.tmdb.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("tmdb.android.library")
            pluginManager.apply("tmdb.android.compose")

            with(dependencies) {
                add("implementation", project(":domain"))
                add("implementation", project(":core:common"))
                add("implementation", project(":core:designsystem"))
                add("implementation", project(":core:navigation"))

                val koinBom = platform(libs.lib("koin-bom"))
                add("implementation", koinBom)
                add("implementation", libs.lib("koin-android"))
                add("implementation", libs.lib("koin-androidx-compose"))

                add("implementation", libs.lib("androidx-lifecycle-runtime-compose"))
                add("implementation", libs.lib("androidx-lifecycle-viewmodel-compose"))
                add("implementation", libs.lib("androidx-navigation-compose"))
                add("implementation", libs.lib("kotlinx-collections-immutable"))
                add("implementation", libs.lib("coil-compose"))
                // Coil 3 ships network loading separately; without this AsyncImage renders nothing.
                add("implementation", libs.lib("coil-network-okhttp"))

                add("testImplementation", project(":core:testing"))
                add("testImplementation", libs.lib("turbine"))
                add("testImplementation", libs.lib("mockk"))
                add("testImplementation", koinBom)
                add("testImplementation", libs.lib("koin-test"))
                add("androidTestImplementation", libs.lib("androidx-junit"))
            }
        }
    }
}
