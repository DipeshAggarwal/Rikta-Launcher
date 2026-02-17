plugins {
    id("convention.android.library")
}

android {
    namespace = "com.lumina.feature.apppicker"
}

dependencies {
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(project(":core:core-common"))
    implementation(project(":core:core-ui"))

    implementation(project(":domain:domain-apps"))
    implementation(libs.androidx.compose.foundation.layout)
}
