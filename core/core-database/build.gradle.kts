plugins {
    id("convention.android.library")
    id("convention.hilt")
    id("convention.room")
    id("convention.test.android")
}

dependencies {
    implementation(project(":core:core-model"))

    androidTestImplementation(project(":core:core-testing"))
}
