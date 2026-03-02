includeBuild("build-logic")

pluginManagement {
    includeBuild("build-logic")

    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        google()
    }

    plugins {
        id("com.android.application") version "9.0.1"
        id("com.android.library") version "9.0.1"
        id("org.jetbrains.kotlin.android") version "2.3.10"
        id("org.jetbrains.kotlin.jvm") version "2.3.10"
        id("org.jetbrains.kotlin.plugin.compose") version "2.3.10"
        id("com.google.dagger.hilt.android") version "2.59.1"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Rikta Launcher"

// app
include(":app")

// core module
include(":core:core-android")
include(":core:core-common")
include(":core:core-datastore")
include(":core:core-logging")
include(":core:core-model")
include(":core:core-testing")
include(":core:core-ui")

// domain module
include(":domain:domain-apps")
include(":domain:domain-countdown")
include(":domain:domain-screentime")
include(":domain:domain-search")
include(":domain:domain-settings")
include(":domain:domain-coordination")

// data module
include(":data:data-apps")
include(":data:data-countdown")
include(":data:data-screentime")
include(":data:data-settings")
include(":data:data-coordination")

// features module
include(":features:feature-apphiding")
include(":features:feature-apppicker")
include(":features:feature-appfavourite")
include(":features:feature-appcountdown")
include(":features:feature-home")
include(":features:feature-onboarding")
include(":features:feature-screentime")
include(":features:feature-settings")
