package it.krzeminski.snakeyaml.engine.kmp

import CommonTestResources
import okio.ByteString

fun stringFromResources(path: String): String {
    val segments = path.split("/")
    return (segments
        .dropLast(1)
        .fold(CommonTestResources().resourcesMap) { map, segment ->
            @Suppress("UNCHECKED_CAST")
            map[segment] as Map<String, Any>
        }[segments.last()] as ByteString).utf8()
}
