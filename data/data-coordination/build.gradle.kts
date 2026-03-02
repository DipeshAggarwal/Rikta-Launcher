plugins {
    id("convention.android.library")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(project(":core:core-android"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-logging"))
    implementation(project(":core:core-model"))

    implementation(project(":domain:domain-apps"))
    implementation(project(":domain:domain-coordination"))
    implementation(project(":domain:domain-countdown"))

    implementation(project(":data:data-countdown"))
}
