package it.krzeminski.snakeyaml.engine.kmp.internal

internal actual fun isInteger(value: Number): Boolean =
    js("return Number.isInteger(value)")
