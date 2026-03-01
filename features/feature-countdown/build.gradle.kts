plugins {
    id("convention.android.library")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.lumina.feature.countdown"
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
    implementation(project(":core:core-ui"))

    implementation(project(":data:data-countdown"))
    implementation(project(":data:data-settings"))

    implementation(project(":domain:domain-countdown"))
    implementation(project(":domain:domain-settings"))

    implementation(project(":core:core-common"))
    implementation(project(":core:core-ui"))

    implementation(project(":features:feature-apppicker"))
}
