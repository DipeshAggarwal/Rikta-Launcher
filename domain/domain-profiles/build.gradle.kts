plugins {
    id("convention.kotlin.library")
}
dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.jakarta.inject)

    implementation(project(":core:core-common"))
    implementation(project(":core:core-model"))

    implementation(project(":domain:domain-apps"))
}
