plugins {
    `maven-publish`
}

val GH_MVN_PKGS = "https://maven.pkg.github.com/"

configure<PublishingExtension> {
    publications {
        register<MavenPublication>("maven") {
            groupId = project.group as String
            artifactId = extensions.getByName<BasePluginExtension>("base").archivesName.get()
            version = project.version as String
            from(components["java"])
        }
    }

    repositories {
        val ghActionRepo = System.getenv("GITHUB_ACTION_REPOSITORY")

        if (ghActionRepo != null) {
            maven {
                name = "gh-packages"
                url = uri("$GH_MVN_PKGS$ghActionRepo")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}
