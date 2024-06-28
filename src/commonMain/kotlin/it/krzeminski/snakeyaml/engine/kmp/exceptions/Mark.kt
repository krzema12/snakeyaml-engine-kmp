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
import it.krzeminski.snakeyaml.engine.kmp.internal.utils.joinCodepointsToString
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
 * @param pointer the index of the character in [codepoints] that will be marked
 */
class Mark @JvmOverloads constructor(
    val name: String,
    val index: Int,
    val line: Int,
    val column: Int,
    val codepoints: List<Int>,
    val pointer: Int = 0,
) {

    @Deprecated("Converted to immutable List<Int>, replace with `codepoints`")
    val buffer: IntArray by lazy { codepoints.toIntArray() }

    /**
     * Deprecated: please convert [str] to codepoints.
     *
     * ```java
     * // java
     * List<Integer> codepoints = str.codePoints().boxed().collect(Collectors.toList());
     * ```
     *
     * @param name the name to be used as identifier
     * @param index the index from the beginning of the stream
     * @param line line of the mark from beginning of the stream
     * @param column column of the mark from beginning of the line
     * @param str the data
     * @param pointer the position of the mark from the beginning of the data
     */
    @JvmOverloads
    @Deprecated("No longer used - please convert CharSequence to codepoints")
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
    @Deprecated("No longer used - please use a List<Int> instead of IntArray")
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
     * Create readable YAML snippet of [codepoints], with a caret `^` pointing at [pointer].
     *
     * Only a single line will be rendered. Content before or after a linebreak will not be shown.
     *
     * Lines longer than [maxLength] will be truncated, using [SNIPPET_OVERFLOW] to indicate truncation.
     *
     * @param indentSize The number of spaces to indent the snippet.
     * @param maxLength Limit the result to this many characters.
     * @return readable Piece of YAML that highlights a .
     */
    @JvmOverloads
    // TODO this function is only exposed because of testing - mark as `internal` once tests are Kotlin
    fun createSnippet(
        indentSize: Int = 4,
        maxLength: Int = 75,
    ): String {
        val halfMaxLength = maxLength / 2

        val lineBeforePointer = codepoints
            .take(pointer)
            .takeLastWhile { !isLineBreak(it) }
            .joinCodepointsToString()

        val lineAfterPointer = codepoints
            .drop(pointer)
            .takeWhile { !isLineBreak(it) }
            .joinCodepointsToString()

        val head = if (lineBeforePointer.length > halfMaxLength) {
            SNIPPET_OVERFLOW + lineBeforePointer.takeLast(halfMaxLength).drop(SNIPPET_OVERFLOW.length)
        } else {
            lineBeforePointer
        }

        val tail = if (lineAfterPointer.length > halfMaxLength) {
            lineAfterPointer.take(halfMaxLength).dropLast(SNIPPET_OVERFLOW.length) + SNIPPET_OVERFLOW
        } else {
            lineAfterPointer
        }

        val indent = " ".repeat(indentSize)
        return buildString {
            append(indent)
            append(head)
            append(tail)
            appendLine()

            append(indent)
            append(" ".repeat(head.length))
            append("^")
        }
    }

    override fun toString(): String {
        val snippet = createSnippet()
        return """
            | in ${name.trim()}, line ${line + 1}, column ${column + 1}:
            |$snippet
        """.trimMargin()
    }

    companion object {
        private const val SNIPPET_OVERFLOW = " ... "
    }
}
