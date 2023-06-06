package org.snakeyaml.engine.internal

import kotlin.jvm.JvmInline

/**
 * Fetch an environment variable from the current system.
 */
internal expect fun getEnvironmentVariable(key: String): String?

@JvmInline
internal value class IdentityHashCode(private val value: Int)

internal expect fun identityHashCode(any: Any?): IdentityHashCode
