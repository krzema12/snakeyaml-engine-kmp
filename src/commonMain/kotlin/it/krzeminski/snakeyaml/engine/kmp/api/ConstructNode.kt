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
package it.krzeminski.snakeyaml.engine.kmp.api

import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node

/**
 * Provide a way to construct an instance from the composed [Node]. Support recursive objects if
 * it is required. (create Native Data Structure out of Node Graph) (this is the opposite for
 * Represent)
 *
 * @see [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107)
 */
interface ConstructNode {
    /**
     * Construct an instance, with all the properties injected when it is possible.
     *
     * @param node composed [Node]
     * @return a complete Java instance, or an empty collection instance if it is recursive
     */
    fun construct(node: Node?): Any?

    /**
     * Apply the second step when constructing recursive structures. Because the instance is already
     * created it can assign a reference to itself. (no need to implement this method for
     * non-recursive data structures). Fails with a reminder to provide the second step for a recursive
     * structure
     *
     * @param node composed [Node]
     * @param object the instance constructed earlier by [construct] for the provided [Node]
     */
    fun constructRecursive(node: Node, `object`: Any) {
        check(!node.isRecursive) { "Not implemented in ${this::class}" }
        throw YamlEngineException("Unexpected recursive structure for Node: $node")
    }
}
