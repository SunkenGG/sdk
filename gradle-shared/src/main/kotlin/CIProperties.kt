import org.gradle.api.Project
import java.io.OutputStream

/**
 * A data class to hold the CI properties of the project.
 */
data class CIProperties(
    /**
     * The first 8 characters of the commit hash.
     */
    val commitSHA: String,

    /**
     * The branch or tag name for which the project is build.
     */
    val commitRef: String,

    /**
     * The default branch name for the project.
     */
    val defaultBranch: String,

    /**
     * The ID of the CI Pipeline building the project.
     */
    val ciPipelineId: String
) {
    companion object {
        /** Whether this action is running on a CI/CD Server. */
        val runningOnCi get() = System.getenv("CI") != null

        fun getVersionInfo(): String = when (val ciProperties = fromEnvironment()) {
            is CIProperties -> when (ciProperties.commitRef) {
                ciProperties.defaultBranch -> ""
                "develop" -> "-SNAPSHOT+${ciProperties.ciPipelineId}"
                else -> "+${ciProperties.commitRef}.${ciProperties.commitSHA}"
            }

            else -> "+local"
        }

        fun fromEnvironment(): CIProperties? {
            if(!runningOnCi) {
                return null
            }

            return CIProperties(
                commitSHA = getEnvironmentVariable("CI_COMMIT_SHORT_SHA"),
                commitRef = getEnvironmentVariable("CI_COMMIT_REF_SLUG"),
                defaultBranch = getEnvironmentVariable("CI_DEFAULT_BRANCH"),
                ciPipelineId = getEnvironmentVariable("CI_PIPELINE_ID")
            )
        }

        private fun getEnvironmentVariable(name: String): String {
            return System.getenv(name) ?: error("$name is missing from the CI variables")
        }

        internal fun ensureInit(project: Project, outputStream: OutputStream) {
            // Functionally does nothing, but forces this class to be initialized inside internal build scripts
            outputStream.write(project.name.length)
        }
    }
}
