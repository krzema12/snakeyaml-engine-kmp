package it.krzeminski.snakeyaml.engine.kmp.internal

/**
 * Returns `true` if we are not in the browser.
 */
actual fun areEnvVarsSupported(): Boolean =
    js("return (typeof window === 'undefined' || typeof document === 'undefined')") as Boolean
