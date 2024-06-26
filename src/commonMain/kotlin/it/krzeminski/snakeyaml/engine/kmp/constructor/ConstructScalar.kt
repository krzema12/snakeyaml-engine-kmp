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
package it.krzeminski.snakeyaml.engine.kmp.constructor

import it.krzeminski.snakeyaml.engine.kmp.api.ConstructNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node
import it.krzeminski.snakeyaml.engine.kmp.nodes.ScalarNode
import kotlin.jvm.JvmField

/**
 * Share common code for scalar constructs
 */
abstract class ConstructScalar : ConstructNode {
    /**
     * Create String from the provided scalar node
     *
     * @param node - the source
     * @return value of the scalar node
     */
    protected open fun constructScalar(node: Node?): String = (node as ScalarNode).value

    companion object {
        @JvmField
        internal val BOOL_VALUES: Map<String, Boolean> = mapOf(
            "true" to true,
            "false" to false,
        )
    }
}
