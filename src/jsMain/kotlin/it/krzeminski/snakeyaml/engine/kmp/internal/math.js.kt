package it.krzeminski.snakeyaml.engine.kmp.internal

internal actual fun isInteger(value: Number): Boolean =
    value is Long || isIntegerJs(value)

private fun isIntegerJs(value: Number): Boolean =
    js("return Number.isInteger(value)")
