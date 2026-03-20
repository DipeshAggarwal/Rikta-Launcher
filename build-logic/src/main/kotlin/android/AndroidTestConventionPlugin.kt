package convention.android

import com.android.build.api.dsl.LibraryExtension
import convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidTestConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.extensions.configure<LibraryExtension> {
            defaultConfig {
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
        }

        target.dependencies {
            add("androidTestImplementation", target.libs.findLibrary("androidx-junit").get())
            add("androidTestImplementation", target.libs.findLibrary("androidx-test-core").get())
            add("androidTestImplementation", target.libs.findLibrary("androidx-test-runner").get())
            add("androidTestImplementation", target.libs.findLibrary("mockk-android").get())
            add("androidTestImplementation", target.libs.findLibrary("kotlinx-coroutines-test").get())
        }
    }
}
