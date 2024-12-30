package it.krzeminski.snakeyaml.engine.kmp

import CommonTestResources
import okio.ByteString

fun stringFromResources(path: String): String {
    println("Loading resource: $path")
    val segments = path.split("/")
    println("Segments: $segments")
    return (segments
        .dropLast(1)
        .fold(CommonTestResources().resourcesMap) { map, segment ->
            @Suppress("UNCHECKED_CAST")
            val next = map[segment] as Map<String, Any>
            println("Going deeper: $segment -> $next")
            next
        }[segments.last()] as ByteString).utf8()
        .replace(Regex("\\r\\n?"), "\n")
}
