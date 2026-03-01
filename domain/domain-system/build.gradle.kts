plugins {
    id("convention.kotlin.library")
}
dependencies {
    implementation(libs.kotlinx.coroutines.core)

    implementation(project(":core:core-common"))

    implementation(project(":domain:domain-apps"))
}
