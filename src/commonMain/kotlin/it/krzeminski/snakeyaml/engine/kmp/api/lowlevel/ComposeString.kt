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
import it.krzeminski.snakeyaml.engine.kmp.composer.Composer
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node
import it.krzeminski.snakeyaml.engine.kmp.parser.ParserImpl
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader

/**
 * Helper to compose strings to [Node]s.
 *
 * @param settings configuration
 */
@Deprecated("Converted to common it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose", ReplaceWith("it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose"))
class ComposeString(
    private val settings: LoadSettings,
) {

    /**
     * Parse a YAML stream and produce [Node]
     *
     * See [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107).
     *
     * @param string YAML document(s).
     * @return parsed [Node], if available in [string].
     */
    fun compose(string: String): Node? = composer(string).getSingleNode()

    @Deprecated("renamed", ReplaceWith("compose(yaml)"))
    fun composeString(yaml: String): Node? = compose(yaml)

    /**
     * Parse all YAML documents in a stream and produce corresponding representation trees.
     *
     * See [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107).
     *
     * @param string YAML document(s).
     * @return parsed root [Node]s for all YAML documents in [string].
     */
    fun composeAll(string: String): Iterable<Node> = Iterable { composer(string) }

    @Deprecated("renamed", ReplaceWith("compose(yaml)"))
    fun composeAllFromString(yaml: String): Iterable<Node> = composeAll(yaml)

    private fun composer(yaml: String): Composer {
        val reader = StreamReader(stream = yaml, loadSettings = settings)
        val parser = ParserImpl(settings, reader)
        return Composer(settings, parser)
    }
}
