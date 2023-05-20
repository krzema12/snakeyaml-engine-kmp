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
package org.snakeyaml.engine.v2.common

import org.snakeyaml.engine.external.com.google.gdata.util.common.base.Escaper
import org.snakeyaml.engine.external.com.google.gdata.util.common.base.PercentEscaper
import org.snakeyaml.engine.v2.exceptions.YamlEngineException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

object UriEncoder {
    private val UTF8Decoder = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)

    // Include the [] chars to the SAFEPATHCHARS_URLENCODER to avoid
    // its escape as required by spec. See
    private const val SAFE_CHARS = PercentEscaper.SAFEPATHCHARS_URLENCODER + "[]/"
    private val escaper: Escaper = PercentEscaper(SAFE_CHARS, false)

    /**
     * Escape special characters with `%`
     *
     * @param uri URI to be escaped
     * @return encoded URI
     */
    @JvmStatic
    fun encode(uri: String): String = escaper.escape(uri)

    /**
     * Decode `%`-escaped characters. Decoding fails in case of invalid UTF-8
     *
     * @param buff data to decode
     * @return decoded data
     * @throws CharacterCodingException if [buff] cannot be decoded
     */
    @JvmStatic
    @Throws(CharacterCodingException::class)
    fun decode(buff: ByteBuffer): String {
        val chars = UTF8Decoder.decode(buff)
        return chars.toString()
    }

    /**
     * Decode with [URLDecoder]
     *
     * @param buff - the source
     * @return decoded with UTF-8
     */
    @JvmStatic
    fun decode(buff: String?): String {
        return try {
            URLDecoder.decode(buff, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            throw YamlEngineException(e)
        }
    }
}
