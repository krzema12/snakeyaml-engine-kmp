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

import it.krzeminski.snakeyaml.engine.kmp.constructor.ConstructScalar
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ConstructorException
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node
import it.krzeminski.snakeyaml.engine.kmp.nodes.NodeType
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag
import it.krzeminski.snakeyaml.engine.kmp.resolver.ScalarResolver
import java.util.Optional

/**
 * Create instances of [Optional]
 */
class ConstructOptionalClass(private val scalarResolver: ScalarResolver) : ConstructScalar() {
    override fun construct(node: Node?): Optional<Any> {
        if (node?.nodeType != NodeType.SCALAR) {
            throw ConstructorException(
                context = "while constructing Optional",
                contextMark = null,
                problem = "found non scalar node",
                problemMark = node!!.startMark,
            )
        }
        val value = constructScalar(node)
        val implicitTag = scalarResolver.resolve(value, true)
        return if (implicitTag == Tag.NULL) Optional.empty() else Optional.of(value)
    }
}
