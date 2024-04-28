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

import okio.*
import okio.ByteString.Companion.toByteString
import it.krzeminski.snakeyaml.engine.kmp.api.YamlUnicodeReader.CharEncoding.*

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
    source: Source,
) : Source {

    private val encodedSource: Source

    /** the name of the character encoding being used by this stream. */
    val encoding: CharEncoding

    /**
     * Read-ahead four bytes and check for BOM marks. Extra bytes are unread back to the stream, only
     * BOM bytes are skipped.
     */
    init {
        val bufferedSource = source.buffer()
        val peek = bufferedSource.peek()

        val detectedEncoding: CharEncoding? = when {
            peek.rangeEquals(0, UTF_32LE.bom) -> UTF_32LE
            peek.rangeEquals(0, UTF_32BE.bom) -> UTF_32BE
            peek.rangeEquals(0, UTF_16LE.bom) -> UTF_16LE
            peek.rangeEquals(0, UTF_16BE.bom) -> UTF_16BE
            peek.rangeEquals(0, UTF_8.bom)    -> UTF_8
            else                              -> null // no BOM detected
        }

        if (detectedEncoding != null) {
            bufferedSource.skip(detectedEncoding.bom.size.toLong())
        }

        this.encoding = detectedEncoding ?: UTF_8

        val bs = readString(bufferedSource, encoding)
        encodedSource = Buffer().writeUtf8(bs)
    }

    private fun readString(source: BufferedSource, encoding: CharEncoding): String {
        return when (encoding) {
            UTF_8    -> source.readUtf8()

            // For UTF-16, read the entire Buffer one char at a time
            UTF_16BE -> source.concatToString { readShort().toInt().toChar() }

            // For UTF-16LE, do the same thing but swap
            // the first and last bytes of the Char before creating a string
            UTF_16LE -> source.concatToString { readShortLe().toInt().toChar() }

            // For UTF-32, read the buffer an Int at a time
            UTF_32BE -> source.concatToString { readInt().toChar() }

            UTF_32LE -> source.concatToString { readIntLe().toChar() }
        }
    }

    override fun close(): Unit = encodedSource.close()

    override fun read(sink: Buffer, byteCount: Long): Long = encodedSource.read(sink, byteCount)

    override fun timeout(): Timeout = encodedSource.timeout()

    fun readString(): String = encodedSource.buffer().readUtf8()

    enum class CharEncoding(
        val bom: ByteString,
    ) {
        UTF_8(0xEF, 0xBB, 0xBF),
        UTF_16BE(0xFE, 0xFF),
        UTF_16LE(0xFF, 0xFE),
        UTF_32BE(0x00, 0x00, 0xFE, 0xFF),
        UTF_32LE(0xFF, 0xFE, 0x00, 0x00),
        ;

        constructor(vararg values: Int) : this(
            values.map(Int::toByte).toByteArray().toByteString(),
        )
    }

    companion object {
        private fun BufferedSource.concatToString(
            readChar: BufferedSource.() -> Char,
        ): String {
            return sequence {
                while (!exhausted()) yield(readChar())
            }.toList()
                .toCharArray()
                .concatToString()
        }
    }
}
