package it.krzeminski.snakeyaml.engine.kmp.internal

/**
 * Not all targets support environment variables. Thanks to this function being implemented appropriately by each
 * target, we can e.g. skip tests that rely on the environment variables.
 */
expect fun areEnvVarsSupported(): Boolean
