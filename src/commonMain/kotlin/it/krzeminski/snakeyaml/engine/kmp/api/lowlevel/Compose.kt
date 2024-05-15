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
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node
import okio.Source

/**
 * Compose input sources to [Node]s.
 *
 * See [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107).
 */
expect class Compose(settings: LoadSettings) {
    /**
     * Parse a YAML stream and produce a single [Node], if available in [source].
     *
     * @param source YAML document(s).
     */
    fun compose(source: Source): Node?

    /**
     * Parse a YAML stream and produce [Node]
     *
     * @param string YAML document(s).
     * @return parsed [Node], if available in [string].
     */
    fun compose(string: String): Node?

    /**
     * Parse all YAML documents in a stream and produce corresponding representation trees.
     *
     * @param source YAML document(s).
     * @return parsed root [Node]s for all YAML documents in [source].
     */
    fun composeAll(source: Source): Iterable<Node>

    /**
     * Parse all YAML documents in a stream and produce corresponding representation trees.
     *
     * @param string YAML document(s).
     * @return parsed root [Node]s for all YAML documents in [string].
     */
    fun composeAll(string: String): Iterable<Node>

    @Deprecated("renamed", ReplaceWith("compose(yaml)"))
    fun composeString(yaml: String): Node?

    @Deprecated("renamed", ReplaceWith("compose(yaml)"))
    fun composeAllFromString(yaml: String): Iterable<Node>
}
