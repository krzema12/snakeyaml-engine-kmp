package org.snakeyaml.engine.v2.scanner

import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.comments.CommentType
import org.snakeyaml.engine.v2.common.CharConstants
import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.exceptions.Mark
import org.snakeyaml.engine.v2.tokens.CommentToken
import org.snakeyaml.engine.v2.tokens.Token
import java.util.Optional
import java.util.regex.Pattern


/**
 * Scanner produces tokens of the following types:
 *
 * * `STREAM-START`
 * * `STREAM-END`
 * * `COMMENT`
 * * `DIRECTIVE(name, value)`
 * * `DOCUMENT-START`
 * * `DOCUMENT-END`
 * * `BLOCK-SEQUENCE-START`
 * * `BLOCK-MAPPING-START`
 * * `BLOCK-END`
 * * `FLOW-SEQUENCE-START`
 * * `FLOW-MAPPING-START`
 * * `FLOW-SEQUENCE-END`
 * * `FLOW-MAPPING-END`
 * * `BLOCK-ENTRY`
 * * `FLOW-ENTRY`
 * * `KEY`
 * * `VALUE`
 * * `ALIAS(value)`
 * * `ANCHOR(value)`
 * * `TAG(value)`
 * * `SCALAR(value, plain, style)`
 *
 * Read comments in the [Scanner] code for more details.
 */
class ScannerImpl(
    private val settings: LoadSettings,
    private val reader: StreamReader,
) : Scanner {
    private val scannerJava = ScannerImplJava(settings, reader)

    /** List of processed tokens that are not yet emitted. */
    // maybe make this an ArrayDeque?
    private val tokens: MutableList<Token> by scannerJava::tokens
//    = ArrayList<Token>(100)

    /** Past indentation levels. */
    private val indents by scannerJava::indents
    //= ArrayDeque<Int>(10)

    /**
     * Keep track of possible simple keys. This is a dictionary. The key is `flow_level`; there can be
     * no more than one possible simple key for each level.
     *
     * The value is a [SimpleKey] record.
     *
     * A simple key may start with `ALIAS`, `ANCHOR`, `TAG`, `SCALAR(flow)`, `[`, or `{` tokens.
     *
     *  The order in [possibleSimpleKeys] is kept for [nextPossibleSimpleKey]
     */
    private val possibleSimpleKeys by scannerJava::possibleSimpleKeys
//    = LinkedHashMap<Int, SimpleKey>()

    /** Had we reached the end of the stream */
    private var done by scannerJava::done  //= false

    /**
     * The number of unclosed `{` and `[`. [isBlockContext] means block context.
     */
    private var flowLevel by scannerJava::flowLevel // = 0

    /**
     * The last added token
     */
    private var lastToken by scannerJava::lastToken //: Token = null

    /**
     * Variables related to simple keys treatment.
     * Number of tokens that were emitted through the [checkToken] method.
     */
    private var tokensTaken by scannerJava::tokensTaken // = 0

    /** The current indentation level. */
    private var indent by scannerJava::indent // = -1

    /**
     * ```
     * A simple key is a key that is not denoted by the '' indicator.
     * Example of simple keys:
     * ---
     * block simple key: value
     *  not a simple key:
     * : { flow simple key: value }
     * We emit the KEY token before all keys, so when we find a potential
     * simple key, we try to locate the corresponding ':' indicator.
     * Simple keys should be limited to a single line and 1024 characters.
     *
     * Can a simple key start at the current position A simple key may
     * start:
     * - at the beginning of the line, not counting indentation spaces
     * (in block context),
     * - after '{', '[', ',' (in the flow context),
     * - after '', ':', '-' (in the block context).
     * In the block context, this flag also signifies if a block collection
     * may start at the current position.
     * ```
     */
    private var allowSimpleKey by scannerJava::allowSimpleKey // = true

    init {
        fetchStreamStart() // Add the STREAM-START token.
    }

    override fun hasNext(): Boolean = checkToken()

    /** Return the next token, removing it from the queue. */
    override fun checkToken(vararg choices: Token.ID): Boolean {
        while (needMoreTokens()) {
            fetchMoreTokens()
        }
        if (tokens.isNotEmpty()) {
            if (choices.isEmpty()) {
                return true
            }
            // since profiler puts this method on top (it is used a lot), we
            // should not use 'foreach' here because of the performance reasons
            val firstToken = tokens[0]
            val first: Token.ID = firstToken.tokenId
            for (choice in choices) {
                if (first == choice) {
                    return true
                }
            }
        }
        return false
    }

    /** Return the next token, but do not delete it from the queue. */
    override fun peekToken(): Token {
        while (needMoreTokens()) {
            fetchMoreTokens()
        }
        return tokens[0]
    }

    /** Return the next token, removing it from the queue. */
    override fun next(): Token {
        tokensTaken++
        return tokens.removeFirstOrNull() ?: throw NoSuchElementException("No more Tokens found.")
    }

    override fun resetDocumentIndex(): Unit = reader.resetDocumentIndex()

    //@formatter:off
    //region Private methods.

    private fun addToken(token: Token): Unit = scannerJava.addToken(token)
    private fun addToken(index: Int, token: Token): Unit = scannerJava.addToken(index, token)
    private fun addAllTokens(tokens: List<Token>): Unit = scannerJava.addAllTokens(tokens)
    private fun isBlockContext(): Boolean = scannerJava.isBlockContext
    private fun isFlowContext(): Boolean = scannerJava.isFlowContext
    private fun needMoreTokens(): Boolean = scannerJava.needMoreTokens()
    private fun fetchMoreTokens(): Unit = scannerJava.fetchMoreTokens()

    //region Simple keys treatment.
    private fun nextPossibleSimpleKey(): Int = scannerJava.nextPossibleSimpleKey()
    private fun stalePossibleSimpleKeys(): Unit = scannerJava.stalePossibleSimpleKeys()
    private fun savePossibleSimpleKey(): Unit = scannerJava.savePossibleSimpleKey()
    private fun removePossibleSimpleKey(): Unit = scannerJava.removePossibleSimpleKey()
    //endregion

    //region Indentation functions.
    private fun unwindIndent(col: Int): Unit = scannerJava.unwindIndent(col)
    private fun addIndent(column: Int): Boolean = scannerJava.addIndent(column)
    //endregion

    //region Fetchers - add tokens to the stream (can call scanners)
    private fun fetchStreamStart(): Unit = scannerJava.fetchStreamStart()
    private fun fetchStreamEnd(): Unit = scannerJava.fetchStreamEnd()
    private fun fetchDirective(): Unit = scannerJava.fetchDirective()
    private fun fetchDocumentStart(): Unit = scannerJava.fetchDocumentStart()
    private fun fetchDocumentEnd(): Unit = scannerJava.fetchDocumentEnd()
    private fun fetchDocumentIndicator(isDocumentStart: Boolean): Unit = scannerJava.fetchDocumentIndicator(isDocumentStart)
    private fun fetchFlowSequenceStart(): Unit = scannerJava.fetchFlowSequenceStart()
    private fun fetchFlowMappingStart(): Unit = scannerJava.fetchFlowMappingStart()
    private fun fetchFlowCollectionStart(isMappingStart: Boolean): Unit = scannerJava.fetchFlowCollectionStart(isMappingStart)
    private fun fetchFlowSequenceEnd(): Unit = scannerJava.fetchFlowSequenceEnd()
    private fun fetchFlowMappingEnd(): Unit = scannerJava.fetchFlowMappingEnd()
    private fun fetchFlowCollectionEnd(isMappingEnd: Boolean): Unit = scannerJava.fetchFlowCollectionEnd(isMappingEnd)
    private fun fetchFlowEntry(): Unit = scannerJava.fetchFlowEntry()
    private fun fetchBlockEntry(): Unit = scannerJava.fetchBlockEntry()
    private fun fetchKey(): Unit = scannerJava.fetchKey()
    private fun fetchValue(): Unit = scannerJava.fetchValue()
    private fun fetchAlias(): Unit = scannerJava.fetchAlias()
    private fun fetchAnchor(): Unit = scannerJava.fetchAnchor()
    private fun fetchTag(): Unit = scannerJava.fetchTag()
    private fun fetchLiteral(): Unit = scannerJava.fetchLiteral()
    private fun fetchFolded(): Unit = scannerJava.fetchFolded()
    private fun fetchBlockScalar(style: ScalarStyle): Unit = scannerJava.fetchBlockScalar(style)
    private fun fetchSingle(): Unit = scannerJava.fetchSingle()
    private fun fetchDouble(): Unit = scannerJava.fetchDouble()
    private fun fetchFlowScalar(style: ScalarStyle): Unit = scannerJava.fetchFlowScalar(style)
    private fun fetchPlain(): Unit = scannerJava.fetchPlain()
    //endregion

    //region Checkers.
    private fun checkDirective(): Boolean = scannerJava.checkDirective()
    private fun checkDocumentStart(): Boolean = scannerJava.checkDocumentStart()
    private fun checkDocumentEnd(): Boolean = scannerJava.checkDocumentEnd()
    private fun checkBlockEntry(): Boolean = scannerJava.checkBlockEntry()
    private fun checkKey(): Boolean = scannerJava.checkKey()
    private fun checkValue(): Boolean = scannerJava.checkValue()
    private fun checkPlain(): Boolean = scannerJava.checkPlain()
    //endregion

    //region Scanners - create tokens
    private fun scanToNextToken(): Unit = scannerJava.scanToNextToken()
    private fun scanComment(type: CommentType): CommentToken = scannerJava.scanComment(type)
    private fun scanDirective(): List<Token> = scannerJava.scanDirective()
    private fun scanDirectiveName(startMark: Optional<Mark>): String = scannerJava.scanDirectiveName(startMark)
    private fun scanYamlDirectiveValue(startMark: Optional<Mark>): List<Int> = scannerJava.scanYamlDirectiveValue(startMark)
    private fun scanYamlDirectiveNumber(startMark: Optional<Mark>): Int = scannerJava.scanYamlDirectiveNumber(startMark)
    private fun scanTagDirectiveValue(startMark: Optional<Mark>): List<String> = scannerJava.scanTagDirectiveValue(startMark)
    private fun scanTagDirectiveHandle(startMark: Optional<Mark>): String = scannerJava.scanTagDirectiveHandle(startMark)
    private fun scanTagDirectivePrefix(startMark: Optional<Mark>): String = scannerJava.scanTagDirectivePrefix(startMark)
    private fun scanDirectiveIgnoredLine(startMark: Optional<Mark>): CommentToken = scannerJava.scanDirectiveIgnoredLine(startMark)
    private fun scanAnchor(isAnchor: Boolean): Token = scannerJava.scanAnchor(isAnchor)
    private fun scanTag(): Token = scannerJava.scanTag()
    private fun scanBlockScalar(style: ScalarStyle): List<Token> = scannerJava.scanBlockScalar(style)
    private fun scanBlockScalarIndicators(startMark: Optional<Mark>): Chomping = scannerJava.scanBlockScalarIndicators(startMark)
    private fun scanBlockScalarIgnoredLine(startMark: Optional<Mark>): CommentToken = scannerJava.scanBlockScalarIgnoredLine(startMark)
    private fun scanBlockScalarIndentation(): BreakIntentHolder = scannerJava.scanBlockScalarIndentation()
    private fun scanBlockScalarBreaks(indent: Int): BreakIntentHolder = scannerJava.scanBlockScalarBreaks(indent)
    private fun scanFlowScalar(style: ScalarStyle): Token = scannerJava.scanFlowScalar(style)
    private fun scanFlowScalarNonSpaces(doubleQuoted: Boolean, startMark: Optional<Mark>): String = scannerJava.scanFlowScalarNonSpaces(doubleQuoted ,startMark)
    private fun scanFlowScalarSpaces(startMark: Optional<Mark>): String = scannerJava.scanFlowScalarSpaces(startMark)
    private fun scanFlowScalarBreaks(startMark: Optional<Mark>): String = scannerJava.scanFlowScalarBreaks(startMark)
    private fun scanPlain(): Token = scannerJava.scanPlain()
    private fun atEndOfPlain(): Boolean = scannerJava.atEndOfPlain()
    private fun scanPlainSpaces(): String = scannerJava.scanPlainSpaces()
    private fun scanTagHandle(name: String, startMark: Optional<Mark>): String = scannerJava.scanTagHandle(name, startMark)
    private fun scanTagUri(name: String, range: CharConstants, startMark: Optional<Mark>): String = scannerJava.scanTagUri(name, range, startMark)
    private fun scanUriEscapes(name: String, startMark: Optional<Mark>): String = scannerJava.scanUriEscapes(name, startMark)
    private fun scanLineBreak(): Optional<String> = scannerJava.scanLineBreak()

    //endregion

    private fun makeTokenList(vararg tokens: Token): List<Token> = scannerJava.makeTokenList()
    //@formatter:on

    companion object {

        private const val DIRECTIVE_PREFIX = "while scanning a directive"
        private const val EXPECTED_ALPHA_ERROR_PREFIX = "expected alphabetic or numeric character, but found "
        private const val SCANNING_SCALAR = "while scanning a block scalar"
        private const val SCANNING_PREFIX = "while scanning a "

        /**
         * A regular expression matching characters which are not in the hexadecimal set (`0-9`, `A-F`, `a-f`).
         */
        private val NOT_HEXA = Pattern.compile("[^0-9A-Fa-f]")
    }
}
