plugins {
    id("convention.kotlin.library")
}
dependencies {
    implementation(project(":core:core-common"))

    implementation(project(":domain:domain-apps"))
}
