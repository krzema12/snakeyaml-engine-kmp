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
package it.krzeminski.snakeyaml.engine.kmp.exceptions

import it.krzeminski.snakeyaml.engine.kmp.internal.utils.Character

/**
 * Indicate invalid input stream
 *
 * @param[name] the name of the reader
 * @param[position] the position from the beginning of the stream
 * @param[codePoint] the invalid character
 * @param[message] the problem
 */
class ReaderException(
    val name: String,
    val position: Int,
    val codePoint: Int,
    message: String,
) : YamlEngineException(message) {

    override fun toString(): String {
        val s = Character.toChars(codePoint).concatToString()
        val charHex = codePoint.toString(16).uppercase()
        return """
             unacceptable code point '$s' (0x$charHex) $message
             in "$name", position $position
             """.trimIndent()
    }
}
