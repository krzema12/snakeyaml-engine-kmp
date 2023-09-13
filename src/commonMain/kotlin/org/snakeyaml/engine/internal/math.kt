package org.snakeyaml.engine.internal

internal expect fun createBigInteger(value: String, radix: Int = 10): Number

internal fun String.toBigInteger(radix: Int = 10): Number = createBigInteger(this, radix)

internal expect fun createBigDecimal(value: String): Number
