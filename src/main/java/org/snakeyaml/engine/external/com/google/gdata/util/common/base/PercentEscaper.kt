/*
 * Copyright (c) 2008 Google Inc.
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
package org.snakeyaml.engine.external.com.google.gdata.util.common.base


/**
 * An [Escaper] that escapes some set of Java characters using the URI percent encoding
 * scheme. The set of safe characters (those which remain unescaped) can be specified on
 * construction.
 *
 * For details on escaping URIs for use in web pages, see section 2.4 of
 * [RFC 3986](http://www.ietf.org/rfc/rfc3986.txt).
 *
 * In most cases this class should not need to be used directly. If you have no special requirements
 * for escaping your URIs, you should use either `CharEscapers#uriEscaper()` or
 * `CharEscapers#uriEscaper(boolean)`.
 *
 * When encoding a String, the following rules apply:
 *
 * 1. The alphanumeric characters "a" through "z", "A" through "Z" and "0" through "9" remain the same.
 * 2. Any additionally specified safe characters remain the same.
 * 3. If [plusForSpace] was specified, the space character ` ` is converted into a plus sign `+`.
 * 4. All other characters are converted into one or more bytes using UTF-8 encoding and each byte
 * is then represented by the 3-character string `%XY`, where `XY` is the two-digit, uppercase,
 * hexadecimal representation of the byte value.
 *
 * RFC 2396 specifies the set of unreserved characters as `-`, `_`, `.`, `!`, `~`, `*`, `'`, `(` and `)`.
 * It goes on to state:
 *
 * > "Unreserved characters can be escaped without changing the semantics of the URI, but this
 * should not be done unless the URI is being used in a context that does not allow the unescaped
 * character to appear."
 *
 * For performance reasons the only currently supported character encoding of this class is UTF-8.
 *
 * **Note**: This escaper produces uppercase hexadecimal sequences. From
 * [RFC 3986](http://www.ietf.org/rfc/rfc3986.txt)
 *
 * > "URI producers and normalizers should use uppercase hexadecimal digits for all percent-encodings."
 *
 * @param safeChars a non-null string specifying additional safe characters for this escaper (the
 * ranges `0..9`, `a..z` and `A..Z` are always safe and should not be specified here)
 * @param plusForSpace true if ASCII space should be escaped to `+` rather than `%20`
 * @throws IllegalArgumentException if any of the parameters were invalid
 */
internal class PercentEscaper(
    safeChars: String,
    /** If `true` we should convert space to the `+` character. */
    private val plusForSpace: Boolean,
) : Escaper {

    /**
     * An array of flags where for any `char c` if `safeOctets.get(c)` is true then `c` should remain
     * unmodified in the output.
     *
     * If `c > safeOctets.length` then it should be escaped.
     */
    private val safeOctets: BooleanArray = createSafeOctets(safeChars)

    init {
        // Avoid any misunderstandings about the behavior of this escaper
        require(!safeChars.matches(".*[0-9A-Za-z].*".toRegex())) {
            "Alphanumeric characters are always 'safe' and should not be explicitly specified"
        }
        // Avoid ambiguous parameters. Safe characters are never modified so if
        // space is a safe character then setting plusForSpace is meaningless.
        require(!(plusForSpace && safeChars.contains(" "))) {
            "plusForSpace cannot be specified when space is a 'safe' character"
        }
        require(!safeChars.contains("%")) {
            "The '%' character cannot be specified as 'safe'"
        }
    }

    /**
     * Scans a sub-sequence of characters from a given [CharSequence], returning the index of
     * the next character that requires escaping.
     *
     * **Note:** When implementing an escaper, it is a good idea to override this method for
     * efficiency. The base class implementation determines successive Unicode code points and invokes
     * [.escape] for each of them. If the semantics of your escaper are such that code
     * points in the supplementary range are either all escaped or all unescaped, this method can be
     * implemented more efficiently using [CharSequence.get].
     *
     * Note however that if your escaper does not escape characters in the supplementary range, you
     * should either continue to validate the correctness of any surrogate characters encountered or
     * provide a clear warning to users that your escaper does not validate its input.
     *
     * See [PercentEscaper] for an example.
     *
     * @param csq a sequence of characters
     * @param start the index of the first character to be scanned
     * @param end the index immediately after the last character to be scanned
     * @throws IllegalArgumentException if the scanned sub-sequence of `csq` contains invalid
     * surrogate pairs
     */
    private fun nextEscapeIndex(csq: CharSequence, start: Int, end: Int): Int {
        var i = start
        while (i < end) {
            val c = csq[i]
            if (c.code >= safeOctets.size || !safeOctets[c.code]) {
                break
            }

            i++
        }
        return i
    }

    /**
     * Returns the escaped form of a given literal string.
     *
     * If you are escaping input in arbitrary successive chunks, then it is not generally safe to use
     * this method. If an input string ends with an unmatched high surrogate character, then this
     * method will throw [IllegalArgumentException]. You should either ensure your input is
     * valid [UTF-16](http://en.wikipedia.org/wiki/UTF-16) before calling this method or
     * use an escaped [Appendable] (as returned by [.escape]) which can cope
     * with arbitrarily split input.
     *
     * @param string the literal string to be escaped
     * @returns the escaped form of `string`
     * @throws NullPointerException if [string] is `null`
     * @throws IllegalArgumentException if invalid surrogate characters are encountered
     */
    override fun escape(string: String): String {
        val length = string.length
        for (index in 0 until length) {
            val c = string[index]
            if (c.code >= safeOctets.size || !safeOctets[c.code]) {
                return escapeSlow(string, index)
            }
        }
        return string
    }

    /**
     * Returns the escaped form of the given Unicode code point, or `null` if this code point
     * does not need to be escaped. When called as part of an escaping operation, the given code point
     * is guaranteed to be in the range `0 <= cp <= Character#MAX_CODE_POINT`.
     *
     * If an empty array is returned, this effectively strips the input character from the resulting
     * text.
     *
     * If the character does not need to be escaped, this method should return `null`, rather
     * than an array containing the character representation of the code point. This enables the
     * escaping algorithm to perform more efficiently.
     *
     * If the implementation of this method cannot correctly handle a particular code point then it
     * should either throw an appropriate runtime exception or return a suitable replacement
     * character. It must never silently discard invalid input as this may constitute a security risk.
     *
     * @param cp the Unicode code point to escape if necessary
     * @return the replacement characters, or `null` if no escaping was needed
     */
    fun escape(cp: Int): CharArray? {
        var codePoint = cp

        // We should never get negative values here but if we do it will throw an
        // IndexOutOfBoundsException, so at least it will get spotted.

        // We should never get negative values here but if we do it will throw an
        // IndexOutOfBoundsException, so at least it will get spotted.
        return if (codePoint < safeOctets.size && safeOctets[codePoint]) {
            null
        } else if (codePoint == ' '.code && plusForSpace) {
            URI_ESCAPED_SPACE
        } else if (codePoint <= 0x7F) {
            // Single byte UTF-8 characters
            // Start with "%--" and fill in the blanks
            val dest = CharArray(3)
            dest[0] = '%'
            dest[2] = UPPER_HEX_DIGITS[codePoint and 0xF]
            dest[1] = UPPER_HEX_DIGITS[codePoint ushr 4]
            dest
        } else if (codePoint <= 0x7ff) {
            // Two byte UTF-8 characters [cp >= 0x80 && cp <= 0x7ff]
            // Start with "%--%--" and fill in the blanks
            val dest = CharArray(6)
            dest[0] = '%'
            dest[3] = '%'
            dest[5] = UPPER_HEX_DIGITS[codePoint and 0xF]
            codePoint = codePoint ushr 4
            dest[4] = UPPER_HEX_DIGITS[0x8 or (codePoint and 0x3)]
            codePoint = codePoint ushr 2
            dest[2] = UPPER_HEX_DIGITS[codePoint and 0xF]
            codePoint = codePoint ushr 4
            dest[1] = UPPER_HEX_DIGITS[0xC or codePoint]
            dest
        } else if (codePoint <= 0xffff) {
            // Three byte UTF-8 characters [cp >= 0x800 && cp <= 0xffff]
            // Start with "%E-%--%--" and fill in the blanks
            val dest = CharArray(9)
            dest[0] = '%'
            dest[1] = 'E'
            dest[3] = '%'
            dest[6] = '%'
            dest[8] = UPPER_HEX_DIGITS[codePoint and 0xF]
            codePoint = codePoint ushr 4
            dest[7] = UPPER_HEX_DIGITS[0x8 or (codePoint and 0x3)]
            codePoint = codePoint ushr 2
            dest[5] = UPPER_HEX_DIGITS[codePoint and 0xF]
            codePoint = codePoint ushr 4
            dest[4] = UPPER_HEX_DIGITS[0x8 or (codePoint and 0x3)]
            codePoint = codePoint ushr 2
            dest[2] = UPPER_HEX_DIGITS[codePoint]
            dest
        } else if (codePoint <= 0x10ffff) {
            val dest = CharArray(12)
            // Four byte UTF-8 characters [cp >= 0xffff && cp <= 0x10ffff]
            // Start with "%F-%--%--%--" and fill in the blanks
            dest[0] = '%'
            dest[1] = 'F'
            dest[3] = '%'
            dest[6] = '%'
            dest[9] = '%'
            dest[11] = UPPER_HEX_DIGITS[codePoint and 0xF]
            codePoint = codePoint ushr 4
            dest[10] = UPPER_HEX_DIGITS[0x8 or (codePoint and 0x3)]
            codePoint = codePoint ushr 2
            dest[8] = UPPER_HEX_DIGITS[codePoint and 0xF]
            codePoint = codePoint ushr 4
            dest[7] = UPPER_HEX_DIGITS[0x8 or (codePoint and 0x3)]
            codePoint = codePoint ushr 2
            dest[5] = UPPER_HEX_DIGITS[codePoint and 0xF]
            codePoint = codePoint ushr 4
            dest[4] = UPPER_HEX_DIGITS[0x8 or (codePoint and 0x3)]
            codePoint = codePoint ushr 2
            dest[2] = UPPER_HEX_DIGITS[codePoint and 0x7]
            dest
        } else {
            // If this ever happens it is due to bug in UnicodeEscaper, not bad input.
            throw IllegalArgumentException("Invalid unicode character value $codePoint")
        }
    }

    /**
     * Returns the escaped form of a given literal string, starting at the given index. This method is
     * called by the [.escape] method when it discovers that escaping is required. It is
     * protected to allow subclasses to override the fast path escaping function to inline their
     * escaping test.
     *
     * This method is not reentrant and may only be invoked by the top level [escape] method.
     *
     * @param s the literal string to be escaped
     * @param startIndex the index to start escaping from
     * @return the escaped form of `string`
     * @throws NullPointerException if `string` is null
     * @throws IllegalArgumentException if invalid surrogate characters are encountered
     */
    private fun escapeSlow(s: String, startIndex: Int): String {
        var index = startIndex
        val end = s.length

        // Get a destination buffer and setup some loop variables.
        var dest = CharArray(1024)
        var destIndex = 0
        var unescapedChunkStart = 0
        while (index < end) {
            val cp = codePointAt(s, index, end)
            require(cp >= 0) { "Trailing high surrogate at end of input" }
            val escaped = escape(cp)
            if (escaped != null) {
                val charsSkipped = index - unescapedChunkStart

                // This is the size needed to add the replacement, not the full
                // size needed by the string. We only regrow when we absolutely must.
                val sizeNeeded = destIndex + charsSkipped + escaped.size
                if (dest.size < sizeNeeded) {
                    val destLength = sizeNeeded + (end - index) + DEST_PAD
                    dest = dest.copyOf(destLength)
                }
                // If we have skipped any characters, we need to copy them now.
                if (charsSkipped > 0) {
                    s.toCharArray(dest, destIndex, unescapedChunkStart, index)
                    destIndex += charsSkipped
                }
                if (escaped.isNotEmpty()) {
                    escaped.copyInto(destination = dest, destinationOffset = destIndex)
                    destIndex += escaped.size
                }
            }
            unescapedChunkStart = index + if (Character.isSupplementaryCodePoint(cp)) 2 else 1
            index = nextEscapeIndex(s, unescapedChunkStart, end)
        }

        // Process trailing unescaped characters - no need to account for
        // escaped length or padding the allocation.
        val charsSkipped = end - unescapedChunkStart
        if (charsSkipped > 0) {
            val endIndex = destIndex + charsSkipped
            if (dest.size < endIndex) {
                dest = dest.copyOf(endIndex)
            }
            s.toCharArray(dest, destIndex, unescapedChunkStart, end)
            destIndex = endIndex
        }
        return String(dest, 0, destIndex)
    }

    companion object {

        /**
         * A string of characters that do not need to be encoded when used in URI path segments, as
         * specified in RFC 3986. Note that some of these characters do need to be escaped when used in
         * other parts of the URI.
         */
        const val SAFEPATHCHARS_URLENCODER = "-_.!~*'()@:$&,;="

        /** In some uri escapers spaces are escaped to '+' */
        private val URI_ESCAPED_SPACE = charArrayOf('+')

        private val UPPER_HEX_DIGITS = "0123456789ABCDEF".toCharArray()

        /**
         * Creates a boolean[] with entries corresponding to the character values for 0-9, A-Z, a-z and
         * those specified in safeChars set to true. The array is as small as is required to hold the
         * given character information.
         */
        private fun createSafeOctets(safeChars: String): BooleanArray {
            val safeCharArray = safeChars.toCharArray()
            val maxChar = safeCharArray.maxOf { it.code }.coerceAtLeast('z'.code)
            val octets = BooleanArray(maxChar + 1)

            for (c in '0'..'9') octets[c.code] = true
            for (c in 'A'..'Z') octets[c.code] = true
            for (c in 'a'..'z') octets[c.code] = true

            for (c in safeCharArray) octets[c.code] = true

            return octets
        }

        /**
         * The amount of padding (chars) to use when growing the escape buffer.
         */
        private const val DEST_PAD = 32

        /**
         * Returns the Unicode code point of the character at the given index.
         *
         * Unlike [Character.codePointAt] or [String.codePointAt] this
         * method will never fail silently when encountering an invalid surrogate pair.
         *
         * The behaviour of this method is as follows:
         *
         *  1. If `index >= end`, [IndexOutOfBoundsException] is thrown.
         *  1. **If the character at the specified index is not a surrogate, it is returned.**
         *  1. If the first character was a high surrogate value, then an attempt is made to read the next
         * character.
         *
         *  1. **If the end of the sequence was reached, the negated value of the trailing high surrogate
         * is returned.**
         *  1. **If the next character was a valid low surrogate, the code point value of the high/low
         * surrogate pair is returned.**
         *  1. If the next character was not a low surrogate value, then [IllegalArgumentException]
         * is thrown.
         *
         *  1. If the first character was a low surrogate value, [IllegalArgumentException] is
         * thrown.
         *
         *
         * @param seq the sequence of characters from which to decode the code point
         * @param start the index of the first character to decode
         * @param end the index beyond the last valid character to decode
         * @return the Unicode code point for the given index or the negated value of the trailing high
         * surrogate character at the end of the sequence
         */
        private fun codePointAt(seq: CharSequence, start: Int, end: Int): Int {
            var index = start
            if (index < end) {
                val c1 = seq[index++]
                return if (c1 < Char.MIN_HIGH_SURROGATE || c1 > Char.MAX_LOW_SURROGATE) {
                    // Fast path (first test is probably all we need to do)
                    c1.code
                } else if (c1 <= Char.MAX_HIGH_SURROGATE) {
                    // If the high surrogate was the last character, return its inverse
                    if (index == end) {
                        return -c1.code
                    }
                    // Otherwise look for the low surrogate following it
                    val c2 = seq[index]
                    if (c2.isLowSurrogate()) {
                        return toCodePoint(c1, c2)
                    }
                    throw IllegalArgumentException("Expected low surrogate but got char '$c2' with value ${c2.code} at index $index")
                } else {
                    throw IllegalArgumentException("Unexpected low surrogate character '$c1' with value ${c1.code} at index ${index - 1}")
                }
            }
            throw IndexOutOfBoundsException("Index exceeds specified range")
        }

        /**
         * The minimum value of a
         * [Unicode supplementary code point](http://www.unicode.org/glossary/#supplementary_code_point),
         * constant `U+10000`.
         */
        private const val MIN_SUPPLEMENTARY_CODE_POINT = 0x010000

        /**
         * The minimum value of a
         * [Unicode high-surrogate code unit](http://www.unicode.org/glossary/#high_surrogate_code_unit)
         * in the UTF-16 encoding, constant `'\u005CuD800'`.
         *
         * A high-surrogate is also known as a *leading-surrogate*.
         */
        private const val MIN_HIGH_SURROGATE = '\uD800'

        /**
         * The minimum value of a
         * [Unicode low-surrogate code unit](http://www.unicode.org/glossary/#low_surrogate_code_unit)
         * in the UTF-16 encoding, constant `'\u005CuDC00'`.
         *
         * A low-surrogate is also known as a *trailing-surrogate*.
         */
        private const val MIN_LOW_SURROGATE = '\uDC00'

        /**
         * Converts the specified surrogate pair to its supplementary code
         * point value. This method does not validate the specified
         * surrogate pair. The caller must validate it using [ ][.isSurrogatePair] if necessary.
         *
         * @param  high the high-surrogate code unit
         * @param  low the low-surrogate code unit
         * @return the supplementary code point composed of the specified surrogate pair.
         */
        private fun toCodePoint(high: Char, low: Char): Int {
            return (high.code shl 10) + low.code +
                (MIN_SUPPLEMENTARY_CODE_POINT - (MIN_HIGH_SURROGATE.code shl 10) - MIN_LOW_SURROGATE.code)
        }
    }
}
