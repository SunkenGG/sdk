package gg.sunken

import CIProperties
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.OutputStream

class CIPropsPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        CIProperties.ensureInit(target, NullOutputStream)
    }

    object NullOutputStream : OutputStream() {
        override fun write(b: Int) {}
    }

}
