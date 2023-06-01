package org.snakeyaml.engine.internal

/**
 * Fetch an environment variable using [System.getenv]
 */
internal actual fun getEnvironmentVariable(key: String): String? = System.getenv(key)
