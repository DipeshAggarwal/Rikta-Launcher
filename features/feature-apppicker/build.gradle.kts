plugins {
    id("convention.android.library")
}

android {
    namespace = "com.lumina.feature.apppicker"
}

dependencies {
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.foundation.layout)

    implementation(project(":core:core-common"))
    implementation(project(":core:core-model"))
    implementation(project(":core:core-ui"))
}
