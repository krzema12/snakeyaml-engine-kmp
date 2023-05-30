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
package org.snakeyaml.engine.v2.constructor.json

import org.snakeyaml.engine.v2.constructor.ConstructScalar
import org.snakeyaml.engine.v2.nodes.Node

/**
 * Create Double instances for float
 */
open class ConstructYamlJsonFloat : ConstructScalar() {
    override fun construct(node: Node?): Double {
        return when (val value = constructScalar(node)) {
            ".inf"  -> Double.POSITIVE_INFINITY
            "-.inf" -> Double.NEGATIVE_INFINITY
            ".nan"  -> Double.NaN
            else    -> constructFromString(value)
        }
    }

    private fun constructFromString(value: String): Double {
        val (sign, number) = when (value.firstOrNull()) {
            '-'  -> -1 to value.substring(1)
            '+'  -> +1 to value.substring(1)
            else -> +1 to value
        }
        val d = number.toDouble()
        return d * sign
    }
}
