plugins {
    id("convention.android.library")
    id("convention.compose")
    id("convention.hilt")
}

android {
    namespace = "com.lumina.feature.settings"
}

dependencies {
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(project(":core:core-android"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-ui"))

    implementation(project(":domain:domain-settings"))

    implementation(project(":features:feature-apppicker"))
}
