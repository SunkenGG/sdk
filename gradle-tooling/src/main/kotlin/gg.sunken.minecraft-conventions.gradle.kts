import xyz.jpenilla.runpaper.task.RunServer

plugins {
    id("gg.sunken.minecraft-base-conventions")
    id("de.eldoria.plugin-yml.paper")
    id("xyz.jpenilla.run-paper")
}

tasks {
    named<RunServer>("runServer") {
        minecraftVersion(libs.versions.paper.api.get().dropLast(14))
    }
}