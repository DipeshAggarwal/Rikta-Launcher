plugins {
    id("convention.android.library")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.lumina.feature.settings"
}

dependencies {
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    ksp(libs.hilt.compiler)

    implementation(project(":core:core-android"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-ui"))

    implementation(project(":data:data-settings"))

    implementation(project(":domain:domain-settings"))

    implementation(project(":features:feature-apppicker"))
}
