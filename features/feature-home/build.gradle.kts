plugins {
    id("convention.android.library")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.lumina.feature.home"
}

dependencies {
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(project(":core:core-android"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-logging"))
    implementation(project(":core:core-model"))
    implementation(project(":core:core-ui"))

    implementation(project(":domain:domain-apps"))
    implementation(project(":domain:domain-countdown"))
    implementation(project(":domain:domain-search"))
    implementation(project(":domain:domain-settings"))
    implementation(project(":domain:domain-coordination"))

    implementation(project(":features:feature-apppicker"))
}
