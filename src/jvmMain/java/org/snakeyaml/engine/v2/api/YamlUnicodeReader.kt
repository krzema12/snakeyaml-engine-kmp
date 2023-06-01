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
package org.snakeyaml.engine.v2.api

import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.toByteString
import okio.Source
import okio.Timeout
import okio.buffer
import java.nio.charset.Charset
import kotlin.text.Charsets.UTF_16BE
import kotlin.text.Charsets.UTF_16LE
import kotlin.text.Charsets.UTF_32BE
import kotlin.text.Charsets.UTF_32LE
import kotlin.text.Charsets.UTF_8

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
    val encoding: Charset

    /**
     * Read-ahead four bytes and check for BOM marks. Extra bytes are unread back to the stream, only
     * BOM bytes are skipped.
     */
    init {
        val bufferedSource = source.buffer()
        val peek = bufferedSource.peek()

        val (encoding: Charset, skip: Int) = when {
            peek.rangeEquals(0, UTF_32LE_MARK) -> UTF_32LE to 4
            peek.rangeEquals(0, UTF_32BE_MARK) -> UTF_32BE to 4
            peek.rangeEquals(0, UTF_16LE_MARK) -> UTF_16LE to 2
            peek.rangeEquals(0, UTF_16BE_MARK) -> UTF_16BE to 2
            peek.rangeEquals(0, UTF_8_MARK)    -> UTF_8 to 3
            else                               -> UTF_8 to 0
        }
        this.encoding = encoding

        bufferedSource.skip(skip.toLong())

        val bs = bufferedSource.readString(encoding)
        encodedSource = Buffer().writeString(bs, UTF_8)
    }

    override fun close() {
        encodedSource.close()
    }

    override fun read(sink: Buffer, byteCount: Long): Long = encodedSource.read(sink, byteCount)

    override fun timeout(): Timeout = encodedSource.timeout()

    fun readString(): String = encodedSource.buffer().readUtf8()

    companion object {
        private val UTF_8_MARK = ByteString(0xEF, 0xBB, 0xBF)
        private val UTF_16BE_MARK = ByteString(0xFE, 0xFF)
        private val UTF_16LE_MARK = ByteString(0xFF, 0xFE)
        private val UTF_32BE_MARK = ByteString(0x00, 0x00, 0xFE, 0xFF)
        private val UTF_32LE_MARK = ByteString(0xFF, 0xFE, 0x00, 0x00)

        private fun ByteString(vararg values: Int): ByteString =
            values.map(Int::toByte).toByteArray().toByteString()
    }
}
