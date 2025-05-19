rootProject.name = "sdk"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    includeBuild("gradle-shared")
    includeBuild("gradle-tooling")
}

include(
    ":sdk-core",
    ":sdk-core:sdk-core-common",
    ":sdk-core:sdk-core-paper",
    ":sdk-core:sdk-core-velocity",

    ":platform",
    ":platform:platform-paper",
    ":platform:platform-velocity"
)