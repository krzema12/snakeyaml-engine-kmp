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

import okio.*
import okio.ByteString.Companion.encodeUtf8
import it.krzeminski.snakeyaml.engine.kmp.internal.utils.Character
import it.krzeminski.snakeyaml.engine.kmp.internal.utils.codePointAt
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.common.CharConstants
import it.krzeminski.snakeyaml.engine.kmp.exceptions.Mark
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ReaderException
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException
import it.krzeminski.snakeyaml.engine.kmp.internal.utils.appendCodePoint
import it.krzeminski.snakeyaml.engine.kmp.internal.utils.readUtf8WithLimit
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

/**
 * Reads the provided [stream] of code points, and implements look-ahead operations.
 *
 * Checks if code points are in the allowed range.
 * If [stream] contains invalid UTF-8-encoded bytes, they will be replaced with `?`.
 *
 * @param loadSettings configuration options
 * @param stream the input
 */
class StreamReader(
    loadSettings: LoadSettings,
    stream: Source,
) {
    init {
        require(loadSettings.bufferSize >= 4) { "buffer size must be at least 4 bytes to be able to read all Unicode codepoints" }
    }
    private val stream: BufferedSource = if (stream is BufferedSource) stream else stream.buffer()

    private val name: String = loadSettings.label

    private val useMarks: Boolean = loadSettings.useMarks

    /**
     * Size in bytes what will be used to request a new portion of data from [stream]
     */
    private val bufferReadSize: Long = loadSettings.bufferSize.toLong()

    /** Read data (as a moving window for input stream) */
    private var codePointsWindow: IntArray = IntArray(0)

    /** Real length of the data in dataWindow */
    private var dataLength = 0

    /** The variable points to the current position in the data array */
    private var pointer = 0

    private var eof = false

    /**
     * Current position as number (in characters) from the beginning [stream].
     *
     * [index] is only required to implement 1024 key length restriction and the total length restriction.
     */
    var index = 0 // in code points
        private set

    /**
     * [index] of the current position from the beginning of the current document.
     */
    var documentIndex = 0 // current document index in code points (only for limiting)
        private set

    /** Current line from the beginning of the stream. */
    var line = 0
        private set

    /** Current position as number (in characters) from the beginning of the current [line] */
    var column = 0
        private set

    /**
     * Read the provided String into a [Buffer] and implement look-ahead operations.
     *
     * Checks if code points are in the allowed range.
     *
     * @param loadSettings configuration options
     * @param stream the input
     */
    constructor(loadSettings: LoadSettings, stream: String) : this(
        loadSettings = loadSettings,
        stream = Buffer().write(stream.encodeUtf8()),
    )

    /** Generate [Mark] of the current position, or `null` if [LoadSettings.useMarks] is `false`. */
    fun getMark(): Mark? {
        if (!useMarks) return null

        return Mark(
            name = name,
            index = index,
            line = line,
            column = column,
            codepoints = codePointsWindow.asList(),
            pointer = pointer,
        )
    }

    /**
     * Read the next [length] characters and move the pointer.
     *
     * If the last character is high surrogate one more character will be read.
     *
     * @param length amount of characters to move forward.
     */
    @JvmOverloads
    fun forward(length: Int = 1) {
        repeat(length) {
            if (!ensureEnoughData()) return@repeat

            val c = codePointsWindow[pointer++]
            moveIndices(1)
            if (CharConstants.LINEBR.has(c) || c == '\r'.code && ensureEnoughData() && codePointsWindow[pointer] != '\n'.code) {
                line++
                column = 0
            } else if (c != 0xFEFF) {
                column++
            }
        }
    }

    /**
     * Peek the next code point.
     *
     * @return the next code point or `0` if empty
     */
    fun peek(): Int = if (ensureEnoughData()) codePointsWindow[pointer] else 0

    /**
     * Peek the next [index]-th code point.
     *
     * [index] **must** be greater than 0.
     *
     * @param index to peek
     * @return the next [index]-th code point or `0` if empty
     */
    fun peek(index: Int): Int = if (ensureEnoughData(index)) codePointsWindow[pointer + index] else 0

    /**
     * Create [String] from code points.
     *
     * @param length amount of the characters to convert
     * @return the [String] representation
     */
    fun prefix(length: Int): String {
        if (length == 0) return ""

        val stringLength = when {
            ensureEnoughData(length) -> length
            else                     -> length.coerceAtMost(dataLength - pointer)
        }

        return buildString(stringLength) {
            for (i in pointer until pointer + stringLength) {
                appendCodePoint(codePointsWindow[i])
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
        pointer += length
        moveIndices(length)
        // prefix never contains new line characters
        column += length
        return prefix
    }

    private fun ensureEnoughData(size: Int = 0): Boolean {
        if (!eof && pointer + size >= dataLength) {
            update()
        }
        return pointer + size < dataLength
    }

    private fun update() {
        try {
            val buffer = stream.readUtf8WithLimit(bufferReadSize)
            val read = buffer.length
            if (read > 0) {
                var cpIndex = dataLength - pointer
                codePointsWindow = codePointsWindow.copyOfRangeSafe(pointer, dataLength + read)
                var nonPrintable: Int? = null
                var i = 0
                while (i < read) {
                    val codePoint = buffer.codePointAt(i)
                    codePointsWindow[cpIndex] = codePoint
                    if (isPrintable(codePoint)) {
                        i += Character.charCount(codePoint)
                    } else {
                        nonPrintable = codePoint
                        i = read
                    }
                    cpIndex++
                }
                dataLength = cpIndex
                pointer = 0
                if (nonPrintable != null) {
                    throw ReaderException(
                        name = name,
                        position = cpIndex - 1,
                        codePoint = nonPrintable,
                        message = "special characters are not allowed",
                    )
                }
            } else {
                eof = true
            }
        } catch (ioe: IOException) {
            throw YamlEngineException(ioe)
        }
    }

    private fun moveIndices(length: Int) {
        index += length
        documentIndex += length
    }

    /**
     * Reset the position to start (at the start of a new document in the stream)
     */
    fun resetDocumentIndex() {
        documentIndex = 0
    }

    companion object {
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
         * @param c - code point to be checked for human-readability
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

        /**
         * Like [IntArray.copyOfRange], but allows for [toIndex] to be out-of-bounds.
         */
        private fun IntArray.copyOfRangeSafe(fromIndex: Int, toIndex: Int): IntArray =
            IntArray(toIndex - fromIndex) { getOrElse(fromIndex + it) { 0 } }
    }
}
