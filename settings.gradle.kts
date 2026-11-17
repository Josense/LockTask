pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google\\.android.*")
                includeGroupByRegex("com\\.google\\.gms.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "LockTask"
include(":app")

include(":task:domain")
project(":task:domain").projectDir = file("feature/task/domain")
include(":task:data")
project(":task:data").projectDir = file("feature/task/data")
include(":task:presentation")
project(":task:presentation").projectDir = file("feature/task/presentation")

include(":notification:domain")
project(":notification:domain").projectDir = file("feature/notification/domain")
include(":notification:data")
project(":notification:data").projectDir = file("feature/notification/data")
include(":notification:presentation")
project(":notification:presentation").projectDir = file("feature/notification/presentation")

include(":core:designsystem")
project(":core:designsystem").projectDir = file("core/designsystem")

project(":task").projectDir = file("feature/task")
project(":notification").projectDir = file("feature/notification")
