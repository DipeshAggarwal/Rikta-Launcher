plugins {
    id("convention.android.library")
    id("convention.hilt")
    id("convention.test.android")
}

dependencies {
    implementation(libs.androidx.datastore.preferences)

    implementation(project(":core:core-android"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-database"))
    implementation(project(":core:core-logging"))
    implementation(project(":core:core-model"))

    implementation(project(":domain:domain-apps"))
}
