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

import okio.source
import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.api.YamlUnicodeReader
import org.snakeyaml.engine.v2.composer.Composer
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.parser.ParserImpl
import org.snakeyaml.engine.v2.scanner.StreamReader
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
     * @param yaml - YAML document(s). Since the encoding is already known the BOM must not be present
     * (it will be parsed as content)
     * @return parsed [Node] if available
     * @see [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107)
     */
    fun composeReader(yaml: Reader): Node? =
        Composer(
            settings,
            ParserImpl(settings, StreamReader(settings, yaml.readText())),
        ).singleNode

    /**
     * Parse a YAML stream and produce [Node]
     *
     * @param yaml - YAML document(s). Default encoding is UTF-8. The BOM must be present if the
     * encoding is UTF-16 or UTF-32
     * @return parsed [Node] if available
     * @see [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107)
     */
    fun composeInputStream(yaml: InputStream): Node? =
        Composer(
            settings,
            ParserImpl(settings, StreamReader(settings, YamlUnicodeReader(yaml.source()))),
        ).singleNode

    /**
     * Parse a YAML stream and produce [Node]
     *
     * @param yaml - YAML document(s).
     * @return parsed [Node] if available
     * @see [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107)
     */
    actual fun composeString(yaml: String): Node? = composeString.composeString(yaml)

    // Compose all documents
    /**
     * Parse all YAML documents in a stream and produce corresponding representation trees.
     *
     * @param yaml stream of YAML documents
     * @return parsed root Nodes for all the specified YAML documents
     * @see [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107)
     */
    fun composeAllFromReader(yaml: Reader): Iterable<Node> =
        Composer(
            settings,
            ParserImpl(settings, StreamReader(settings, yaml.readText())),
        ).asSequence().asIterable()

    /**
     * Parse all YAML documents in a stream and produce corresponding representation trees.
     *
     * @param yaml - YAML document(s). Default encoding is UTF-8. The BOM must be present if the
     * encoding is UTF-16 or UTF-32
     * @return parsed root Nodes for all the specified YAML documents
     * @see [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107)
     */
    fun composeAllFromInputStream(yaml: InputStream): Iterable<Node> =
        Composer(
            settings,
            ParserImpl(settings, StreamReader(settings, YamlUnicodeReader(yaml.source()))),
        ).asSequence().asIterable()

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
