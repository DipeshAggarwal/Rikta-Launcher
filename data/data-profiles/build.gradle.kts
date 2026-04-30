plugins {
    id("convention.android.library")
    id("convention.hilt")
    id("convention.room")
    id("convention.test.unit")
}

dependencies {
    implementation(libs.androidx.datastore.preferences)

    implementation(project(":core:core-common"))
    implementation(project(":core:core-database"))
    implementation(project(":core:core-logging"))
    implementation(project(":core:core-model"))

    implementation(project(":domain:domain-profiles"))

    testImplementation(project(":core:core-testing"))
}
