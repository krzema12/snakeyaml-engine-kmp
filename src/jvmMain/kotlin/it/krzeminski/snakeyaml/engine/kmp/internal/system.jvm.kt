package it.krzeminski.snakeyaml.engine.kmp.internal

/**
 * Fetch an environment variable using [System.getenv]
 */
internal actual fun getEnvironmentVariable(key: String): String? = System.getenv(key)

internal actual fun objectIdentityHashCode(any: Any): IdentityHashCode {
    val hc = System.identityHashCode(any)
    return IdentityHashCode(hc)
}
