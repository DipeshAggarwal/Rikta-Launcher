plugins {
    id("convention.android.library")
    id("convention.compose")
    id("convention.hilt")
}

android {
    namespace = "com.lumina.feature.appfavourite"
}

dependencies {
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(project(":core:core-android"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-logging"))
    implementation(project(":core:core-model"))
    implementation(project(":core:core-ui"))

    implementation(project(":domain:domain-apps"))
    implementation(project(":domain:domain-coordination"))
    implementation(project(":domain:domain-profiles"))
    implementation(project(":domain:domain-settings"))
}
