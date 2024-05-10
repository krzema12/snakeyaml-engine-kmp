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
package it.krzeminski.snakeyaml.engine.kmp.api

import it.krzeminski.snakeyaml.engine.kmp.api.YamlUnicodeReader.CharEncoding
import it.krzeminski.snakeyaml.engine.kmp.api.YamlUnicodeReader.CharEncoding.*
import it.krzeminski.snakeyaml.engine.kmp.api.YamlUnicodeReader.CodepointReader
import okio.*
import okio.ByteString.Companion.decodeHex

/**
 * Generic unicode text reader, which will use BOM mark to identify the encoding to be used. If BOM
 * is not found then use a given default or system encoding.
 *
 * version: 1.1 / 2007-01-25 - changed BOM recognition ordering (longer boms first)
 *
 * Original pseudocode : Thomas Weidenfeller Implementation tweaked: Aki Nieminen Implementation
 * changed: Andrey Somov no default encoding must be provided - UTF-8 is used by default
 * (http://www.yaml.org/spec/1.2/spec.html#id2771184)
 *
 * http://www.unicode.org/unicode/faq/utf_bom.html BOMs:
 *
 * * `00 00 FE FF` = UTF-32, big-endian
 * * `FF FE 00 00` = UTF-32, little-endian
 * * `FE FF` = UTF-16, big-endian
 * * `FF FE` = UTF-16, little-endian
 * * `EF BB BF` = UTF-8
 *
 * Win2k Notepad: Unicode format = UTF-16LE
 * @param source [Source] to be read
 */
class YamlUnicodeReader(
    private val source: BufferedSource,
) : Source {

    constructor(source: Source) : this(source.buffer())

    /** The character encoding being used by [source]. */
    val encoding: CharEncoding = source.detectCharEncoding() ?: UTF_8

    private val codepointReader: CodepointReader? = when (encoding) {
        // UTF8 is the default Kotlin & Okio encoding, and requires no special treatment
        UTF_8    -> null

        // For UTF-16, read one char at a time
        UTF_16BE -> CodepointReader { readShort().toInt() }

        // For UTF-16LE, read one char at a time, but swap the first and last bytes
        UTF_16LE -> CodepointReader { readShortLe().toInt() }

        // For UTF-32, read one Int at a time
        UTF_32BE -> CodepointReader { readInt() }

        // For UTF-32LE, read one Int at a time, but swap the first and last bytes
        UTF_32LE -> CodepointReader { readIntLe() }
    }

    override fun close(): Unit = source.close()

    override fun read(sink: Buffer, byteCount: Long): Long {
        if (codepointReader != null) {
            Buffer().use { buffer ->
                while (source.request(encoding.charSize) && buffer.size < byteCount) {
                    val codepoint = with(codepointReader) { source.readCodepoint() }
                    buffer.writeUtf8CodePoint(codepoint)
                }
                if (buffer.size <= 0L) return -1
                buffer.copyTo(sink)
                return buffer.size
            }
        } else {
            return source.read(sink, byteCount)
        }
    }

    override fun timeout(): Timeout = source.timeout()

    fun readString(): String = buffer().readUtf8()

    enum class CharEncoding(
        internal val bom: ByteString,
        internal val charSize: Long,
    ) {
        UTF_8(bom = "efbbbf", charSize = Short.SIZE_BYTES),
        UTF_16BE(bom = "feff", charSize = Short.SIZE_BYTES),
        UTF_16LE(bom = "fffe", charSize = Short.SIZE_BYTES),
        UTF_32BE(bom = "0000feff", charSize = Int.SIZE_BYTES),
        UTF_32LE(bom = "fffe0000", charSize = Int.SIZE_BYTES),
        ;

        constructor(
            bom: String,
            charSize: Int,
        ) : this(
            bom = bom.decodeHex(),
            charSize = charSize.toLong(),
        )
    }

    internal fun interface CodepointReader {
        fun BufferedSource.readCodepoint(): Int
    }

    companion object {

        private fun ByteString.Companion.of(vararg values: Int): ByteString =
            values.map(Int::toByte).toByteArray().toByteString()

//        private fun BufferedSource.concatToString(
//            readChar: BufferedSource.() -> Char,
//        ): String {
//            return sequence {
//                while (!exhausted()) yield(readChar())
//            }.toList()
//                .toCharArray()
//                .concatToString()
//        }
    }
}


/**
 * Peek-ahead and check for BOM marks.
 *
 * If a BOM is detected, the bytes will be skipped. Otherwise, no bytes will be skipped.
 */
private fun BufferedSource.detectCharEncoding(): CharEncoding? = select(UnicodeBomOptions)


private val UnicodeBomOptions: TypedOptions<CharEncoding>
    get() {
        // it's important to sort the entries so that larger BOMs are first,
        // otherwise a UTF16 BOM will 'hide' a UTF32 BOM (since they start with the same bytes).
        val sortedEntries = CharEncoding.entries.sortedByDescending { it.bom.size }

        return TypedOptions.of(sortedEntries, CharEncoding::bom)
    }

//
//        /**
//         * Rounds up a number to the next highest multiple of [factor].
//         *
//         * Examples:
//         *
//         * - `0.roundToMultiple(2) -> 0`
//         * - `1.roundToMultiple(2) -> 2`
//         * - `2.roundToMultiple(2) -> 2`
//         * - `3.roundToMultiple(2) -> 4`
//         */
//        private fun Long.roundToMultiple(factor: Int): Long =
//                this + factor - 1 - (this + factor - 1) % factor


//private sealed class EncodedBufferedSource(
//    private val source: BufferedSource,
//) : Source {
//
//    override fun read(sink: Buffer, byteCount: Long): Long {
//        Buffer().use { buffer ->
//            while (source.request(charSize) && buffer.size < byteCount) {
//                val codepoint = source.readCodepoint()
//                buffer.writeUtf8CodePoint(codepoint)
//            }
//            if (buffer.size <= 0L) return -1
//            buffer.copyTo(sink)
//
//            return buffer.size
//        }
//    }
//
//    abstract val charSize: Long
//    abstract fun BufferedSource.readCodepoint(): Int
//
//    override fun timeout(): Timeout = source.timeout()
//
//    override fun close(): Unit = source.close()
//
//    class Utf16BE(source: BufferedSource) : EncodedBufferedSource(source) {
//        override val charSize: Long = Short.SIZE_BYTES.toLong()
//        override fun BufferedSource.readCodepoint(): Int =
//            readShort().toInt()
//    }
//
//    class Utf16LE(source: BufferedSource) : EncodedBufferedSource(source) {
//        override val charSize: Long = Short.SIZE_BYTES.toLong()
//        override fun BufferedSource.readCodepoint(): Int =
//            readShortLe().toInt()
//    }
//
//    class Utf32BE(source: BufferedSource) : EncodedBufferedSource(source) {
//        override val charSize: Long = Int.SIZE_BYTES.toLong()
//        override fun BufferedSource.readCodepoint(): Int =
//            readInt()
//    }
//
//    class Utf32LE(source: BufferedSource) : EncodedBufferedSource(source) {
//        override val charSize: Long = Int.SIZE_BYTES.toLong()
//        override fun BufferedSource.readCodepoint(): Int =
//            readIntLe()
//    }
//}
