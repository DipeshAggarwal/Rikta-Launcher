plugins {
    id("convention.android.library")
    id("convention.compose")
}

android {
    namespace = "com.lumina.core.ui"
}

dependencies {
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.google.material)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)

    implementation(libs.material.kolor)

    implementation(project(":core:core-common"))
}
