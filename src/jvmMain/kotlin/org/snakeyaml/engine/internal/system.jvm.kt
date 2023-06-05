package org.snakeyaml.engine.internal

/**
 * Fetch an environment variable using [System.getenv]
 */
internal actual fun getEnvironmentVariable(key: String): String? = System.getenv(key)

internal actual fun identityHashCode(any: Any?): IdentityHashCode {
    val hc = System.identityHashCode(any)
    return IdentityHashCode(hc)
}
