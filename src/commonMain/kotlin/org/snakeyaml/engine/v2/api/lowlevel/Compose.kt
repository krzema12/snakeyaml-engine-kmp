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
package org.snakeyaml.engine.v2.api.lowlevel

import org.snakeyaml.engine.v2.nodes.Node

/**
 * Helper to compose input stream to Node
 */
expect class Compose {

    /**
     * Parse a YAML stream and produce [Node]
     *
     * See [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107).
     *
     * @param yaml - YAML document(s).
     * @return parsed [Node] if available
     */
    fun composeString(yaml: String): Node?


    /**
     * Parse all YAML documents in a stream and produce corresponding representation trees.
     *
     * See [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107).
     *
     * @param yaml - YAML document(s).
     * @return parsed root Nodes for all the specified YAML documents
     */
    fun composeAllFromString(yaml: String): Iterable<Node>
}
