plugins {
    id("convention.kotlin.library")
    id("convention.test.unit")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.jakarta.inject)

    implementation(project(":core:core-common"))
    implementation(project(":core:core-model"))

    implementation(project(":domain:domain-apps"))
    implementation(project(":domain:domain-countdown"))
    implementation(project(":domain:domain-profiles"))
    implementation(project(":domain:domain-settings"))
    implementation(project(":domain:domain-shortcut"))
}
