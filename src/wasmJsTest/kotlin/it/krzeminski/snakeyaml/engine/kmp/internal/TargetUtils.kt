package it.krzeminski.snakeyaml.engine.kmp.internal

/**
 * Returns `true` if we are not in the browser.
 */
actual fun areEnvVarsSupported(): Boolean =
    js("(typeof window === 'undefined' || typeof document === 'undefined')")
