plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(kotlin("gradle-plugin"))
}

gradlePlugin {
    plugins {
        register("androidLibraryConvention") {
            id = "convention.android.library"
            implementationClass = "convention.android.AndroidLibraryConventionPlugin"
        }

        register("kotlinLibraryConvention") {
            id = "convention.kotlin.library"
            implementationClass = "convention.kotlin.KotlinLibraryConventionPlugin"
        }

        register("conventionCompose") {
            id = "convention.compose"
            implementationClass = "convention.android.ComposeConventionPlugin"
        }

        register("conventionHilt") {
            id = "convention.hilt"
            implementationClass = "convention.android.HiltConventionPlugin"
        }

        register("conventionRoom") {
            id = "convention.room"
            implementationClass = "convention.android.RoomConventionPlugin"
        }

        register("conventionUnitTest") {
            id = "convention.test.unit"
            implementationClass = "convention.android.UnitTestConventionPlugin"
        }

        register("conventionAndroidTest") {
            id = "convention.test.android"
            implementationClass = "convention.android.AndroidTestConventionPlugin"
        }
    }
}
