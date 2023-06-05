package org.snakeyaml.engine.v2.api.lowlevel

import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.nodes.Node

/**
 * Helper to compose input stream to Node
 * @param settings - configuration
 */
actual class Compose(settings: LoadSettings) {

    private val composeString = ComposeString(settings)

    /**
     * Parse a YAML stream and produce [Node]
     *
     * @param yaml - YAML document(s).
     * @return parsed [Node] if available
     * @see [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107)
     */
    actual fun composeString(yaml: String): Node? = composeString.composeString(yaml)

    /**
     * Parse all YAML documents in a stream and produce corresponding representation trees.
     *
     * @param yaml - YAML document(s).
     * @return parsed root Nodes for all the specified YAML documents
     * @see [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107)
     */
    actual fun composeAllFromString(yaml: String): Iterable<Node> =
        composeString.composeAllFromString(yaml)
}
