plugins {
    id("convention.android.library")
    id("convention.hilt")
}

dependencies {
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
