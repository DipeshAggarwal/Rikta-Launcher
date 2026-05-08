plugins {
    id("convention.android.library")
    id("convention.hilt")
}

dependencies {
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.work.runtime)

    implementation(project(":core:core-common"))
}
