plugins {
    id("convention.android.library")
    id("convention.compose")
    id("convention.hilt")
    id("convention.test.unit")
}

dependencies {
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.play.services.location)

    implementation(project(":core:core-android"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-logging"))
    implementation(project(":core:core-model"))

    implementation(project(":domain:domain-coordination"))
    implementation(project(":domain:domain-profiles"))
    implementation(project(":domain:domain-usage"))

    testImplementation(project(":core:core-testing"))
}
