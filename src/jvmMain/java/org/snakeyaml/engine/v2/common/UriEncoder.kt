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

import org.snakeyaml.engine.external.net.thauvin.erik.urlencoder.UrlEncoder
import org.snakeyaml.engine.v2.exceptions.YamlEngineException
import java.io.UnsupportedEncodingException
import java.nio.ByteBuffer

object UriEncoder {
    private val urlEncoder = UrlEncoder(
        safeChars = "-_.!~*'()@:$&,;=[]/", // Include the [] chars to avoid its escape as required by spec
        plusForSpace = false,
    )

    /**
     * Escape special characters with `%`
     *
     * @param uri URI to be escaped
     * @return encoded URI
     */
    @JvmStatic
    fun encode(uri: String): String = urlEncoder.encode(uri)

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
        val content = String(buff.array(), Charsets.UTF_8)
        return urlEncoder.decode(content)
    }

    /**
     * Decode with [UrlEncoder]
     *
     * @param buff - the source
     * @return decoded with UTF-8
     */
    @JvmStatic
    fun decode(buff: String): String {
        return try {
            urlEncoder.decode(buff)
        } catch (e: UnsupportedEncodingException) {
            throw YamlEngineException(e)
        }
    }
}
