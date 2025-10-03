package it.krzeminski.snakeyaml.engine.kmp.internal

internal actual fun isInteger(value: Number): Boolean =
    value is Byte || value is Short || value is Int || value is Long
