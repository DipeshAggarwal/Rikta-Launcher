plugins {
    id("convention.android.library")
    id("convention.compose")
}

android {
    namespace = "com.lumina.feature.apppicker"
}

dependencies {
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(project(":core:core-common"))
    implementation(project(":core:core-model"))
    implementation(project(":core:core-ui"))
}
