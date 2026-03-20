package convention.android

import com.android.build.api.dsl.LibraryExtension
import convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class ComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        target.extensions.configure<LibraryExtension> {
            buildFeatures {
                compose = true
            }
        }

        target.dependencies {
            add("implementation", platform(target.libs.findLibrary("androidx-compose-bom").get()))
            add("implementation", target.libs.findLibrary("androidx-compose-foundation").get())
            add("implementation", target.libs.findLibrary("androidx-compose-animation").get())
            add("implementation", target.libs.findLibrary("androidx-compose-material3").get())
            add("implementation", target.libs.findLibrary("androidx-compose-runtime").get())
            add("implementation", target.libs.findLibrary("androidx-compose-ui").get())
        }
    }
}