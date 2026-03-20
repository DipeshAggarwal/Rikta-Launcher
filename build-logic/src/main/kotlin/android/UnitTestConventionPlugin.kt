package convention.android

import convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class UnitTestConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.dependencies {
            add("testImplementation", target.libs.findLibrary("junit").get())
            add("testImplementation", target.libs.findLibrary("mockk").get())
            add("testImplementation", target.libs.findLibrary("kotlinx-coroutines-test").get())
        }
    }
}
