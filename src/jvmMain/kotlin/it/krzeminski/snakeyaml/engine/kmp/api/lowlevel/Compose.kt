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
import okio.source
import java.io.InputStream
import java.io.Reader

actual class Compose actual constructor(
    settings: LoadSettings,
) {
    private val common = ComposeCommon(settings)

    actual fun compose(source: Source): Node? = common.compose(source)

    actual fun compose(string: String): Node? = common.compose(string)

    actual fun composeAll(source: Source): Iterable<Node> = common.composeAll(source)

    actual fun composeAll(string: String): Iterable<Node> = common.composeAll(string)

    /**
     * Parse a YAML stream and produce a single [Node], if available in [reader].
     *
     * @param inputStream YAML document(s).
     */
    fun compose(inputStream: InputStream): Node? = compose(inputStream.source())

    @Deprecated("renamed", ReplaceWith("compose(yaml)"))
    fun composeInputStream(yaml: InputStream): Node? = compose(yaml)


    /**
     * Parse a YAML stream and produce a single [Node], if available in [reader].
     *
     * @param reader YAML document(s).
     */
    fun compose(reader: Reader): Node? = compose(reader.readText())

    @Deprecated("renamed", ReplaceWith("compose(yaml)"))
    fun composeReader(yaml: Reader): Node? = compose(yaml)

    /**
     * Parse all YAML documents in a stream and produce corresponding representation trees.
     *
     * See [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107).
     *
     * @param inputStream stream of YAML documents
     * @return parsed root [Node]s for all YAML documents in [inputStream].
     */
    fun composeAll(inputStream: InputStream): Iterable<Node> = composeAll(inputStream.source())

    @Deprecated("renamed", ReplaceWith("composeAll(yaml)"))
    fun composeAllFromInputStream(yaml: InputStream): Iterable<Node> = composeAll(yaml)

    /**
     * Parse all YAML documents in a stream and produce corresponding representation trees.
     *
     * See [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107).
     *
     * @param reader stream of YAML documents
     * @return parsed root [Node]s for all YAML documents in [reader].
     */
    fun composeAll(reader: Reader): Iterable<Node> = composeAll(reader.readText())

    @Deprecated("renamed", ReplaceWith("composeAll(yaml)"))
    fun composeAllFromReader(yaml: Reader): Iterable<Node> = composeAll(yaml)

    @Deprecated("renamed", ReplaceWith("compose(yaml)"))
    actual fun composeString(yaml: String): Node? = compose(yaml)

    @Deprecated("renamed", ReplaceWith("compose(yaml)"))
    actual fun composeAllFromString(yaml: String): Iterable<Node> = composeAll(yaml)
}
