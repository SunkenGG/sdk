plugins {
    `java-library`

    id("gg.sunken.minecraft-base-conventions")
    id("gg.sunken.publishing-conventions")
    id("gg.sunken.ci-props")
}

repositories {
    mavenCentral()
}

dependencies {
//    compileOnly(project(":sdk-bom:sdk-velocity-bom"))
}