plugins {
    id("convention.android.library")
    id("convention.hilt")
    id("convention.test.unit")
}

dependencies {
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.common)

    implementation(project(":core:core-android"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-database"))
    implementation(project(":core:core-logging"))
    implementation(project(":core:core-model"))

    implementation(project(":domain:domain-usage"))
}
