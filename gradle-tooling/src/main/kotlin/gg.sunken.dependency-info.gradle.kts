import com.google.gson.GsonBuilder

abstract class GenerateDependencyInfo : DefaultTask() {

    @get:InputFile
    abstract val tomlFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val repositories: SetProperty<String>

    @get:Input
    abstract val exclusions: SetProperty<String>

    private fun getDependencies(): Set<Dependency> {
        val inputFile = tomlFile.get().asFile

        val versions = mutableMapOf<String, String>()
        val dependencies = mutableSetOf<Dependency>()

        val lines = inputFile.readLines()
        var currentBlock: Section? = null

        for (line in lines) {
            val line = line.trim()
            if (line.isBlank() || line.startsWith('#')) continue

            if (line.startsWith('[') && line.endsWith(']')) {
                val key = line.removeSurrounding("[", "]")
                currentBlock = Section.values().firstOrNull { it.key == key } ?: error("Invalid toml key: $key")
                continue
            }

            if (currentBlock == null) error("Invalid line, expected header: $line")
            if (currentBlock.ignore) continue

            when (currentBlock) {
                Section.Version -> {
                    val data = toml.variable(line)
                    versions[data.first] = data.second
                }
                Section.Library -> {
                    val library = toml.variable(line)
                    val key = library.first
                    val data = toml.table(library.second)

                    val module = data["module"] ?: error("No module specified in library $key")
                    val version = data["version"]
                        ?: data["version.ref"]?.let { "ref:$it" }
                        ?: error("No version specified in module $key")

                    val moduleData = module.split(":", limit = 2).takeIf { it.size == 2 }
                        ?: error("Invalid module specified in module $key, module: $module")

                    dependencies.add(Dependency(
                        group = moduleData[0],
                        artifact = moduleData[1],
                        version = version
                    ))
                }
                else -> error("No handling defined for $currentBlock")
            }
        }

        return dependencies
            .map { it.map(versions) }
            .filterNot { dep ->
                exclusions.get()
                    .mapNotNull { Exclusion.fromString(it) }
                    .any { it.matches(dep) }
            }
            .toSet()
    }

    @TaskAction
    fun generate() {
        val gson = GsonBuilder().serializeNulls().disableHtmlEscaping().create()
        val outputFile = outputDir.get().file("dependencies.json").asFile
        val dependencyInfo = DependencyInfo(
            this.repositories.get(),
            this.getDependencies()
        )

        outputFile.parentFile.mkdirs()
        outputFile.writeText(gson.toJson(dependencyInfo))
    }

    fun exclude(group: String? = null, artifact: String? = null, version: String? = null) {
        this.exclusions.add(Exclusion(group, artifact, version).toString())
    }

    private enum class Section(
        val key: String,
        val ignore: Boolean = false,
    ) {
        Version("versions"),
        Library("libraries"),
        Plugin("plugins", ignore = true),
        Bundle("bundles", ignore = true)
    }
}

data class DependencyInfo(
    val repositories: Set<String>,
    val dependencies: Set<Dependency>
)

data class Exclusion(
    val group: String?,
    val artifact: String?,
    val version: String?
) {
    override fun toString(): String {
        return "${group ?: ""}:${artifact ?: ""}:${version ?: ""}"
    }

    fun matches(dependency: Dependency): Boolean {
        if (this.group == null && this.artifact == null && this.version == null) return false

        if (this.group != null && this.group != dependency.group) return false
        if (this.artifact != null && this.artifact != dependency.artifact) return false
        if (this.version != null && this.version != dependency.version) return false

        return true
    }

    companion object {
        fun fromString(content: String): Exclusion? {
            val data = content.split(":")
            if (data.size != 3) return null

            return Exclusion(
                data[0].takeIf { it.isNotBlank() },
                data[1].takeIf { it.isNotBlank() },
                data[2].takeIf { it.isNotBlank() }
            )
        }
    }
}

data class Dependency(
    val group: String,
    val artifact: String,
    val version: String
) {
    override fun toString(): String {
        return "$group:$artifact:$version"
    }

    fun map(versions: Map<String, String>): Dependency {
        if (!version.startsWith("ref:")) return this

        val key = version.removePrefix("ref:")
        val version = versions[key]
            ?: error("Dependency $this requires version reference $key, yet it isn't defined")

        return this.copy(version = version)
    }
}

tasks.register<GenerateDependencyInfo>("generateDependencyInfo") {
    group = "gg.sunken"

    tomlFile.set(rootProject.layout.projectDirectory.file("gradle/libs.versions.toml"))
    outputDir.set(layout.buildDirectory.dir("generated/sources/dependency-info"))

    exclude(group = "com.github.Redempt", artifact = "Crunch") // No checksums!!! >:(

    this.repositories.set(
        rootProject.allprojects.flatMap { project ->
            project.buildscript.repositories.plus(project.repositories)
                .filterIsInstance<MavenArtifactRepository>()
                .map { it.url.toString() }
        }.toSet()
    )
}
