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
package it.krzeminski.snakeyaml.engine.kmp.constructor.json

import it.krzeminski.snakeyaml.engine.kmp.internal.toBigInteger
import it.krzeminski.snakeyaml.engine.kmp.constructor.ConstructScalar
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node

/**
 * Create instances for numbers (Integer, Long, BigInteger)
 */
class ConstructYamlJsonInt : ConstructScalar() {
    override fun construct(node: Node?): Number {
        val value = constructScalar(node)
        return createIntNumber(value)
    }

    /**
     * Create number trying fist Integer, then Long, then BigInteger
     *
     * @param number - the source
     * @return number that fits the source
     */
    private fun createIntNumber(number: String): Number {
        // first try integer, then Long, and BigInteger as the last resource
        return number.toIntOrNull()
            ?: number.toLongOrNull()
            ?: number.toBigInteger()
    }
}
