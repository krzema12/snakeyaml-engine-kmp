package it.krzeminski.snakeyaml.engine.kmp.internal


internal actual fun createBigInteger(value: String, radix: Int): Number {
    TODO("Kotlin/JS BigInteger implementation")
}

internal actual fun createBigDecimal(value: String): Number {
    TODO("Kotlin/Native BigDecimal implementation")
}

internal actual fun isInteger(value: Number): Boolean =
    value is Byte || value is Short || value is Int || value is Long
