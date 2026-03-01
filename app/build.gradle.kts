import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val baseVersionCode = "2.3.1"

android {
    namespace = "com.lumina.rikta"
    compileSdk = 36

    lint {
        baseline = file("lint-baseline.xml")
    }

    defaultConfig {
        applicationId = "com.lumina.rikta"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = baseVersionCode
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        resValue("string", "app_version", baseVersionCode)
        resValue("string", "app_name", "Escape Launcher")
        resValue("string", "app_flavour", "Unknown Flavor")
        resValue("string", "empty", "")
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    flavorDimensions += listOf("version", "distribution")
    productFlavors{
        create("dev"){
            applicationIdSuffix = ".dev"
            dimension = "version"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "Escape Launcher Dev")
        }
        create("prod"){
            dimension = "version"
            applicationIdSuffix = ""
        }
        create("google") {
            dimension = "distribution"
            buildConfigField("boolean", "IS_FOSS", "false")
            resValue("string", "app_flavour", "Google API")
        }
        create("foss") {
            dimension = "distribution"
            versionNameSuffix = "-foss"
            buildConfigField("boolean", "IS_FOSS", "true")
            resValue("string", "app_flavour", "FOSS")
        }
    }

    sourceSets {
        getByName("foss") {
            res.directories.add("src/foss/res")
            java.directories.add("src/foss/java")
        }
        getByName("google") {
            res.directories.add("src/google/res")
            java.directories.add("src/google/java")
        }
    }
    
    androidComponents.beforeVariants { variantBuilder ->
        val flavorVersion = variantBuilder.productFlavors.find { it.first == "version" }?.second
        val buildType = variantBuilder.buildType

        if ((flavorVersion == "prod" && buildType == "debug") ||
            (flavorVersion == "dev" && buildType == "release")) {
            variantBuilder.enable = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/gradle/incremental.annotation.processors"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
}

// Apply Google-specific configurations from secondary file
val taskNames = gradle.startParameter.taskNames
val isFoss = taskNames.any { it.contains("foss", ignoreCase = true) }
if (!isFoss) {
    apply(from = "google.gradle")
}

dependencies {
    implementation(project(":core:core-android"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-datastore"))
    implementation(project(":core:core-logging"))
    implementation(project(":core:core-ui"))

    implementation(project(":data:data-apps"))
    implementation(project(":data:data-countdown"))
    implementation(project(":data:data-settings"))
    implementation(project(":data:data-system"))

    implementation(project(":domain:domain-apps"))
    implementation(project(":domain:domain-countdown"))
    implementation(project(":domain:domain-search"))
    implementation(project(":domain:domain-settings"))
    implementation(project(":domain:domain-system"))

    implementation(project(":features:feature-apphiding"))
    implementation(project(":features:feature-apppicker"))
    implementation(project(":features:feature-appfavourite"))
    implementation(project(":features:feature-countdown"))
    implementation(project(":features:feature-home"))
    implementation(project(":features:feature-settings"))

    // Core Android Libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.kotlinx.coroutines.core)

    // Material Design and UI Libraries
    implementation(libs.google.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.graphics.shapes)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(platform(libs.androidx.compose.bom))

    // Lifecycle and Activity Libraries
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.activity.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.jakarta.inject)
    ksp(libs.hilt.compiler)

    // Third-Party Library
    implementation(libs.material.kolor)

    // Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit)

    // Debugging Tools
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // TEMPORARY MIGRATION DEPENDENCIES
    implementation(libs.google.gson)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.register("testClasses") {
    group = "verification"
    description = "Test classes for all variants."
    dependsOn(
        tasks.matching { it.name.startsWith("compile") && it.name.endsWith("UnitTestSources") } )}
