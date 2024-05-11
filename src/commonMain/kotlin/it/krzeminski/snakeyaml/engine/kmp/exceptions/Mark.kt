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

import it.krzeminski.snakeyaml.engine.kmp.common.CharConstants
import it.krzeminski.snakeyaml.engine.kmp.internal.utils.appendCodePoint
import it.krzeminski.snakeyaml.engine.kmp.internal.utils.toCodePoints
import kotlin.jvm.JvmOverloads

/**
 * Location of a problem in the YAML document. Its only use is producing nice error messages. Parser
 * does not use it for any other purposes.
 *
 * @param name the name of the data stream, to be used as identifier
 * @param index the index from the beginning of the stream
 * @param line line of the mark from beginning of the stream
 * @param column column of the mark from beginning of the line
 * @param codepoints the data
 * @param pointer the position of the mark from the beginning of the data
 */
class Mark @JvmOverloads constructor(
    val name: String,
    val index: Int,
    val line: Int,
    val column: Int,
    val codepoints: List<Int>,
    @Deprecated("pointer is no longer required")
    val pointer: Int = 0,
) {

    @Deprecated("Converted to immutable List<Int>")
    val buffer: IntArray = codepoints.toIntArray()

    /**
     * This constructor is only for test
     *
     * @param name the name to be used as identifier
     * @param index the index from the beginning of the stream
     * @param line line of the mark from beginning of the stream
     * @param column column of the mark from beginning of the line
     * @param str the data
     * @param pointer the position of the mark from the beginning of the data
     */
    @JvmOverloads
    internal constructor(
        name: String,
        index: Int,
        line: Int,
        column: Int,
        str: CharSequence,
        pointer: Int = 0,
    ) : this(
        name = name,
        index = index,
        line = line,
        column = column,
        codepoints = str.toCodePoints(),
        pointer = pointer,
    )

    @JvmOverloads
    @Deprecated("...")
    internal constructor(
        name: String,
        index: Int,
        line: Int,
        column: Int,
        buffer: IntArray,
        pointer: Int = 0,
    ) : this(
        name = name,
        index = index,
        line = line,
        column = column,
        codepoints = buffer.toList(),
        pointer = pointer,
    )

    private fun isLineBreak(c: Int): Boolean = CharConstants.NULL_OR_LINEBR.has(c)

    /**
     * Create readable YAML with indent 4 and (by default) a [maxLength] of 75 characters.
     *
     * @param indent the indent
     * @param maxLength cut data after this length
     * @return readable piece of YAML where a problem detected
     */
    @Suppress("DEPRECATION")
    @JvmOverloads
    // TODO this function is only exposed because of testing - mark as `internal` once tests are Kotlin
    fun createSnippet(
        indent: Int = 4,
        maxLength: Int = 75,
    ): String {
        val half = maxLength / 2f - 1f
        var start = pointer
        var head = ""
        while (start > 0 && !isLineBreak(buffer[start - 1])) {
            start -= 1
            if (pointer - start > half) {
                head = " ... "
                start += 5
                break
            }
        }
        var tail = ""
        var end = pointer
        while (end < buffer.size && !isLineBreak(buffer[end])) {
            end += 1
            if (end - pointer > half) {
                tail = " ... "
                end -= 5
                break
            }
        }
        val result = StringBuilder()
        for (i in 0 until indent) {
            result.append(" ")
        }
        result.append(head)
        for (i in start until end) {
            result.appendCodePoint(buffer[i])
        }
        result.append(tail)
        result.append("\n")
        for (i in 0 until indent + pointer - start + head.length) {
            result.append(" ")
        }
        result.append("^")
        return result.toString()
    }

    override fun toString(): String {
        val snippet = createSnippet()
        return """
            | in $name, line ${line + 1}, column ${column + 1}:
            |$snippet
        """.trimMargin()
    }
}
