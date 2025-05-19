import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`

    id("gg.sunken.minecraft-base-conventions")
    id("gg.sunken.publishing-conventions")
    id("gg.sunken.ci-props")
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
    maven { url = uri("https://redempt.dev") }
    maven { url = uri("https://repo.codemc.io/repository/maven-public/") }
    maven { url = uri("https://repo.codemc.io/repository/maven-snapshots/") }
    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }
    maven { url = uri("https://repo.alessiodp.com/snapshots") }
}

dependencies {
    api(project(":sdk-core:sdk-core-common"))

    api(libs.bundles.cloud)
    api(libs.cloud.paper)
    api(libs.packetevents)
    api(libs.nbtapi)

    annotationProcessor(libs.cloud.annotations)
}

tasks.shadowJar {
    archiveClassifier.set("")

    relocate("org.incendo.cloud", "gg.sunken.sdk.dependencies.libs.cloud")
    relocate("com.github.retrooper.packetevents", "gg.sunken.sdk.dependencies.libs.packetevents.api")
    relocate("io.github.retrooper.packetevents", "gg.sunken.sdk.dependencies.libs.packetevents.impl")
    relocate("de.tr7zw.nbtapi", "gg.sunken.sdk.dependencies.libs.nbtapi")
}