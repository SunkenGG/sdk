import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    java
    `java-library`

    id("gg.sunken.minecraft-conventions")
    id("gg.sunken.publishing-conventions")
    id("gg.sunken.dependency-info")
    id("gg.sunken.ci-props")
    alias(libs.plugins.shadow)
}

val shade by configurations.creating

repositories {
    mavenCentral()
    // todo
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")

    maven("https://repo.alessiodp.com/snapshots")
    maven("https://redempt.dev")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.xenondevs.xyz/releases") }

dependencies {
    compileOnlyApi(project(":sdk-core:sdk-core-paper"))
    annotationProcessor(libs.auto.service)

    shade("com.alessiodp.libby:libby-core:2.0.0-SNAPSHOT")
    shade("com.alessiodp.libby:libby-standalone:2.0.0-SNAPSHOT")

    shade(rootProject.libs.crunch)
    shade(rootProject.libs.cloud.core)
    shade(rootProject.libs.cloud.paper)
    shade(rootProject.libs.cloud.annotations)
    annotationProcessor(rootProject.libs.cloud.annotations)

    shade(rootProject.libs.guice) {
        isTransitive = false
    }

    shade(rootProject.libs.guice.assistedinject) {
        isTransitive = false
    }

    shade(project(":sdk-core:sdk-core-paper")) {
        isTransitive = false
    }

    shade(project(":sdk-core:sdk-core-common")) {
        isTransitive = false
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    mergeServiceFiles()
    dependsOn(":sdk-core:sdk-core-paper:shadowJar")
    isZip64 = true
}

tasks.register<ShadowJar>("dynamic") {
    group = "gg.sunken"
    dependsOn("generateDependencyInfo", "classes")

    archiveClassifier.set("dynamic")

    mergeServiceFiles()

    val generatedDir = file("build/generated/sources/dependency-info")
    outputs.dir(generatedDir)

    // Shade dependencies
    configurations = listOf(shade)

    // Include project
    from(sourceSets["main"].output)
    from(sourceSets["main"].resources)

    // Add dependencies.json
    from(generatedDir)

    // Relocations
    relocate("com.google.inject", "gg.sunken.lib.guice")
    relocate("com.alessiodp.libby", "gg.sunken.lib.libby")
    relocate("redempt.crunch", "gg.sunken.lib.crunch")
    relocate("io.leangen.geantyref", "gg.sunken.lib.geantyref")
}

tasks.build {
    dependsOn("dynamic")
    dependsOn("shadowJar")
}

paper {
    name = "sunken-sdk-paper"
    main = "gg.sunken.platform.PaperPlatformPlugin"
    bootstrapper = "gg.sunken.platform.PaperPlatformBootstrap"
    loader = "gg.sunken.platform.PaperPlatformPluginLoader"
    version = project.version.toString()
    apiVersion = "1.20"
    description = "A platform for the Sunken SDK on PaperMC"
    authors = listOf("Sunken Developers")
    serverDependencies {
        register("ProtocolLib") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }

        register("ProtocolSupport") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }

        register("ViaVersion") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }

        register("ViaBackwards") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }

        register("ViaRewind") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }

        register("Geyser-Spigot") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
    }
}

tasks.register("prepareKotlinBuildScriptModel") { }