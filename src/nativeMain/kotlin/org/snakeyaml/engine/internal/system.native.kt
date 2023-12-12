package org.snakeyaml.engine.internal

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.getenv
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.identityHashCode

@OptIn(ExperimentalForeignApi::class)
internal actual fun getEnvironmentVariable(key: String): String? =
    getenv(key)?.toKStringFromUtf8()

@OptIn(ExperimentalNativeApi::class)
internal actual fun identityHashCode(any: Any?): IdentityHashCode =
    IdentityHashCode(any.identityHashCode())
