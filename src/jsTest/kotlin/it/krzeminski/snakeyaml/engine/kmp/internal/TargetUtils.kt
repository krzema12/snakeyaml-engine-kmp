package it.krzeminski.snakeyaml.engine.kmp.internal

// returns 'true' is we are in the browser
actual fun areEnvVarsSupported(): Boolean =
    js("return (typeof window === 'undefined' || typeof document === 'undefined')") as Boolean
