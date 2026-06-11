import com.tmdb.buildlogic.lib
import com.tmdb.buildlogic.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class KotlinJvmConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            extensions.configure<JavaPluginExtension> {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
            extensions.configure<KotlinJvmProjectExtension> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
            dependencies.add("implementation", libs.lib("kotlinx-coroutines-core"))
            dependencies.add("implementation", dependencies.platform(libs.lib("koin-bom")))
            dependencies.add("implementation", libs.lib("koin-core"))
            dependencies.add("testImplementation", libs.lib("junit"))
            dependencies.add("testImplementation", libs.lib("kotlinx-coroutines-test"))
            dependencies.add("testImplementation", libs.lib("turbine"))
        }
    }
}
