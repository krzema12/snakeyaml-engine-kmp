package it.krzeminski.snakeyaml.engine.kmp

import CommonTestResources
import okio.ByteString

/**
 * Retrieves a string content from common resources using a given path.
 * TODO: Add proper error handling if the resource doesn't exist.
 *
 * @param path The slash-delimited path to the resource.
 * @return The content of the resource as a UTF-8 encoded string with normalized line breaks.
 */
fun stringFromResources(path: String): String {
    val segments = path.split("/")
    return (segments
        .dropLast(1)
        .fold(CommonTestResources.resourcesMap) { map, segment ->
            @Suppress("UNCHECKED_CAST")
            map[segment] as Map<String, Any>
        }[segments.last()] as ByteString).utf8()
        // In this particular library, we produce uniform line breaks (\n)
        // on all OSes, hence it's desired to normalize them even if in
        // the resource file, we have a different kind of line breaks.
        .replace(Regex("\\r\\n?"), "\n")
}
