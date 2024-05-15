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
import okio.Buffer
import okio.Source

/**
 * Kotlin Common implementations of [Compose] functions.
 */
internal class ComposeCommon(
    private val settings: LoadSettings,
) {
    /** @see Compose.compose */
    fun compose(source: Source): Node? = composer(source).getSingleNode()

    /** @see Compose.compose */
    fun compose(string: String): Node? = compose(Buffer().writeUtf8(string))

    /** @see Compose.composeAll */
    fun composeAll(source: Source): Iterable<Node> = Iterable { composer(source) }

    /** @see Compose.composeAll */
    fun composeAll(string: String): Iterable<Node> = composeAll(Buffer().writeUtf8(string))

    private fun composer(source: Source): Composer {
        val reader = StreamReader(stream = source, loadSettings = settings)
        val parser = ParserImpl(settings = settings, reader = reader)
        return Composer(settings = settings, parser = parser)
    }
}
