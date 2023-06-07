package org.snakeyaml.engine.internal

import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.getenv
import kotlin.native.identityHashCode

internal actual fun getEnvironmentVariable(key: String): String? =
    getenv(key)?.toKStringFromUtf8()

internal actual fun identityHashCode(any: Any?): IdentityHashCode =
    IdentityHashCode(any.identityHashCode())
