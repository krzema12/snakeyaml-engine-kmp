/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Copyright 2022-2023 Erik C. Thauvin (erik@thauvin.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Adapted version of UrlEncoder.kt
// - converted to a class
// - converted safeChars to class constructor parameter
// - replaced BitSet with BooleanArray
// - updated ByteArray operations to use Kotlin Multiplatform equivalents
// - added some assertions (based on Google PercentEscaper)
// https://github.com/ethauvin/urlencoder/blob/34b69a7d1f3570aa056285253376ed7a7bde03d8/lib/src/main/kotlin/net/thauvin/erik/urlencoder/UrlEncoder.kt

package org.snakeyaml.engine.external.net.thauvin.erik.urlencoder

import org.snakeyaml.engine.internal.utils.Character
import org.snakeyaml.engine.internal.utils.codePointAt

/**
 * Most defensive approach to URL encoding and decoding.
 *
 * - Rules determined by combining the unreserved character set from
 * [RFC 3986](https://www.rfc-editor.org/rfc/rfc3986#page-13) with the percent-encode set from
 * [application/x-www-form-urlencoded](https://url.spec.whatwg.org/#application-x-www-form-urlencoded-percent-encode-set).
 *
 * - Both specs above support percent decoding of two hexadecimal digits to a binary octet, however their unreserved
 * set of characters differs and `application/x-www-form-urlencoded` adds conversion of space to `+`, which has the
 * potential to be misunderstood.
 *
 * - This library encodes with rules that will be decoded correctly in either case.
 *
 * @param safeChars a non-null string specifying additional safe characters for this escaper (the
 * ranges `0..9`, `a..z` and `A..Z` are always safe and should not be specified here)
 * @param plusForSpace `true` if ASCII space should be escaped to `+` rather than `%20`
 *
 * @author Geert Bevin (gbevin(remove) at uwyn dot com)
 * @author Erik C. Thauvin (erik@thauvin.net)
 **/
internal class UrlEncoder(
    safeChars: String,
    private val plusForSpace: Boolean,
) {
    private val hexDigits: CharArray = "0123456789ABCDEF".toCharArray()

    // see https://www.rfc-editor.org/rfc/rfc3986#page-13
    // and https://url.spec.whatwg.org/#application-x-www-form-urlencoded-percent-encode-set
    private val unreservedChars = createUnreservedChars(safeChars)

    init {
        // Avoid any misunderstandings about the behavior of this escaper
        require(!safeChars.matches(".*[0-9A-Za-z].*".toRegex())) {
            "Alphanumeric characters are always 'safe' and should not be explicitly specified"
        }
        // Avoid ambiguous parameters. Safe characters are never modified so if
        // space is a safe character then setting plusForSpace is meaningless.
        require(!(plusForSpace && ' ' in safeChars)) {
            "plusForSpace cannot be specified when space is a 'safe' character"
        }
        require('%' !in safeChars) {
            "The '%' character cannot be specified as 'safe'"
        }
    }

    /**
     * Transforms a provided [String] into a new string, containing decoded URL characters in the UTF-8
     * encoding.
     */
    fun decode(
        source: String,
        plusToSpace: Boolean = plusForSpace,
    ): String {
        if (source.isEmpty()) return source

        val length = source.length
        val out = StringBuilder(length)
        var ch: Char
        var bytesBuffer: ByteArray? = null
        var bytesPos = 0
        var i = 0
        var started = false
        while (i < length) {
            ch = source[i]
            if (ch == '%') {
                if (!started) {
                    out.append(source, 0, i)
                    started = true
                }
                if (bytesBuffer == null) {
                    // the remaining characters divided by the length of the encoding format %xx, is the maximum number
                    // of bytes that can be extracted
                    bytesBuffer = ByteArray((length - i) / 3)
                }
                i++
                require(length >= i + 2) { "Incomplete trailing escape ($ch) pattern" }
                try {
                    val v = source.substring(i, i + 2).toInt(16)
                    require(v in 0..0xFF) { "Illegal escape value" }
                    bytesBuffer[bytesPos++] = v.toByte()
                    i += 2
                } catch (e: NumberFormatException) {
                    throw IllegalArgumentException("Illegal characters in escape sequence: $e.message", e)
                }
            } else {
                if (bytesBuffer != null) {
                    out.append(bytesBuffer.decodeToString(0, bytesPos))
                    started = true
                    bytesBuffer = null
                    bytesPos = 0
                }
                if (plusToSpace && ch == '+') {
                    if (!started) {
                        out.append(source, 0, i)
                        started = true
                    }
                    out.append(" ")
                } else if (started) {
                    out.append(ch)
                }
                i++
            }
        }

        if (bytesBuffer != null) {
            out.append(bytesBuffer.decodeToString(0, bytesPos))
        }

        return if (!started) source else out.toString()
    }

    /**
     * Transforms a provided [String] object into a new string, containing only valid URL
     * characters in the UTF-8 encoding.
     *
     * - Letters, numbers, unreserved (`_-!.'()*`) and allowed characters are left intact.
     */
    fun encode(
        source: String,
        spaceToPlus: Boolean = plusForSpace,
    ): String {
        if (source.isEmpty()) {
            return source
        }
        var out: StringBuilder? = null
        var ch: Char
        var i = 0
        while (i < source.length) {
            ch = source[i]
            if (ch.isUnreserved()) {
                out?.append(ch)
                i++
            } else {
                if (out == null) {
                    out = StringBuilder(source.length)
                    out.append(source, 0, i)
                }
                val cp = source.codePointAt(i)
                if (cp < 0x80) {
                    if (spaceToPlus && ch == ' ') {
                        out.append('+')
                    } else {
                        out.appendEncodedByte(cp)
                    }
                    i++
                } else if (Character.isBmpCodePoint(cp)) {
                    for (b in ch.toString().encodeToByteArray()) {
                        out.appendEncodedByte(b.toInt())
                    }
                    i++
                } else if (Character.isSupplementaryCodePoint(cp)) {
                    val high = Character.highSurrogateOf(cp)
                    val low = Character.lowSurrogateOf(cp)
                    for (b in charArrayOf(high, low).concatToString().encodeToByteArray()) {
                        out.appendEncodedByte(b.toInt())
                    }
                    i += 2
                }
            }
        }

        return out?.toString() ?: source
    }

    /**
     * see https://www.rfc-editor.org/rfc/rfc3986#page-13
     * and https://url.spec.whatwg.org/#application-x-www-form-urlencoded-percent-encode-set
     */
    private fun Char.isUnreserved(): Boolean = this <= 'z' && unreservedChars[code]

    private fun StringBuilder.appendEncodedDigit(digit: Int) {
        append(hexDigits[digit and 0x0F])
    }

    private fun StringBuilder.appendEncodedByte(ch: Int) {
        append("%")
        appendEncodedDigit(ch shr 4)
        appendEncodedDigit(ch)
    }

    /**
     * Creates a [BooleanArray] with entries corresponding to the character values for
     * `0-9`, `A-Z`, `a-z` and those specified in [safeChars] set to `true`.
     *
     * The array is as small as is required to hold the given character information.
     */
    private fun createUnreservedChars(safeChars: String): BooleanArray {
        val safeCharArray = safeChars.toCharArray()
        val maxChar = safeCharArray.maxOf { it.code }.coerceAtLeast('z'.code)

        val unreservedChars = BooleanArray(maxChar + 1)

        unreservedChars['-'.code] = true
        unreservedChars['.'.code] = true
        unreservedChars['_'.code] = true
        for (c in '0'..'9') unreservedChars[c.code] = true
        for (c in 'A'..'Z') unreservedChars[c.code] = true
        for (c in 'a'..'z') unreservedChars[c.code] = true

        for (c in safeCharArray) unreservedChars[c.code] = true

        return unreservedChars
    }
}
