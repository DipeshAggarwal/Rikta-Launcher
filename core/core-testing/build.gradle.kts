plugins {
    id("convention.android.library")
}

dependencies {
    implementation(project(":core:core-database"))
    implementation(project(":core:core-model"))

    implementation(project(":domain:domain-profiles"))
    implementation(project(":domain:domain-usage"))
}
