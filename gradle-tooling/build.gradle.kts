plugins {
    `kotlin-dsl`
    `version-catalog`
    `maven-publish`
    id("gg.sunken.ci-props")
}

// On main: <version>
// On develop: <version>-SNAPSHOT+<build number>
// On other branches: <version>+<commit branch>.<commit hash>
// Locally: <version>+local
version = "${project.property("version")}${CIProperties.getVersionInfo()}"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

fun convertPlugin(plugin: Provider<PluginDependency>): String {
    val id = plugin.get().pluginId
    return "$id:$id.gradle.plugin:${plugin.get().version}"
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(convertPlugin(libs.plugins.dokka))
    implementation(convertPlugin(libs.plugins.gitversion))
    implementation(convertPlugin(libs.plugins.kotlin.jvm))
    implementation(convertPlugin(libs.plugins.lombok))
    implementation(convertPlugin(libs.plugins.plugin.yml.paper))
    implementation(convertPlugin(libs.plugins.runpaper))
    implementation(convertPlugin(libs.plugins.shadow))
    implementation(convertPlugin(libs.plugins.userdev))

    implementation(libs.gson)
    implementation(libs.kotlin.gradle.plugin)

    // This is a cheap and nasty hack, but because the build itself depends on gradle-shared being built first,
    // we can guarantee the JAR will exist by the time we try and depend on it.
    // That said, this needs to be queried to someone at gradle because there has to be a better way than this shit...
    implementation(files("../gradle-shared/build/libs/gradle-shared.jar"))
}

catalog {
    versionCatalog {
        from(files("../gradle/libs.versions.toml"))
        library("cow-bom", "gg.sunken:cow-bom:$version")
        plugin("cow-conventions-base", "gg.sunken.base-conventions").version("$version")
        plugin("cow-conventions-publish", "gg.sunken.publishing-conventions").version("$version")
        plugin("cow-conventions-minecraft", "gg.sunken.minecraft-conventions").version("$version")
        plugin("cow-conventions-minecraft-base", "gg.sunken.minecraft-base-conventions").version("$version")
    }
}

publishing {
    publications {
        create<MavenPublication>("vercat") {
            groupId = project.group as String
            artifactId = "version-catalog"
            version = project.version as String
            from(components["versionCatalog"])
        }
    }
}
