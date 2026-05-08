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
        id("com.android.application") version "9.2.1"
        id("com.android.library") version "9.2.1"
        id("org.jetbrains.kotlin.android") version "2.3.21"
        id("org.jetbrains.kotlin.jvm") version "2.3.21"
        id("org.jetbrains.kotlin.plugin.compose") version "2.3.21"
        id("com.google.dagger.hilt.android") version "2.59.2"
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
include(":core:core-database")
include(":core:core-datastore")
include(":core:core-logging")
include(":core:core-model")
include(":core:core-testing")
include(":core:core-ui")

// domain module
include(":domain:domain-apps")
include(":domain:domain-coordination")
include(":domain:domain-countdown")
include(":domain:domain-profiles")
include(":domain:domain-screentime")
include(":domain:domain-search")
include(":domain:domain-shortcut")
include(":domain:domain-settings")
include(":domain:domain-usage")

// data module
include(":data:data-apps")
include(":data:data-coordination")
include(":data:data-countdown")
include(":data:data-profiles")
include(":data:data-screentime")
include(":data:data-settings")
include(":data:data-shortcut")
include(":data:data-usage")

// features module
include(":features:feature-apphiding")
include(":features:feature-appfavourite")
include(":features:feature-appcountdown")
include(":features:feature-home")
include(":features:feature-onboarding")
include(":features:feature-profiles")
include(":features:feature-screentime")
include(":features:feature-settings")
include(":features:feature-system")
