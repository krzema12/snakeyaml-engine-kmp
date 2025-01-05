package it.krzeminski.snakeyaml.engine.kmp.internal

internal actual fun getEnvironmentVariable(key: String): String? {
    return process.env[key] as String?
}

