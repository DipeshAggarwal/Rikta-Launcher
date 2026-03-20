package convention.android

import com.android.build.api.dsl.LibraryExtension
import convention.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class AndroidLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.pluginManager.apply("com.android.library")

        val baseNamespace = target.providers
            .gradleProperty("BASE_NAMESPACE")
            .get()

        val moduleNamespace = target.path
            .removePrefix(":")
            .replace(":", ".")
            .replace("-", ".")

        val fullNamespace = "$baseNamespace.$moduleNamespace"

        target.extensions.configure<LibraryExtension> {
            namespace = fullNamespace

            compileSdk = 36
            defaultConfig {
                minSdk = 26
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }

        target.extensions.configure<KotlinAndroidProjectExtension> {
            jvmToolchain(17)
        }

        target.dependencies {
            add("implementation", target.libs.findLibrary("androidx-core-ktx").get())
            add("implementation", target.libs.findLibrary("androidx-lifecycle-runtime").get())

            add("implementation", target.libs.findLibrary("kotlinx-coroutines-core").get())
        }
    }
}
