import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin.Companion.shadowJar

plugins {
    `java-library`
    id("io.freefair.lombok")
    id("com.gradleup.shadow")
}

version = "${project.property("version")}${CIProperties.getVersionInfo()}"

repositories {
    mavenCentral()
}



java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(JAVA_VERSION))
    }

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

//    withJavadocJar() TODO
    withSourcesJar()
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(JAVA_VERSION)
    }

    javadoc {
        (options as StandardJavadocDocletOptions)
            .tags("apiNote:a:API:", "implSpec:a:Implementation Requirements:", "implNote:a:Implementation Note:")
    }

    withType<Test> {
        useJUnitPlatform()
    }

    shadowJar {
        relocate("com.google.inject", "gg.sunken.lib.guice")
    }
}