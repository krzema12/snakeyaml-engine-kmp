package org.snakeyaml.engine.internal

/**
 * Fetch an environment variable from the current system.
 */
internal expect fun getEnvironmentVariable(key: String): String?
