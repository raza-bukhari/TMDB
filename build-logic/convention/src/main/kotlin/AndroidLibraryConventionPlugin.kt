import com.android.build.api.dsl.LibraryExtension
import com.tmdb.buildlogic.configureAndroidCommon
import com.tmdb.buildlogic.lib
import com.tmdb.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            extensions.configure<LibraryExtension> {
                configureAndroidCommon(this)
                // Robolectric needs android resources on the unit-test classpath
                testOptions.unitTests.isIncludeAndroidResources = true
            }
            dependencies.add("implementation", dependencies.platform(libs.lib("koin-bom")))
            dependencies.add("implementation", libs.lib("koin-core"))
            dependencies.add("testImplementation", libs.lib("junit"))
            dependencies.add("testImplementation", libs.lib("kotlinx-coroutines-test"))
        }
    }
}
