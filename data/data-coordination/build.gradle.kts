plugins {
    id("convention.android.library")
    id("convention.hilt")
}

dependencies {
    implementation(project(":core:core-android"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-logging"))
    implementation(project(":core:core-model"))

    implementation(project(":domain:domain-apps"))
    implementation(project(":domain:domain-coordination"))
    implementation(project(":domain:domain-countdown"))

    implementation(project(":data:data-countdown"))
}
