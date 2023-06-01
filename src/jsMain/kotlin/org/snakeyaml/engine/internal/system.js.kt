package org.snakeyaml.engine.internal

internal actual fun getEnvironmentVariable(key: String): String? {
    // TODO how can environment variables be implemented in Kotlin/JS?
    return null
}
