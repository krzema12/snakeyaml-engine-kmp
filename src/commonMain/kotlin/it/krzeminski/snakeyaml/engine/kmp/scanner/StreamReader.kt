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
package it.krzeminski.snakeyaml.engine.kmp.scanner

import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.common.CharConstants
import it.krzeminski.snakeyaml.engine.kmp.exceptions.Mark
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ReaderException
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException
import it.krzeminski.snakeyaml.engine.kmp.internal.utils.Character
import it.krzeminski.snakeyaml.engine.kmp.internal.utils.appendCodePoint
import it.krzeminski.snakeyaml.engine.kmp.internal.utils.codePointAt
import okio.*
import okio.ByteString.Companion.encodeUtf8
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

/**
 * Read the provided stream of code points into String and implement look-ahead operations. Checks
 * if code points are in the allowed range.
 *
 * @param stream the input
 * @param loadSettings configuration options
 */
class StreamReader(
    loadSettings: LoadSettings,
    private val stream: BufferedSource,
) {

    /**
     * Read the provided [stream] of code points into a [String] and implement look-ahead operations.
     * Checks if code points are in the allowed range.
     *
     * @param stream the input
     * @param loadSettings configuration options
     */
    constructor(
        loadSettings: LoadSettings,
        stream: String,
    ) : this(
        loadSettings = loadSettings,
        stream = Buffer().write(stream.encodeUtf8()),
    )

    /**
     * Read the provided [stream] of code points into a [String] and implement look-ahead operations.
     * Checks if code points are in the allowed range.
     *
     * @param stream the input
     * @param loadSettings configuration options
     */
    constructor(
        loadSettings: LoadSettings,
        stream: Source,
    ) : this(
        loadSettings = loadSettings,
        stream = stream.buffer(),
    )

    private val name: String = loadSettings.label

    private val useMarks: Boolean = loadSettings.useMarks

    private val bufferSize = loadSettings.bufferSize

    /**
     * Cached codepoints from [stream].
     *
     * Data will be added when it is [peeked][peek], and dropped when it is read.
     */
    private val codepointsBuffer: ArrayDeque<Int> = ArrayDeque(bufferSize)

    /**
     * Retain codepoints that have been removed from [codepointsBuffer], for use in [getMark].
     */
    private val codepointsBufferHistory: ArrayDeque<Int> = ArrayDeque(bufferSize)

    /**
     * Current position as number (in characters) from the beginning [stream].
     *
     * [index] is only required to implement 1024 key length restriction and the total length restriction.
     */
    var index = 0
        private set

    /** [index] of the current position (in characters) from the beginning of the current document. */
    var documentIndex = 0 // (only used for limiting)
        private set

    /** Current line from the beginning of the stream. */
    var line = 0
        private set

    /** Current position as number (in characters) from the beginning of the current [line] */
    var column = 0
        private set

    /** Generate [Mark] of the current position, or `null` if [LoadSettings.useMarks] is `false`. */
    fun getMark(): Mark? {
        if (!useMarks) return null

        return Mark(
            name = name,
            index = index,
            line = line,
            column = column,
            codepoints = buildList {
                addAll(codepointsBufferHistory)
                addAll(codepointsBuffer)
            },
            pointer = codepointsBufferHistory.size,
        )
    }

    /**
     * Read the next [length] characters and move the pointer.
     *
     * If the last character is high surrogate one more character will be read.
     *
     * @param length number of characters to move forward.
     */
    @JvmOverloads
    fun forward(length: Int = 1) {
        var read = 0
        while (ensureEnoughData() && read < length) {
            val codepoint = codepointsBuffer.removeFirstOrNull() ?: return

            if (useMarks) {
                codepointsBufferHistory.addLast(codepoint)
                while (codepointsBufferHistory.size > bufferSize) {
                    codepointsBufferHistory.removeFirst()
                }
            }

            if (
                CharConstants.LINEBR.has(codepoint)
                ||
                codepoint == '\r'.code && peek() != '\n'.code
            ) {
                line++
                column = 0
            } else if (codepoint != ZWNBSP_CODEPOINT) {
                column++
            }

            val count = Character.charCount(codepoint)
            moveIndices(count)
            read += count
        }
    }

    /** @returns `true` if there are no more characters, `false` otherwise. */
    fun isEmpty(): Boolean = peek() == 0

    /**
     * Peek the next [index]-th codepoint.
     *
     * [index] should be greater or equal to 0.
     *
     * @param index to peek
     * @return the next [index]-th codepoint or `0` if empty
     */
    @JvmOverloads
    fun peek(index: Int = 0): Int {
        ensureEnoughData(index)
        return codepointsBuffer.getOrNull(index) ?: 0
    }

    /**
     * Create [String] from codepoints.
     *
     * @param length amount of the characters to convert
     * @return the [String] representation
     */
    fun prefix(length: Int): String {
        if (length == 0) return ""

        return buildString {
            while (this.length < length && ensureEnoughData()) {
                val codepoint = codepointsBuffer.getOrNull(this.length) ?: break
                appendCodePoint(codepoint)
            }
        }
    }

    /**
     * `prefix(length)` immediately followed by `forward(length)`
     *
     * @param length amount of characters to get
     * @return the next [length] code points, as a [String].
     */
    fun prefixForward(length: Int): String {
        val prefix = prefix(length)
        forward(length)
        return prefix
    }

    private fun ensureEnoughData(index: Int = 1): Boolean {
        if (index >= codepointsBuffer.size) {
            update()
        }
        return index <= codepointsBuffer.size
    }

    private fun update() {
        try {
            while (!stream.exhausted() && codepointsBuffer.size < bufferSize) {
                val codepoint = stream.readUtf8CodePointOrNull()
                    ?: break

                codepointsBuffer.addLast(codepoint)

                if (!isPrintable(codepoint)) {
                    throw ReaderException(
                        name = name,
                        position = index + codepointsBuffer.size,
                        codePoint = codepoint,
                        message = "special characters are not allowed",
                    )
                }
            }
        } catch (ioe: IOException) {
            throw YamlEngineException(ioe)
        }
    }

    /**
     * ```
     * index += length
     * documentIndex += length
     * ```
     */
    private fun moveIndices(length: Int) {
        index += length
        documentIndex += length
    }

    /**
     * Reset the position to start (at the start of a new document in the stream).
     */
    fun resetDocumentIndex() {
        documentIndex = 0
    }

    companion object {

        /**
         * The name `ZWNBSP` should be used if the BOM appears in the middle of a data stream.
         * Unicode says it should be interpreted as a normal codepoint (namely a word joiner), not as a BOM.
         */
        private const val ZWNBSP_CODEPOINT = 0xFEFF

        /**
         * Check if the all the data is human-readable
         * (used in [it.krzeminski.snakeyaml.engine.kmp.representer.Representer])
         *
         * @param data content to be checked for human-readability
         * @return `true` only when everything is human-readable
         */
        @JvmStatic
        fun isPrintable(data: String): Boolean {
            val length = data.length
            var offset = 0
            while (offset < length) {
                val codePoint = data.codePointAt(offset)
                if (!isPrintable(codePoint)) {
                    return false
                }
                offset += Character.charCount(codePoint)
            }
            return true
        }

        /**
         * Check if the code point is human-readable
         *
         * @param c code point to be checked for human-readability
         * @return `true` only when the code point is human-readable
         */
        @JvmStatic
        fun isPrintable(c: Int): Boolean {
            return c in 0x20..0x7E
                || c == 0x9
                || c == 0xA
                || c == 0xD
                || c == 0x85
                || c in 0xA0..0xD7FF
                || c in 0xE000..0xFFFD
                || c in 0x10000..0x10FFFF
        }

        private fun BufferedSource.readUtf8CodePointOrNull(): Int? {
            return try {
                readUtf8CodePoint()
            } catch (ex: EOFException) {
                null
            }
        }
    }
}
