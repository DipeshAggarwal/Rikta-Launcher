plugins {
    id("convention.android.library")
    id("convention.compose")
    id("convention.hilt")
}

android {
    namespace = "com.lumina.feature.apphiding"
}

dependencies {
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(project(":core:core-android"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-model"))
    implementation(project(":core:core-ui"))

    implementation(project(":domain:domain-apps"))
    implementation(project(":domain:domain-settings"))
}
