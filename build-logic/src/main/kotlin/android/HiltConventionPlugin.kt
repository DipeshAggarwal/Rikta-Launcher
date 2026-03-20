package convention.android

import convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class HiltConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.pluginManager.apply("com.google.devtools.ksp")
        target.pluginManager.apply("com.google.dagger.hilt.android")

        target.dependencies {
            add("implementation", target.libs.findLibrary("hilt-android").get())
            add("ksp", target.libs.findLibrary("hilt-compiler").get())
        }
    }
}
