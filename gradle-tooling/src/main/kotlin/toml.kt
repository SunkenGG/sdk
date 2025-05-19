/**
 * A very minimal and bare bones toml parser for reading simple values, required for libs.versions.toml
 * This parser only supports parsing individual lines
 * @author santio
 */
object toml {

    fun variable(content: String): Pair<String, String> {
        val data = content.split("=", limit = 2).map { it.trim() }
        if (data.size != 2) error("Invalid version line: $content")
        return data[0] to data[1].removeSurrounding("\"").removeSurrounding("'")
    }

    fun table(content: String): Map<String, String> {
        if (!content.startsWith('{') && !content.endsWith('}')) error("Invalid table content: $content")

        val content = content.removeSurrounding("{", "}")
        val variables = content.split(",").map { it.trim() }

        return variables.associate { variable(it) }
    }

}
