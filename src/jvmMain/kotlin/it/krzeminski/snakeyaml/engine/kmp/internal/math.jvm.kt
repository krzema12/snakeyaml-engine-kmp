package it.krzeminski.snakeyaml.engine.kmp.internal

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext


internal actual fun createBigInteger(value: String, radix: Int): Number = BigInteger(value, radix)

internal actual fun createBigDecimal(value: String): Number = BigDecimal(value, MathContext.UNLIMITED)
