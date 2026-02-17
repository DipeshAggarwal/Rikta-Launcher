plugins {
    id("convention.android.library")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.lumina.feature.appfavourite"
}

dependencies {
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(project(":core:core-android"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-ui"))

    implementation(project(":data:data-apps"))

    implementation(project(":domain:domain-apps"))
    implementation(project(":domain:domain-settings"))

    implementation(project(":features:feature-apppicker"))
}
