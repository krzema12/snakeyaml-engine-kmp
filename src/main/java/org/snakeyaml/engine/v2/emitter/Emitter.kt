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
package org.snakeyaml.engine.v2.emitter;

import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.api.StreamDataWriter
import org.snakeyaml.engine.v2.events.Event


/**
 * <pre>
 * Emitter expects events obeying the following grammar:
 * stream ::= STREAM-START document* STREAM-END
 * document ::= DOCUMENT-START node DOCUMENT-END
 * node ::= SCALAR | sequence | mapping
 * sequence ::= SEQUENCE-START node* SEQUENCE-END
 * mapping ::= MAPPING-START (node node)* MAPPING-END
 * </pre>
 */
class Emitter(
    private val opts: DumpSettings,
    private val stream: StreamDataWriter,
) : Emitable {
    private val emitterJava = EmitterJava(opts, stream)
    override fun emit(event: Event): Unit = emitterJava.emit(event)

    companion object {
        private val ESCAPE_REPLACEMENTS: Map<Char, String> = mapOf(
            '\u0000' to "0",
            '\u0007' to "a",
            '\u0008' to "b",
            '\u0009' to "t",
            '\n' to "n",
            '\u000B' to "v",
            '\u000C' to "f",
            '\r' to "r",
            '\u001B' to "e",
            '"' to "\"",
            '\\' to "\\",
            '\u0085' to "N",
            '\u00A0' to "_",
            '\u2028' to "L",
            '\u2029' to "P",
        )

        /**
         * indent cannot be zero spaces
         */
        const val MIN_INDENT = 1

        /**
         * indent should not be more than 10 spaces
         */
        const val MAX_INDENT = 10

        private const val SPACE = " "
    }
}
