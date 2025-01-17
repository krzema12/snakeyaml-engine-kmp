package it.krzeminski.snakeyaml.engine.kmp

import CommonTestResources
import okio.ByteString
import org.intellij.lang.annotations.Language

/**
 * Retrieves a string content from common resources using a given path.
 *
 * @param path The slash-delimited path to the resource, with a leading slash.
 * @return The content of the resource as a UTF-8 encoded string with normalized line breaks.
 * @throws IllegalArgumentException if the resource doesn't exist.
 */
fun stringFromResources(@Language("file-reference") path: String): String {
    require(path.startsWith("/")) { "A leading slash is required!" }
    val segments = path.drop(1).split("/")
    return (segments
        .dropLast(1)
        .fold(Pair(CommonTestResources.resourcesMap, "/resources/")) { (map, pathSoFar), directory ->
            require(directory in map) {
                "Directory '$directory' doesn't exist in directory '$pathSoFar'!"
            }
            @Suppress("UNCHECKED_CAST")
            Pair(map[directory] as Map<String, Any>, "$pathSoFar$directory/")
        }).let { (map, pathSoFar) ->
            val file = segments.last()
            require(file in map) {
                "File '$file' doesn't exist in directory '$pathSoFar'!"
            }
            map[file] as ByteString
        }.utf8()
        // In this particular library, we produce uniform line breaks (\n)
        // on all OSes, hence it's desired to normalize them even if in
        // the resource file, we have a different kind of line breaks.
        .replace(Regex("\\r\\n?"), "\n")
}
