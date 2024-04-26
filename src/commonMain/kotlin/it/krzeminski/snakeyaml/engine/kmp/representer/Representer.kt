package it.krzeminski.snakeyaml.engine.kmp.representer

import it.krzeminski.snakeyaml.engine.kmp.nodes.Node

/**
 * Represent some data as a YAML [Node].
 */
interface Representer {
    fun represent(data: Any?): Node
}
