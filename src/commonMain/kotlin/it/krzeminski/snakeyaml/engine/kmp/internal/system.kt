package it.krzeminski.snakeyaml.engine.kmp.internal

import kotlin.jvm.JvmInline

/**
 * Fetch an environment variable from the current system.
 *
 * @param key - the name of the variable
 * @returns the value, or `null` if the environment variable is not present or has no value
 */
internal expect fun getEnvironmentVariable(key: String): String?

@JvmInline
internal value class IdentityHashCode(private val value: Int)

internal expect fun identityHashCode(any: Any?): IdentityHashCode
