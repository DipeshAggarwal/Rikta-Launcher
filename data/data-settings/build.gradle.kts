plugins {
    id("convention.android.library")
    id("convention.hilt")
}

dependencies {
    implementation(libs.androidx.datastore.preferences)

    implementation(project(":core:core-android"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-datastore"))
    implementation(project(":core:core-logging"))

    implementation(project(":domain:domain-settings"))
}
