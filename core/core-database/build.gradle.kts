plugins {
    id("convention.android.library")
    id("convention.hilt")
    id("convention.room")
    id("convention.test.android")
}

dependencies {
    implementation(project(":core:core-model"))
    implementation(project(":domain:domain-profiles"))

    androidTestImplementation(project(":core:core-testing"))
}
