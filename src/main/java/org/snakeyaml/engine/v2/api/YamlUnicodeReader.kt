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

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PushbackInputStream
import java.io.Reader
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

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
 * http://www.unicode.org/unicode/faq/utf_bom.html BOMs: 00 00 FE FF = UTF-32, big-endian FF FE 00
 * 00 = UTF-32, little-endian FE FF = UTF-16, big-endian FF FE = UTF-16, little-endian EF BB BF =
 * UTF-8
 *
 * Win2k Notepad: Unicode format = UTF-16LE
 * @param stream InputStream to be read
 */
class YamlUnicodeReader(stream: InputStream?) : Reader() {
    private var internalIn: PushbackInputStream = PushbackInputStream(stream, BOM_SIZE)
    private var internalIn2: InputStreamReader? = null

    /**
     * Get stream encoding or NULL if stream is uninitialized. Call init() or read() method to
     * initialize it.
     *
     * @return the name of the character encoding being used by this stream.
     */
    @JvmField
    var encoding: Charset = UTF8

    /**
     * Read-ahead four bytes and check for BOM marks. Extra bytes are unread back to the stream, only
     * BOM bytes are skipped.
     *
     * @throws IOException if InputStream cannot be created
     */
    @Throws(IOException::class)
    fun init() {
        if (internalIn2 != null) {
            return
        }
        val bom = ByteArray(BOM_SIZE)
        val unread: Int
        val n: Int = internalIn.read(bom, 0, bom.size)
        if (bom[0] == 0x00.toByte() && bom[1] == 0x00.toByte() && bom[2] == 0xFE.toByte() && bom[3] == 0xFF.toByte()) {
            encoding = UTF32BE
            unread = n - 4
        } else if (bom[0] == 0xFF.toByte() && bom[1] == 0xFE.toByte() && bom[2] == 0x00.toByte() && bom[3] == 0x00.toByte()) {
            encoding = UTF32LE
            unread = n - 4
        } else if (bom[0] == 0xEF.toByte() && bom[1] == 0xBB.toByte() && bom[2] == 0xBF.toByte()) {
            encoding = UTF8
            unread = n - 3
        } else if (bom[0] == 0xFE.toByte() && bom[1] == 0xFF.toByte()) {
            encoding = UTF16BE
            unread = n - 2
        } else if (bom[0] == 0xFF.toByte() && bom[1] == 0xFE.toByte()) {
            encoding = UTF16LE
            unread = n - 2
        } else {
            // Unicode BOM mark not found, unread all bytes
            encoding = UTF8
            unread = n
        }
        if (unread > 0) {
            internalIn.unread(bom, n - unread, unread)
        }

        // Use given encoding
        val decoder = encoding.newDecoder().onUnmappableCharacter(CodingErrorAction.REPORT)
        internalIn2 = InputStreamReader(internalIn, decoder)
    }

    @Throws(IOException::class)
    override fun close() {
        init()
        internalIn2!!.close()
    }

    @Throws(IOException::class)
    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        init()
        return internalIn2!!.read(cbuf, off, len)
    }

    companion object {
        private val UTF8 = StandardCharsets.UTF_8
        private val UTF16BE = StandardCharsets.UTF_16BE
        private val UTF16LE = StandardCharsets.UTF_16LE
        private val UTF32BE = Charset.forName("UTF-32BE")
        private val UTF32LE = Charset.forName("UTF-32LE")
        private const val BOM_SIZE = 4
    }
}
