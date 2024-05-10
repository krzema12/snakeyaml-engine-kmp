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
import it.krzeminski.snakeyaml.engine.kmp.internal.utils.codePointAt
import okio.*
import okio.ByteString.Companion.encodeUtf8
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

/**
 * Read the provided stream of code points into String and implement look-ahead operations. Checks
 * if code points are in the allowed range.
 *
 * @param loadSettings - configuration options
 * @param stream - the input
 */
@OptIn(ExperimentalStdlibApi::class)
class StreamReader(
    loadSettings: LoadSettings,
    private val stream: BufferedSource,
) : AutoCloseable {

    /**
     * Read the provided [stream] of code points into a [String] and implement look-ahead operations.
     * Checks if code points are in the allowed range.
     *
     * @param loadSettings configuration options
     * @param stream the input
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
     * @param loadSettings configuration options
     * @param stream the input
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

    /** Read data (as a moving window for input stream) */
    private val codepointsBuffer: ArrayDeque<Int> = ArrayDeque(bufferSize)

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
    var column = 0 // in code points
        private set

    /** Generate [Mark] of the current position, or `null` if [LoadSettings.useMarks] is `false`. */
    fun getMark(): Mark? {
        if (!useMarks) return null

        return Mark(
            name = name,
            index = index,
            line = line,
            column = column,
            codepoints = codepointsBuffer.toList(), // create a copy
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
            val codepoint = stream.readUtf8CodePointOrNull() ?: return

            moveIndices(Character.charCount(codepoint))

            if (
                CharConstants.LINEBR.has(codepoint)
                ||
                codepoint == '\r'.code && peek() != '\n'.code
            ) {
                line++
                column = 0
            } else if (codepoint != ZwnbspCodepoint) {
                column++
            }

        }

//        if (!ensureEnoughData(length)) return
//        repeat(length) {
//            val c = codepointsBuffer.removeFirst()
////            index++
////                codePointsWindow[pointer++]
//            moveIndices(1)
//            if (
//                CharConstants.LINEBR.has(c)
//                ||
//                c == '\r'.code && peek() != '\n'.code
//            ) {
//                line++
//                column = 0
//            } else if (c != 0xFEFF) {
//                column++
//            }
//        }
    }

    /**
     * Moves the pointer forward while [predicate] returns `true`.
     *
     * @param predicate The predicate function to apply to each character.
     * @returns the number characters that matched the predicate.
     */
    fun forwardWhile(predicate: (c: Char) -> Boolean): Int {
        val ff = peekCount(predicate)
        forward(ff)
        return ff
    }

    /** @returns `true` if there are no more characters, `false` otherwise. */
    fun isEmpty(): Boolean = peek() == 0

    /**
     * Peek the next [index]-th code point.
     *
     * [index] **must** be greater than 0.
     *
     * @param index to peek
     * @return the next [index]-th code point or `0` if empty
     */
    @JvmOverloads
    fun peek(index: Int = 0): Int {
        stream.peek().use { peek ->
            var i = 0
            while (i < index) {
                peek.readUtf8CodePointOrNull() ?: return 0
                i++
            }
            return peek.readUtf8CodePointOrNull() ?: 0
        }
//        val bytesToSkip = (index.coerceAtLeast(0) * Int.SIZE_BYTES).toLong()
//
//        return stream.peek().use { peek ->
//
//            if (!peek.request(bytesToSkip)) return 0
//            peek
//                .apply { skip(bytesToSkip) }
//                .readUtf8CodePointOrNull()
//                ?: 0
//        }
//        return if (ensureEnoughData(index)) codepointsBuffer[index] else 0
    }

    /**
     * Counts the number of consecutive characters for which [predicate] returns `true`.
     * The counting starts from the current position of the pointer and stops when the predicate returns `false`.
     *
     * @returns the number of consecutive characters that match [predicate]
     */
    fun peekCount(predicate: (c: Char) -> Boolean): Int {
        var ff = 0
        while (predicate(peek(ff).toChar())) {
            ff++
        }
        return ff
    }

    /**
     * Create [String] from code points.
     *
     * @param length amount of the characters to convert
     * @return the [String] representation
     */
    fun prefix(length: Int): String {
        if (length == 0) return ""

        return stream.peek().use { peek ->
            buildString {
                var i = 0
                while (i++ < length) {
                    val codepoint = peek.readUtf8CodePointOrNull() ?: break
                    val c = Character.toChars(codepoint)
                    append(c)
                }
            }
        }

//        return codepointsBuffer.take(length).fold(StringBuilder()) { sb, codepoint ->
//            val c = Character.toChars(codepoint)
//            sb.append(c)
////            sb.append(Char(codepoint))
//        }.toString()
    }

    /**
     * `prefix(length)` immediately followed by `forward(length)`
     *
     * @param length amount of characters to get
     * @return the next [length] code points, as a [String].
     */
    fun prefixForward(length: Int): String {
        val prefix = prefix(length)
        repeat(length) {
            stream.readUtf8CodePointOrNull()
        }
        moveIndices(length)
//        pointer += length
//        moveIndices(length)
        // prefix never contains new line characters
        column += length
        return prefix
    }

    private fun ensureEnoughData(index: Int = 0): Boolean {
        if (codepointsBuffer.size < index) {
            update()
        }
        return index in codepointsBuffer.indices
    }

    private fun update() {
        if (stream.exhausted() || stream.request(1)) {
            stream.close()
            return
        }

        try {
            Buffer().use { buffer ->

                val read = stream.read(buffer, Int.SIZE_BYTES * 256L)
                if (read <= 0) {
                    stream.close()
                    buffer.close()
                    return
                }

                fun appendCodepoint(codepoint: Int) {
                    if (!isPrintable(codepoint)) {
                        throw ReaderException(
                            name = name,
                            position = index,
                            codePoint = codepoint,
                            message = "special characters are not allowed",
                        )
                    }
                    codepointsBuffer.addLast(codepoint)
                }

                while (!buffer.exhausted()) {
                    val codepoint = buffer.readInt()
                    appendCodepoint(codepoint)
                }

                val lastChar = codepointsBuffer.lastOrNull()
                if (lastChar != null) {
                    if (Character.highSurrogateOf(lastChar).isHighSurrogate()) {
                        val codepoint = stream.readInt()
                        appendCodepoint(codepoint)
                    }
                }
            }

//            val line = stream.readUtf8Line() ?: run {
//                println("stream is finished - stream.readUtf8Line() returned null")
//                return
//            }
//
//            codepointsBuffer += line.map { it.code }
//            println("read line $line, codepointsBuffer:$codepointsBuffer")

//            var i = 0
//            while (i < line.length) {
//                val codepoint = line.codePointAt(i)
//
//                if (!isPrintable(codepoint)) {
//                    throw ReaderException(
//                        name = name,
//                        position = index + codepointsBuffer.size,
//                        codePoint = codepoint,
//                        message = "special characters are not allowed",
//                    )
//                }
//
//                codepointsBuffer.addLast(codepoint)
//
//                i += Character.charCount(codepoint)
//            }
//            codepointsBuffer += '\n'.code
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
     * Reset the position to start (at the start of a new document in the stream)
     */
    fun resetDocumentIndex() {
        documentIndex = 0
    }

    override fun close() {
        stream.close()
    }

    // TODO rename readUtf8CodePointOrNull - 'OrNull' implies it doesn't throw,
    //      but it does if codepoint is not printable
    private fun BufferedSource.readUtf8CodePointOrNull(): Int? {
        // TODO readUtf8CodePointOrNull gets called multiple times - during peeking and forwarding
        //      StreamReader should be refactored to read chars into a buffer.
        return try {
            val codepoint = readUtf8CodePoint()
            if (!isPrintable(codepoint)) {
                throw ReaderException(
                    name = name,
                    position = 0,
                    codePoint = codepoint,
                    message = "special characters are not allowed",
                )
            }
            codepoint
        } catch (ex: EOFException) {
            return null
        }
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
            IntArray(toIndex - fromIndex) { getOrNull(fromIndex + it) ?: 0 }
    }
}


/**
 * The name `ZWNBSP` should be used if the BOM appears in the middle of a data stream.
 * Unicode says it should be interpreted as a normal codepoint (namely a word joiner), not as a BOM.
 */
private const val ZwnbspCodepoint = 0xFEFF
