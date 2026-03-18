plugins {
    id("convention.android.library")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    implementation(project(":core:core-model"))

    implementation(project(":domain:domain-profiles"))
    implementation(project(":domain:domain-usage"))
}
