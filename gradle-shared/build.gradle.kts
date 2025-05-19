plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("ciPropsInternal") {
            id = "gg.sunken.ci-props"
            implementationClass = "gg.sunken.CIPropsPlugin"
        }
    }
}
