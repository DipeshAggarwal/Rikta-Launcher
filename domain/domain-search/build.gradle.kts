plugins {
    id("convention.kotlin.library")
}
dependencies {
    implementation(libs.jakarta.inject)

    implementation(project(":core:core-common"))

    implementation(project(":domain:domain-apps"))
}
