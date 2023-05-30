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
 * An [Escaper] that converts literal text into a format safe for inclusion in a particular
 * context (such as an XML document). Typically (but not always), the inverse process of
 * "unescaping" the text is performed automatically by the relevant scanner.
 *
 * For example, an XML escaper would convert the literal string `"Foo<Bar>"` into
 * `"Foo<Bar>"` to prevent `"<Bar>"` from being confused with an XML tag. When the
 * resulting XML document is parsed, the scanner API will return this text as the original literal
 * string `"Foo<Bar>"`.
 *
 * **Note:** This class is similar to [CharEscaper] but with one very important difference.
 * A CharEscaper can only process Java [UTF-16](http://en.wikipedia.org/wiki/UTF-16)
 * characters in isolation and may not cope when it encounters surrogate pairs. This class
 * facilitates the correct escaping of all Unicode characters.
 *
 * As there are important reasons, including potential security issues, to handle Unicode correctly
 * if you are considering implementing a new escaper you should favor using UnicodeEscaper wherever
 * possible.
 *
 * A `UnicodeEscaper` instance is required to be stateless, and safe when used concurrently by
 * multiple threads.
 *
 * Several popular escapers are defined as constants in the class [CharEscapers]. To create
 * your own escapers extend this class and implement the [escape] method.
 */
abstract class UnicodeEscaper : Escaper {
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
    protected abstract fun escape(cp: Int): CharArray?

    /**
     * Scans a sub-sequence of characters from a given [CharSequence], returning the index of
     * the next character that requires escaping.
     *
     * **Note:** When implementing an escaper, it is a good idea to override this method for
     * efficiency. The base class implementation determines successive Unicode code points and invokes
     * [.escape] for each of them. If the semantics of your escaper are such that code
     * points in the supplementary range are either all escaped or all unescaped, this method can be
     * implemented more efficiently using [CharSequence.charAt].
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
    protected open fun nextEscapeIndex(csq: CharSequence, start: Int, end: Int): Int {
        var index = start
        while (index < end) {
            val cp = codePointAt(csq, index, end)
            if (cp < 0 || escape(cp) != null) {
                break
            }
            index += if (Character.isSupplementaryCodePoint(cp)) 2 else 1
        }
        return index
    }

    /**
     * Returns the escaped form of a given literal string.
     *
     *
     * If you are escaping input in arbitrary successive chunks, then it is not generally safe to use
     * this method. If an input string ends with an unmatched high surrogate character, then this
     * method will throw [IllegalArgumentException]. You should either ensure your input is
     * valid [UTF-16](http://en.wikipedia.org/wiki/UTF-16) before calling this method or
     * use an escaped [Appendable] (as returned by [.escape]) which can cope
     * with arbitrarily split input.
     *
     *
     * **Note:** When implementing an escaper it is a good idea to override this method for
     * efficiency by inlining the implementation of [.nextEscapeIndex]
     * directly. Doing this for [PercentEscaper] more than doubled the performance for unescaped
     * strings (as measured by [CharEscapersBenchmark]).
     *
     * @param string the literal string to be escaped
     * @return the escaped form of `string`
     * @throws NullPointerException if `string` is null
     * @throws IllegalArgumentException if invalid surrogate characters are encountered
     */
    override fun escape(string: String): String {
        val end = string.length
        val index = nextEscapeIndex(string, 0, end)
        return if (index == end) string else escapeSlow(string, index)
    }

    /**
     * Returns the escaped form of a given literal string, starting at the given index. This method is
     * called by the [.escape] method when it discovers that escaping is required. It is
     * protected to allow subclasses to override the fastpath escaping function to inline their
     * escaping test. See [CharEscaperBuilder] for an example usage.
     *
     * This method is not reentrant and may only be invoked by the top level [.escape]
     * method.
     *
     * @param s the literal string to be escaped
     * @param index the index to start escaping from
     * @return the escaped form of `string`
     * @throws NullPointerException if `string` is null
     * @throws IllegalArgumentException if invalid surrogate characters are encountered
     */
    protected fun escapeSlow(s: String, index: Int): String {
        var index = index
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
                    dest = growBuffer(dest, destIndex, destLength)
//                    dest = dest.copyOf(sizeNeeded) //growBuffer(dest, destIndex, destLength)
                }
                // If we have skipped any characters, we need to copy them now.
                if (charsSkipped > 0) {
                    s.toCharArray(dest, destIndex, unescapedChunkStart, index)
                    destIndex += charsSkipped
                }
                if (escaped.isNotEmpty()) {
                    System.arraycopy(escaped, 0, dest, destIndex, escaped.size)
//                    dest.copyInto(escaped)
                    destIndex += escaped.size
                }
            }
            unescapedChunkStart = index + if (Character.isSupplementaryCodePoint(cp)) 2 else 1
            index = nextEscapeIndex(s, unescapedChunkStart, end)
        }

        // Process trailing unescaped characters - no need to account for
        // escaped
        // length or padding the allocation.
        val charsSkipped = end - unescapedChunkStart
        if (charsSkipped > 0) {
            val endIndex = destIndex + charsSkipped
            if (dest.size < endIndex) {
                dest = growBuffer(dest, destIndex, endIndex)
            }
            s.toCharArray(dest, destIndex, unescapedChunkStart, end)
            destIndex = endIndex
        }
        return String(dest, 0, destIndex)
    }

    companion object {
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
         * @param index the index of the first character to decode
         * @param end the index beyond the last valid character to decode
         * @return the Unicode code point for the given index or the negated value of the trailing high
         * surrogate character at the end of the sequence
         */
        protected fun codePointAt(seq: CharSequence, index: Int, end: Int): Int {
            var index = index
            if (index < end) {
                val c1 = seq[index++]
                return if (c1 < Char.MIN_HIGH_SURROGATE || c1 > Char.MAX_LOW_SURROGATE) {
                    // Fast path (first test is probably all we need to do)
                    c1.code
                } else if (c1 <= Char.MAX_HIGH_SURROGATE) {
                    // If the high surrogate was the last character, return its
                    // inverse
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
         * Helper method to grow the character buffer as needed, this only happens once in a while so it's
         * ok if it's in a method call. If the index passed in is 0 then no copying will be done.
         */
        private fun growBuffer(dest: CharArray, index: Int, size: Int): CharArray {
            val copy = CharArray(size)
            if (index > 0) {
                dest.copyOf(size)

                System.arraycopy(dest, 0, copy, 0, index)
            }
            return copy
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
