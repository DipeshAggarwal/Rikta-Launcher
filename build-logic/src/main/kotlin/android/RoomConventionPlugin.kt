package convention.android

import convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class RoomConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.pluginManager.apply("com.google.devtools.ksp")

        target.dependencies {
            add("implementation", target.libs.findLibrary("androidx-room-runtime").get())
            add("implementation", target.libs.findLibrary("androidx-room-ktx").get())
            add("ksp", target.libs.findLibrary("androidx-room-compiler").get())
        }
    }
}
