package org.snakeyaml.engine.v2.representer

import org.snakeyaml.engine.v2.nodes.Node

/**
 * Represent some data as a YAML [Node].
 */
interface Representer {
    fun represent(data: Any?): Node
}
