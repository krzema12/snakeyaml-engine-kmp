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
package it.krzeminski.snakeyaml.engine.kmp.usecases.inherited

import it.krzeminski.snakeyaml.engine.kmp.common.Anchor
import it.krzeminski.snakeyaml.engine.kmp.exceptions.Mark
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag
import it.krzeminski.snakeyaml.engine.kmp.scanner.Scanner
import it.krzeminski.snakeyaml.engine.kmp.tokens.*
import org.snakeyaml.engine.usecases.inherited.CanonicalException

class CanonicalScanner(data: String, private val label: String) : Scanner {
    private val data = data + "\u0000"
    private val mark = Mark(
        name = "test",
        index = 0,
        line = 0,
        column = 0,
        codepoints =  data.map { it.code },
        pointer = 0,
    )
    @JvmField
    var tokens: MutableList<Token> = mutableListOf()
    private var index = 0
    private var scanned = false

    override fun checkToken(vararg choices: Token.ID): Boolean {
        if (!scanned) {
            scan()
        }
        if (tokens.isNotEmpty()) {
            if (choices.isEmpty()) {
                return true
            }
            val first = tokens[0]
            for (choice in choices) {
                if (first.tokenId == choice) {
                    return true
                }
            }
        }
        return false
    }

    override fun peekToken(): Token {
        if (!scanned) {
            scan()
        }
        return tokens.stream().findFirst().orElseThrow {
            NoSuchElementException(
                "No value present"
            )
        }
    }

    override fun hasNext(): Boolean {
        return checkToken()
    }

    override fun next(): Token {
        if (!scanned) {
            scan()
        }
        return tokens.removeAt(0)
    }

    override fun resetDocumentIndex() {
        this.index = 0
    }

    fun getToken(choice: Token.ID): Token {
        val token = next()
        if (token.tokenId != choice) {
            throw CanonicalException("unexpected token $token")
        }
        return token
    }

    private fun scan() {
        tokens.add(StreamStartToken(mark, mark))
        var stop = false
        while (!stop) {
            findToken()
            when (val c = data[index]) {
                '\u0000' -> {
                    tokens.add(StreamEndToken(mark, mark))
                    stop = true
                }
                '%' -> tokens.add(scanDirective())
                '-' -> if ("---" == data.substring(index, index + 3)) {
                    index += 3
                    tokens.add(DocumentStartToken(mark, mark))
                }
                '.' -> if ("..." == data.substring(index, index + 3)) {
                    index += 3
                    tokens.add(DocumentEndToken(mark, mark))
                }
                '[' -> {
                    index++
                    tokens.add(FlowSequenceStartToken(mark, mark))
                }
                '{' -> {
                    index++
                    tokens.add(FlowMappingStartToken(mark, mark))
                }
                ']' -> {
                    index++
                    tokens.add(FlowSequenceEndToken(mark, mark))
                }
                '}' -> {
                    index++
                    tokens.add(FlowMappingEndToken(mark, mark))
                }
                '?' -> {
                    index++
                    tokens.add(KeyToken(mark, mark))
                }
                ':' -> {
                    index++
                    tokens.add(ValueToken(mark, mark))
                }
                ',' -> {
                    index++
                    tokens.add(FlowEntryToken(mark, mark))
                }
                '*' -> tokens.add(scanAlias())
                '&' -> tokens.add(scanAlias())
                '!' -> tokens.add(scanTag())
                '"' -> tokens.add(scanScalar())
                else -> throw CanonicalException("invalid token: $c in $label")
            }
        }
        scanned = true
    }

    private fun scanDirective(): Token {
        val chunk1 = data.substring(index, index + DIRECTIVE.length)
        val chunk2 = data[index + DIRECTIVE.length]
        if (DIRECTIVE == chunk1 && "\n\u0000".indexOf(chunk2) != -1) {
            index += DIRECTIVE.length
            val implicit = DirectiveToken.YamlDirective(1, 1)
            return DirectiveToken(implicit, mark, mark)
        } else {
            throw CanonicalException("invalid directive: $chunk1 $chunk2 in $label")
        }
    }

    private fun scanAlias(): Token {
        val isTokenClassAlias: Boolean
        val c = data.codePointAt(index)
        isTokenClassAlias = c == '*'.code
        index += Character.charCount(c)
        val start = index
        while (", \n\u0000".indexOf(data[index]) == -1) {
            index++
        }
        val value = data.substring(start, index)
        val token = if (isTokenClassAlias) {
            AliasToken(
                Anchor(value),
                mark,
                mark
            )
        } else {
            AnchorToken(
                Anchor(value),
                mark,
                mark
            )
        }
        return token
    }

    private fun scanTag(): Token {
        index += Character.charCount(data.codePointAt(index))
        val start = index
        while (" \n\u0000".indexOf(data[index]) == -1) {
            index++
        }
        var value = data.substring(start, index)
        value = if (value.isEmpty()) {
            "!"
        } else if (value[0] == '!') {
            Tag.PREFIX + value.substring(1)
        } else if (value[0] == '<' && value[value.length - 1] == '>') {
            value.substring(1, value.length - 1)
        } else {
            "!$value"
        }
        return TagToken(TagTuple("", value), mark, mark)
    }

    private fun scanScalar(): Token {
        index += Character.charCount(data.codePointAt(index))
        val chunks = StringBuilder()
        var start = index
        var ignoreSpaces = false
        while (data[index] != '"') {
            if (data[index] == '\\') {
                ignoreSpaces = false
                chunks.append(data, start, index)
                index += Character.charCount(data.codePointAt(index))
                val c = data.codePointAt(index)
                index += Character.charCount(data.codePointAt(index))
                if (c == '\n'.code) {
                    ignoreSpaces = true
                } else if (!Character.isSupplementaryCodePoint(c) && ESCAPE_CODES.containsKey(c.toChar())) {
                    val length = ESCAPE_CODES[c.toChar()]!!
                    val code = data.substring(index, index + length).toInt(16)
                    chunks.append(code.toChar())
                    index += length
                } else {
                    if (Character.isSupplementaryCodePoint(c) || !ESCAPE_REPLACEMENTS.containsKey(c.toChar())) {
                        throw CanonicalException("invalid escape code")
                    }
                    chunks.append(ESCAPE_REPLACEMENTS[c.toChar()])
                }
                start = index
            } else if (data[index] == '\n') {
                chunks.append(data, start, index)
                chunks.append(" ")
                index += Character.charCount(data.codePointAt(index))
                start = index
                ignoreSpaces = true
            } else if (ignoreSpaces && data[index] == ' ') {
                index += Character.charCount(data.codePointAt(index))
                start = index
            } else {
                ignoreSpaces = false
                index += Character.charCount(data.codePointAt(index))
            }
        }
        chunks.append(data, start, index)
        index += Character.charCount(data.codePointAt(index))
        return ScalarToken(chunks.toString(), false, mark, mark)
    }

    private fun findToken() {
        var found = false
        while (!found) {
            while (" \t".indexOf(data[index]) != -1) {
                index++
            }
            if (data[index] == '#') {
                while (data[index] != '\n') {
                    index++
                }
            }
            if (data[index] == '\n') {
                index++
            } else {
                found = true
            }
        }
    }

    companion object {
        private val ESCAPE_REPLACEMENTS = mapOf(
            // ASCII null
            '0' to "\u0000",
            // ASCII bell
            'a' to "\u0007",
            // ASCII backspace
            'b' to "\u0008",
            // ASCII horizontal tab
            't' to "\t",
            // ASCII newline (line feed; &#92;n maps to 0x0A)
            'n' to "\n",
            // ASCII vertical tab
            'v' to "\u000B",
            // ASCII form-feed
            'f' to "\u000C",
            // carriage-return (&#92;r maps to 0x0D)
            'r' to "\r",
            // ASCII escape character (Esc)
            'e' to "\u001B",
            // ASCII space
            ' ' to " ",
            // ASCII double-quote
            '"' to "\"",
            // ASCII backslash
            '\\' to "\\",
            // Unicode next line
            'N' to "\u0085",
            // Unicode non-breaking-space
            '_' to "\u00A0",
        )
        private const val DIRECTIVE = "%YAML 1.2"
        private val ESCAPE_CODES = mapOf(
            // 8-bit Unicode
            'x' to 2,
            // 16-bit Unicode
            'u' to 4,
            // 32-bit Unicode (Supplementary characters are supported)
            'U' to 8,
        )
    }
}
