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
package it.krzeminski.snakeyaml.engine.kmp.api.lowlevel

import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.YamlUnicodeReader
import it.krzeminski.snakeyaml.engine.kmp.composer.Composer
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node
import it.krzeminski.snakeyaml.engine.kmp.parser.ParserImpl
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader
import okio.source
import java.io.InputStream
import java.io.Reader

/**
 * Helper to compose input stream to Node
 * @param settings - configuration
 */
actual class Compose(
    private val settings: LoadSettings,
) {

    private val composeString = ComposeString(settings)

    /**
     * Parse a YAML stream and produce [Node]
     *
     * See [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107).
     *
     * @param yaml - YAML document(s). Since the encoding is already known the BOM must not be present
     * (it will be parsed as content)
     * @return parsed [Node] if available
     */
    fun composeReader(yaml: Reader): Node? =
        Composer(
            settings,
            ParserImpl(settings, StreamReader(settings, yaml.readText())),
        ).getSingleNode()

    /**
     * Parse a YAML stream and produce [Node]
     *
     * See [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107).
     *
     * @param yaml - YAML document(s). Default encoding is UTF-8. The BOM must be present if the
     * encoding is UTF-16 or UTF-32
     * @return parsed [Node] if available
     */
    fun composeInputStream(yaml: InputStream): Node? =
        Composer(
            settings,
            ParserImpl(settings, StreamReader(settings, YamlUnicodeReader(yaml.source()))),
        ).getSingleNode()

    /**
     * Parse a YAML stream and produce [Node]
     *
     * See [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107).
     *
     * @param yaml - YAML document(s).
     * @return parsed [Node] if available
     */
    actual fun composeString(yaml: String): Node? = composeString.composeString(yaml)

    // Compose all documents
    /**
     * Parse all YAML documents in a stream and produce corresponding representation trees.
     *
     * See [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107).
     *
     * @param yaml stream of YAML documents
     * @return parsed root Nodes for all the specified YAML documents
     */
    fun composeAllFromReader(yaml: Reader): Iterable<Node> =
        Iterable {
            Composer(
                settings,
                ParserImpl(settings, StreamReader(settings, yaml.readText())),
            )
        }

    /**
     * Parse all YAML documents in a stream and produce corresponding representation trees.
     *
     * See [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107).
     *
     * @param yaml - YAML document(s). Default encoding is UTF-8. The BOM must be present if the
     * encoding is UTF-16 or UTF-32
     * @return parsed root Nodes for all the specified YAML documents
     */
    fun composeAllFromInputStream(yaml: InputStream): Iterable<Node> =
        Iterable {
            Composer(
                settings,
                ParserImpl(settings, StreamReader(settings, YamlUnicodeReader(yaml.source()))),
            )
        }

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
