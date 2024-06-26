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
package it.krzeminski.snakeyaml.engine.kmp.common

import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

class CharConstants private constructor(content: String) {
    val contains: BooleanArray

    init {
        val contentCodes = content.map { it.code }
        contains = BooleanArray(ASCII_SIZE) { it in contentCodes }
    }

    fun has(c: Int): Boolean = c < ASCII_SIZE && contains[c]

    fun hasNo(c: Int): Boolean = !has(c)

    fun has(c: Int, additional: String): Boolean = has(c) || c.toChar() in additional

    fun hasNo(c: Int, additional: String): Boolean = !has(c, additional)

    companion object {
        private const val ALPHA_S =
            "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-_"
        private const val LINEBR_S = "\n"
        private const val FULL_LINEBR_S = "\r" + LINEBR_S
        private const val NULL_OR_LINEBR_S = "\u0000" + FULL_LINEBR_S
        private const val NULL_BL_LINEBR_S = " $NULL_OR_LINEBR_S"
        private const val NULL_BL_T_LINEBR_S = "\t" + NULL_BL_LINEBR_S
        private const val NULL_BL_T_S = "\u0000 \t"

        /**
         * The suffix must not contain the characters
         *
         * ```text
         * []{},.
         * ```
         *
         * These characters would cause ambiguity with flow collection structures.
         * https://yaml.org/spec/1.2.2/#691-node-tags
         */
        private const val URI_CHARS_SUFFIX_S = "$ALPHA_S-;/?:@&=+\$_.!~*'()%"

        @JvmField
        val LINEBR = CharConstants(LINEBR_S)

        @JvmField
        val NULL_OR_LINEBR = CharConstants(NULL_OR_LINEBR_S)

        @JvmField
        val NULL_BL_LINEBR = CharConstants(NULL_BL_LINEBR_S)

        @JvmField
        val NULL_BL_T_LINEBR = CharConstants(NULL_BL_T_LINEBR_S)

        @JvmField
        val NULL_BL_T = CharConstants(NULL_BL_T_S)

        // prefix may contain ,[]
        @JvmField
        val URI_CHARS_FOR_TAG_PREFIX = CharConstants("$URI_CHARS_SUFFIX_S,[]")

        @JvmField
        val URI_CHARS_FOR_TAG_SUFFIX = CharConstants(URI_CHARS_SUFFIX_S)

        @JvmField
        val ALPHA = CharConstants(ALPHA_S)

        private const val ASCII_SIZE = 128

        /**
         * A mapping from an escaped character in the input stream to the character that they should be replaced with.
         *
         * YAML defines several common and a few uncommon escape sequences.
         */
        @JvmField
        val ESCAPE_REPLACEMENTS: Map<Char, String> = mapOf(
            '0' to "\u0000", // ASCII null
            'a' to "\u0007", // ASCII bell
            'b' to "\u0008", // ASCII backspace
            't' to "\u0009", // ASCII horizontal tab
            'n' to "\n",     // ASCII newline (line feed; \n maps to 0x0A)
            'v' to "\u000B", // ASCII vertical tab
            'f' to "\u000C", // ASCII form-feed
            'r' to "\r",     // carriage-return (\r maps to 0x0D)
            'e' to "\u001B", // ASCII escape character (Esc)
            ' ' to "\u0020", // ASCII space
            '"' to "\"",     // ASCII double-quote
            '/' to "/",      // ASCII slash, for JSON compatibility.
            '\\' to "\\",    // ASCII backslash
            'N' to "\u0085", // Unicode next line
            '_' to "\u00A0", // Unicode non-breaking-space
        )

        /** The reverse of [ESCAPE_REPLACEMENTS]: Map characters to the escaped replacement. */
        private val escapedReplacements: Map<String, Char> =
            ESCAPE_REPLACEMENTS.entries.associate { (k, v) -> v to k }

        /**
         * A mapping from a character to a number of bytes to read-ahead for that escape sequence. These
         * escape sequences are used to handle unicode escaping in the following formats, where `H` is a
         * hexadecimal character:
         *
         * * `\xHH`         : escaped 8-bit Unicode character
         * * `\uHHHH`       : escaped 16-bit Unicode character
         * * `\UHHHHHHHH`   : escaped 32-bit Unicode character
         */
        @JvmField
        val ESCAPE_CODES: Map<Char, Int> = mapOf(
            'x' to 2, // 8-bit Unicode
            'u' to 4, // 16-bit Unicode
            'U' to 8, // 32-bit Unicode (Supplementary characters are supported)
        )

        /**
         * Replace a single character with a readable [String] representation
         *
         * @param char - the char to escape
         * @return the same string or its escaped representation
         */
        @JvmStatic
        fun escapeChar(char: Char): String {
            val charString = char.toString()

            // YAML requires these chars are escaped, but here we're just escaping for readability,
            // so don't escape these chars:
            if (char in " /\"") return charString

            val escaped = escapedReplacements[charString] ?: return charString
            return "\\${escaped}"
        }
    }
}
