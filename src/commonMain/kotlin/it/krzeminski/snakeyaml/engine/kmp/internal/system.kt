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

/**
 * Returns the [IdentityHashCode] for an object. For `null` always returns `IdentityHashCode(0)`
 * @throws IllegalArgumentException if [any] is a primitive type or a [String]
 */
internal fun identityHashCode(any: Any?): IdentityHashCode =
    when {
        any == null              -> IdentityHashCode(0)
        hasIdentityHashCode(any) -> objectIdentityHashCode(any)
        else                     ->
            throw IllegalArgumentException("identity hash code cannot be computed for primitives and strings (type: ${any::class.simpleName})")
    }

internal expect fun objectIdentityHashCode(any: Any): IdentityHashCode

/**
 * Function returns `true` if the object can be used to compute the [IdentityHashCode] on all platforms.
 * The following types do not have identity:
 * + primitive numbers
 * + boolean
 * + character
 * + string (despite the fact that it is an object on most of the platforms but JS does not allow to apply identity hash code logic to it)
 */
internal fun hasIdentityHashCode(any: Any?): Boolean =
    when (any) {
        is Byte,
        is Short,
        is Int,
        is Long,
        is Float,
        is Double,
        is Boolean,
        is Char,
        is String -> false

        else      -> true
    }
