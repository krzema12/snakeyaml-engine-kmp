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

import it.krzeminski.snakeyaml.engine.kmp.api.YamlUnicodeReader.CharEncoding.*
import it.krzeminski.snakeyaml.engine.kmp.api.YamlUnicodeReader.CharEncoding.Companion.detectCharEncoding
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

    private fun interface CodepointReader {
        fun readCodepoint(source: BufferedSource): Int

        companion object {
            // For UTF-16, read one char at a time
            val UTF_16BE = CodepointReader { source -> source.readShort().toInt() }

            // For UTF-16LE, read one char at a time, but swap the first and last bytes
            val UTF_16LE = CodepointReader { source -> source.readShortLe().toInt() }

            // For UTF-32, read one Int at a time
            val UTF_32BE = CodepointReader { source -> source.readInt() }

            // For UTF-32LE, read one little-endian Int at a time
            val UTF_32LE = CodepointReader { source -> source.readIntLe() }
        }
    }

    private val codepointReader: CodepointReader? = when (encoding) {
        UTF_8    -> null // UTF8 is already supported by Okio and requires no special treatment
        UTF_16BE -> CodepointReader.UTF_16BE
        UTF_16LE -> CodepointReader.UTF_16LE
        UTF_32BE -> CodepointReader.UTF_32BE
        UTF_32LE -> CodepointReader.UTF_32LE
    }

    override fun read(sink: Buffer, byteCount: Long): Long {
        if (codepointReader != null) {
            Buffer().use { buffer ->
                while (source.request(encoding.charSize) && buffer.size < byteCount) {
                    val codepoint = codepointReader.readCodepoint(source)
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

    override fun close(): Unit = source.close()

    override fun timeout(): Timeout = source.timeout()

    /** Read the entire source as a [String]. */
    // This method is only used in tests - it should be made `internal` when all tests are Kotlin.
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

        companion object {
            /**
             * Peek-ahead and check for BOM marks.
             *
             * If a BOM is detected, the bytes will be skipped. Otherwise, no bytes will be skipped.
             */
            internal fun BufferedSource.detectCharEncoding(): CharEncoding? = select(UnicodeBomOptions)

            private val UnicodeBomOptions: TypedOptions<CharEncoding>
                get() {
                    // it's important to sort the entries so that larger BOMs are first,
                    // otherwise a UTF16 BOM will 'hide' a UTF32 BOM (since they start with the same bytes).
                    val sortedEntries = CharEncoding.entries.sortedByDescending { it.bom.size }

                    return TypedOptions.of(sortedEntries, CharEncoding::bom)
                }
        }
    }

    companion object
}
