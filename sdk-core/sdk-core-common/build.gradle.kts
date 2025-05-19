import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`

    id("gg.sunken.base-conventions")
    id("gg.sunken.publishing-conventions")
    id("gg.sunken.ci-props")

    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()

    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven { url = uri("https://redempt.dev") }
}

dependencies {
    api(libs.adventure)
    api(libs.adventure.minimessage)
    api(libs.annotations)
    api(libs.gson)
    api(libs.guice)
    api(libs.guice.assistedinject)
    api(libs.guice.grapher)
    api(libs.mongo)
    api(libs.influx)
    api(libs.caffeine)
    api(libs.crunch)
    api(libs.boostedyml)
    api(libs.objenesis)
    api(libs.slf4j.api)
    api(libs.lombok)
    api(libs.libby)
    annotationProcessor(libs.lombok)
    api(libs.auto.service.annotations)
    annotationProcessor(libs.auto.service.annotations)
}

tasks.shadowJar {
    archiveClassifier.set("")

    relocate("com.alessiodp.libby", "gg.sunken.sdk.dependencies.libs.libby")
    relocate("org.jetbrains.annotations", "gg.sunken.sdk.dependencies.libs.annotations")
    relocate("com.google", "gg.sunken.sdk.dependencies.libs.google")
    relocate("com.mongodb", "gg.sunken.sdk.dependencies.libs.mongo")
    relocate("com.influxdb", "gg.sunken.sdk.dependencies.libs.influxdb")
    relocate("redempt.crunch", "gg.sunken.sdk.dependencies.libs.crunch")
    relocate("com.github.benmanes.caffeine", "gg.sunken.sdk.dependencies.libs.caffeine")
    relocate("dev.dejvokep.boostedyaml", "gg.sunken.sdk.dependencies.libs.boostedyaml")
    relocate("org.objenesis", "gg.sunken.sdk.dependencies.libs.objenesis")
    relocate("org.slf4j", "gg.sunken.sdk.dependencies.libs.slf4j")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

abstract class GenerateDependencyInfo : DefaultTask() {

    @get:InputFile
    abstract val tomlFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val inputFile = tomlFile.get().asFile
        val outputFile = outputDir.get().file("gg/sunken/sdk/dependencies/DependencyInfo.java").asFile

        val lines = inputFile.readLines()
        val versions = mutableMapOf<String, String>()
        val libraries = mutableMapOf<String, Triple<String, String, String>>()

        var currentBlock: String? = null

        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("[") -> {
                    currentBlock = when {
                        trimmed.startsWith("[versions]") -> "versions"
                        trimmed.startsWith("[libraries]") -> "libraries"
                        else -> null
                    }
                }

                currentBlock == "versions" && "=" in trimmed -> {
                    val (key, value) = trimmed.split("=", limit = 2).map { it.trim().removeSurrounding("\"") }
                    versions[key] = value
                }

                currentBlock == "libraries" && "=" in trimmed -> {
                    val (name, def) = trimmed.split("=", limit = 2).map { it.trim() }
                    val moduleMatch = Regex("""module\s*=\s*"([^:]+):([^"]+)"""").find(def)
                    val versionRefMatch = Regex("""version\.ref\s*=\s*"([^"]+)"""").find(def)

                    if (moduleMatch != null && versionRefMatch != null) {
                        val (groupId, artifactId) = moduleMatch.destructured
                        val versionKey = versionRefMatch.groupValues[1]
                        libraries[name] = Triple(groupId, artifactId, versionKey)
                    }
                }
            }
        }

        outputFile.parentFile.mkdirs()
        outputFile.writeText(buildString {
            appendLine("package gg.sunken.sdk.dependencies;")
            appendLine()
            appendLine("public class DependencyInfo {")

            libraries.toSortedMap().forEach { (name, triple) ->
                val (group, artifact, versionKey) = triple
                val version = versions[versionKey] ?: error("Missing version for key: $versionKey")
                val constPrefix = name.uppercase().replace(Regex("[^A-Z0-9_]"), "_")

                appendLine("    public static final String ${constPrefix}_GROUP = \"$group\";")
                appendLine("    public static final String ${constPrefix}_ARTIFACT = \"$artifact\";")
                appendLine("    public static final String ${constPrefix}_VERSION = \"$version\";")
                appendLine()
            }

            appendLine("}")
        })
    }
}

val generateDependencyInfo by tasks.registering(GenerateDependencyInfo::class) {
    tomlFile.set(rootProject.layout.projectDirectory.file("gradle/libs.versions.toml"))
    outputDir.set(layout.buildDirectory.dir("generated/sources/dependency-info"))
}

sourceSets["main"].java.srcDir(generateDependencyInfo.map { it.outputDir })

tasks.create("prepareKotlinBuildScriptModel") { }