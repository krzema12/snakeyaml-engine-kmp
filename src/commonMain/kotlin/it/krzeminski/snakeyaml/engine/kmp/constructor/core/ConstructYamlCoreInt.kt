/*
 * Copyright (c) 2018, SnakeYAML
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package it.krzeminski.snakeyaml.engine.kmp.constructor.core

import it.krzeminski.snakeyaml.engine.kmp.internal.createBigInteger
import it.krzeminski.snakeyaml.engine.kmp.internal.toBigInteger
import it.krzeminski.snakeyaml.engine.kmp.constructor.ConstructScalar
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ConstructorException
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node

/**
 * Create instances for numbers (Integer, Long, BigInteger)
 */
class ConstructYamlCoreInt : ConstructScalar() {
    override fun construct(node: Node?): Number {
        val value = constructScalar(node)
        if (value.isEmpty()) {
            throw ConstructorException(
                context = "while constructing an int",
                contextMark = node!!.startMark,
                problem = "found empty value",
                problemMark = node.startMark,
            )
        }
        return createIntNumber(value)
    }

    private fun createIntNumber(value: String): Number {
        val (sign: Int, numeral: String) = when (value.firstOrNull()) {
            '-'  -> -1 to value.substring(1)
            '+'  -> +1 to value.substring(1)
            else -> +1 to value
        }

        val (base: Int, number: String) = when {
            numeral == "0"           -> return 0
            numeral.startsWith("0x") -> 16 to numeral.substring(2)
            numeral.startsWith("0o") -> 8 to numeral.substring(2)
            else                     -> 10 to numeral
        }

        return createNumber(sign, number, base)
    }

    private fun createNumber(sign: Int, numeric: String, radix: Int): Number {
        val len = numeric.length
        val number = if (sign < 0) "-$numeric" else numeric
        val maxArr = if (radix < RADIX_MAX.size) RADIX_MAX[radix] else null
        if (maxArr != null) {
            val gtInt = len > maxArr.first()
            if (gtInt) {
                return if (len > maxArr[1]) {
                    createBigInteger(number, radix)
                } else {
                    number.toLongOrBigInteger(radix)
                }
            }
        }
        return number.toIntOrNull(radix) ?: number.toLongOrBigInteger(radix)
    }

    companion object {
        private val RADIX_MAX = Array(17) { IntArray(2) }

        init {
            val radixList = intArrayOf(8, 10, 16)
            for (radix in radixList) {
                RADIX_MAX[radix] = intArrayOf(
                    Int.MAX_VALUE.toString(radix).length,
                    Long.MAX_VALUE.toString(radix).length,
                )
            }
        }

        private fun String.toLongOrBigInteger(radix: Int): Number =
            toLongOrNull(radix) ?: toBigInteger(radix)
    }
}
