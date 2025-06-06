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
package it.krzeminski.snakeyaml.engine.kmp.serializer

import it.krzeminski.snakeyaml.engine.kmp.common.Anchor
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node

/**
 * Simple generate of the format id + number
 *
 * @param lastAnchorId - the number to start from
 */
class NumberAnchorGenerator(
    private var lastAnchorId: UInt = 0u,
) : AnchorGenerator {
    /**
     * Create the anchor name (increasing the number) or keep the one when it was already created in
     * the node by the low level API
     *
     * @param node - the data to anchor
     * @return unique anchor name or existing anchor name
     */
    override fun nextAnchor(node: Node): Anchor {
        node.anchor?.let { return it }

        lastAnchorId++
        val anchorId = lastAnchorId.toString().padStart(3, '0')
        return Anchor("id$anchorId")
    }
}
