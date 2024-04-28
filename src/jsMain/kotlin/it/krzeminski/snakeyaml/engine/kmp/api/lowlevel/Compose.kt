package it.krzeminski.snakeyaml.engine.kmp.api.lowlevel

import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node

/**
 * Helper to compose input stream to Node
 * @param settings - configuration
 */
actual class Compose(settings: LoadSettings) {

    private val composeString = ComposeString(settings)

    /**
     * Parse a YAML stream and produce [Node]
     *
     * See [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107).
     *
     * @param yaml - YAML document(s).
     * @return parsed [Node] if available
     */
    actual fun composeString(yaml: String): Node? = composeString.composeString(yaml)

    /**
     * Parse all YAML documents in a stream and produce corresponding representation trees.
     *
     * See [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107).
     *
     * @param yaml - YAML document(s).
     * @return parsed root Nodes for all the specified YAML documents
     */
    actual fun composeAllFromString(yaml: String): Iterable<Node> =
        composeString.composeAllFromString(yaml)
}
