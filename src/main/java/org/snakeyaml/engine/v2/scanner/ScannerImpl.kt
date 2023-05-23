package org.snakeyaml.engine.v2.scanner

import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.comments.CommentType
import org.snakeyaml.engine.v2.common.Anchor
import org.snakeyaml.engine.v2.common.CharConstants
import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.exceptions.Mark
import org.snakeyaml.engine.v2.exceptions.ScannerException
import org.snakeyaml.engine.v2.exceptions.YamlEngineException
import org.snakeyaml.engine.v2.tokens.AliasToken
import org.snakeyaml.engine.v2.tokens.AnchorToken
import org.snakeyaml.engine.v2.tokens.BlockEndToken
import org.snakeyaml.engine.v2.tokens.BlockEntryToken
import org.snakeyaml.engine.v2.tokens.BlockMappingStartToken
import org.snakeyaml.engine.v2.tokens.BlockSequenceStartToken
import org.snakeyaml.engine.v2.tokens.CommentToken
import org.snakeyaml.engine.v2.tokens.DocumentEndToken
import org.snakeyaml.engine.v2.tokens.DocumentStartToken
import org.snakeyaml.engine.v2.tokens.FlowEntryToken
import org.snakeyaml.engine.v2.tokens.FlowMappingEndToken
import org.snakeyaml.engine.v2.tokens.FlowMappingStartToken
import org.snakeyaml.engine.v2.tokens.FlowSequenceEndToken
import org.snakeyaml.engine.v2.tokens.FlowSequenceStartToken
import org.snakeyaml.engine.v2.tokens.KeyToken
import org.snakeyaml.engine.v2.tokens.ScalarToken
import org.snakeyaml.engine.v2.tokens.StreamEndToken
import org.snakeyaml.engine.v2.tokens.StreamStartToken
import org.snakeyaml.engine.v2.tokens.TagToken
import org.snakeyaml.engine.v2.tokens.TagTuple
import org.snakeyaml.engine.v2.tokens.Token
import org.snakeyaml.engine.v2.tokens.ValueToken
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
     * Keep track of possible simple keys. This is a dictionary. Insertion order _must_ be preserved.
     *
     * The key is `flow_level`; there can be no more than one possible simple key for each level.
     *
     * The value is a [SimpleKey] record.
     *
     * A simple key may start with `ALIAS`, `ANCHOR`, `TAG`, `SCALAR(flow)`, `[`, or `{` tokens.
     *
     *  The order in [possibleSimpleKeys] is kept for [nextPossibleSimpleKey]
     */
    private val possibleSimpleKeys: MutableMap<Int, SimpleKey> by scannerJava::possibleSimpleKeys
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

    /** Check whether the next token is one of the given types. */
    override fun checkToken(vararg choices: Token.ID): Boolean {
        while (needMoreTokens()) {
            fetchMoreTokens()
        }
        val firstTokenId = tokens.firstOrNull()?.tokenId ?: return false
        return choices.isEmpty() || choices.any { choice -> firstTokenId == choice }
    }

    /** Return the next token, but do not delete it from the queue. */
    override fun peekToken(): Token {
        while (needMoreTokens()) {
            fetchMoreTokens()
        }
        return tokens[0]
    }

    override fun hasNext(): Boolean = checkToken()

    /** Return the next token, removing it from the queue. */
    override fun next(): Token {
        tokensTaken++
        return tokens.removeFirstOrNull() ?: throw NoSuchElementException("No more Tokens found.")
    }

    override fun resetDocumentIndex(): Unit = reader.resetDocumentIndex()

    //region Private methods.

    private fun addToken(token: Token) {
        lastToken = token
        tokens.add(token)
    }

    private fun addToken(index: Int, token: Token) {
        if (index == tokens.size) {
            lastToken = token
        }
        tokens.add(index, token)
    }

    private fun addAllTokens(tokens: List<Token>) {
        lastToken = tokens.last()
        this.tokens.addAll(tokens)
    }

    private fun isBlockContext(): Boolean = flowLevel == 0

    private fun isFlowContext(): Boolean = !isBlockContext()

    /** Returns `true` if more tokens should be scanned. */
    private fun needMoreTokens(): Boolean {
        // If we are done, we do not require more tokens.
        if (done) return false
        // If we aren't done, but we have no tokens, we need to scan more.
        if (tokens.isEmpty()) return true
        // The current token may be a potential simple key, so we
        // need to look further.
        stalePossibleSimpleKeys()
        return nextPossibleSimpleKey() == tokensTaken
    }

    /** Fetch one or more tokens from the StreamReader. */
    private fun fetchMoreTokens() {
        if (reader.documentIndex > settings.codePointLimit) {
            throw YamlEngineException("The incoming YAML document exceeds the limit: ${settings.codePointLimit} code points.")
        }
        // Eat whitespaces and process comments until we reach the next token.
        scanToNextToken()
        // Remove obsolete possible simple keys.
        stalePossibleSimpleKeys()
        // Compare the current indentation and column. It may add some tokens and decrease the current indentation level.
        unwindIndent(reader.column)
        // Peek the next code point, to decide what the next group of tokens will look like.
        val c = reader.peek()

        // Is it the end of stream?
        if (c == 0) {
            fetchStreamEnd()
            return
        }
        when (c.toChar()) {
            // Is it a directive?
            '%'  ->
                if (checkDirective()) {
                    fetchDirective()
                    return
                }

            // Is it the document start?
            '-'  ->
                if (checkDocumentStart()) {
                    fetchDocumentStart()
                    return
                    // Is it the block entry indicator?
                } else if (checkBlockEntry()) {
                    fetchBlockEntry()
                    return
                }

            // Is it the document end?
            '.'  ->
                if (checkDocumentEnd()) {
                    fetchDocumentEnd()
                    return
                }

            // Is it the flow sequence start indicator?
            '['  -> {
                fetchFlowSequenceStart()
                return
            }

            // Is it the flow mapping start indicator?
            '{'  -> {
                fetchFlowMappingStart()
                return
            }

            // Is it the flow sequence end indicator?
            ']'  -> {
                fetchFlowSequenceEnd()
                return
            }

            // Is it the flow mapping end indicator?
            '}'  -> {
                fetchFlowMappingEnd()
                return
            }

            // Is it the flow entry indicator?
            ','  -> {
                fetchFlowEntry()
                return
            }

            // Is it the key indicator?
            '?'  ->
                if (checkKey()) {
                    fetchKey()
                    return
                }

            // Is it the value indicator?
            ':'  ->
                if (checkValue()) {
                    fetchValue()
                    return
                }

            // Is it an alias?
            '*'  -> {
                fetchAlias()
                return
            }

            // Is it an anchor?
            '&'  -> {
                fetchAnchor()
                return
            }

            // Is it a tag?
            '!'  -> {

                fetchTag()
                return
            }

            // Is it a literal scalar?
            '|'  ->
                if (isBlockContext()) {
                    fetchLiteral()
                    return
                }

            // Is it a folded scalar?
            '>'  ->
                if (isBlockContext()) {
                    fetchFolded()
                    return
                }

            // Is it a single quoted scalar?
            '\'' -> {
                fetchSingle()
                return
            }

            // Is it a double quoted scalar?
            '"'  -> {
                fetchDouble()
                return
            }

            else -> {}
        }
        if (checkPlain()) {
            fetchPlain()
            return
        }
        // No? It's an error. Let's produce a nice error message.
        // We do this by converting escaped characters into their escape sequences.
        var chRepresentation = CharConstants.escapeChar(Character.toChars(c)[0])
        if (c == '\t'.code) {
            chRepresentation += "(TAB)" // TAB deserves a special clarification
        }
        throw ScannerException(
            context = "while scanning for the next token",
            contextMark = Optional.empty(),
            problem = "found character '$chRepresentation' that cannot start any token. (Do not use $chRepresentation for indentation)",
            problemMark = reader.getMark(),
        )

    }

    //region Simple keys treatment.

    /**
     * Return the number of the nearest possible simple key. Actually we don't need to loop through
     * the whole dictionary.
     */
    private fun nextPossibleSimpleKey(): Int {
        // Because possibleSimpleKeys is ordered we can simply take the first key
        return possibleSimpleKeys.values.firstOrNull()?.tokenNumber ?: -1
    }

    //region Simple keys treatment.
    /**
     * ```
     * Remove entries that are no longer possible simple keys. According to
     * the YAML specification, simple keys
     * - should be limited to a single line,
     * - should be no longer than 1024 characters.
     * Disabling this procedure will allow simple keys of any length and
     * height (may cause problems if indentation is broken though).
     * ```
     */
    private fun stalePossibleSimpleKeys() {
        if (possibleSimpleKeys.isNotEmpty()) {
            val iterator = possibleSimpleKeys.values.iterator()
            while (iterator.hasNext()) {
                val key = iterator.next()
                if (key.line != reader.line || reader.index - key.index > 1024) {
                    // If the key is not on the same line as the current
                    // position OR the difference in column between the token
                    // start and the current position is more than the maximum
                    // simple key length, then this cannot be a simple key.
                    if (key.isRequired) {
                        // If the key was required, this implies an error condition.
                        throw ScannerException(
                            context = "while scanning a simple key",
                            contextMark = key.mark,
                            problem = "could not find expected ':'",
                            problemMark = reader.getMark(),
                        )
                    }
                    iterator.remove()
                }
            }
        }
    }

    /**
     * The next token may start a simple key. We check if it's possible and save its position.
     * This function is called for `ALIAS`, `ANCHOR`, `TAG`, `SCALAR(flow)`, `[`, and `{`.
     */
    private fun savePossibleSimpleKey() {
        // The next token may start a simple key. We check if it's possible
        // and save its position. This function is called for
        // ALIAS, ANCHOR, TAG, SCALAR(flow), '[', and '{'.

        // Check if a simple key is required at the current position.
        // A simple key is required if this position is the root flowLevel, AND
        // the current indentation level is the same as the last indent-level.
        val required = isBlockContext() && indent == reader.column

        if (allowSimpleKey || !required) {
            // A simple key is required only if it is the first token in the current line.
            // Therefore, it is always allowed.
        } else {
            throw YamlEngineException("A simple key is required only if it is the first token in the current line")
        }

        // The next token might be a simple key. Let's save its number and position.
        if (allowSimpleKey) {
            removePossibleSimpleKey()
            val tokenNumber = tokensTaken + tokens.size
            val key = SimpleKey(
                tokenNumber = tokenNumber,
                isRequired = required,
                index = reader.index,
                line = reader.line,
                column = reader.column,
                mark = reader.getMark(),
            )
            possibleSimpleKeys[flowLevel] = key
        }
    }

    /** Remove the saved possible key position at the current flow level. */
    private fun removePossibleSimpleKey() {
        val key = possibleSimpleKeys.remove(flowLevel)
        if (key != null && key.isRequired) {
            throw ScannerException(
                context = "while scanning a simple key",
                contextMark = key.mark,
                problem = "could not find expected ':'",
                problemMark = reader.getMark(),
            )
        }
    }
    //endregion

    //region Indentation functions.

    /**
     * * Handle implicitly ending multiple levels of block nodes by decreased indentation. This
     * function becomes important on lines 4 and 7 of this example:
     *
     * ```yaml
     * 1| book one:
     * 2|   part one:
     * 3|     chapter one
     * 4|   part two:
     * 5|     chapter one
     * 6|     chapter two
     * 7| book two:
     * ```
     *
     * In flow context, tokens should respect indentation. Actually the condition should be
     * `self.indent >= column` according to the spec. But this condition will prohibit intuitively
     * correct constructions such as `key : { }`
     */
    private fun unwindIndent(col: Int) {
        // In the flow context, indentation is ignored. We make the scanner less
        // restrictive than specification requires.
        if (isFlowContext()) {
            return
        }

        // In block context, we may need to issue the BLOCK-END tokens.
        while (indent > col) {
            val mark = reader.getMark()
            indent = indents.removeLast()
            addToken(BlockEndToken(mark, mark))
        }
    }

    /** Check if we need to increase indentation. */
    private fun addIndent(column: Int): Boolean {
        if (indent >= column) return false

        indents.addLast(indent)
        indent = column
        return true
    }
    //endregion

    //region Fetchers - add tokens to the stream (can call scanners)

    /** We always add `STREAM-START` as the first token and `STREAM-END` as the last token. */
    private fun fetchStreamStart() {
        // Read the token.
        val mark = reader.getMark()

        // Add STREAM-START.
        val token: Token = StreamStartToken(mark, mark)
        addToken(token)
    }

    private fun fetchStreamEnd() {
        // Set the current indentation to -1.
        unwindIndent(-1)

        // Reset simple keys.
        removePossibleSimpleKey()
        allowSimpleKey = false
        possibleSimpleKeys.clear()

        // Read the token.
        val mark = reader.getMark()

        // Add STREAM-END.
        val token: Token = StreamEndToken(mark, mark)
        addToken(token)

        // The stream is finished.
        done = true
    }

    /**
     * Fetch a YAML directive. Directives are presentation details that are interpreted as
     * instructions to the processor. YAML defines two kinds of directives, YAML and TAG; all other
     * types are reserved for future use.
     */
    private fun fetchDirective() {
        // Set the current indentation to -1.
        unwindIndent(-1)

        // Reset simple keys.
        removePossibleSimpleKey()
        allowSimpleKey = false

        // Scan and add DIRECTIVE.
        val tok = scanDirective()
        addAllTokens(tok)
    }

    /** Fetch a document-start token (`---`). */
    private fun fetchDocumentStart(): Unit = fetchDocumentIndicator(true)

    /** Fetch a document-end token (`...`). */
    private fun fetchDocumentEnd() = fetchDocumentIndicator(false)

    /**
     * Fetch a document indicator, either `---` for "document-start", or else `...` for "document-end.
     * The type is chosen by the given boolean.
     */
    private fun fetchDocumentIndicator(isDocumentStart: Boolean) {
        // Set the current indentation to -1.
        unwindIndent(-1)

        // Reset simple keys. Note that there could not be a block collection after '---'.
        removePossibleSimpleKey()
        allowSimpleKey = false

        // Add DOCUMENT-START or DOCUMENT-END.
        val startMark = reader.getMark()
        reader.forward(3)
        val endMark = reader.getMark()
        val token = if (isDocumentStart) {
            DocumentStartToken(startMark, endMark)
        } else {
            DocumentEndToken(startMark, endMark)
        }
        addToken(token)
    }

    private fun fetchFlowSequenceStart() = fetchFlowCollectionStart(false)

    private fun fetchFlowMappingStart() = fetchFlowCollectionStart(true)

    /**
     * Fetch a flow-style collection start, which is either a sequence or a mapping. The type is
     * determined by the given boolean.
     *
     * A flow-style collection is in a format similar to JSON. Sequences are started by `[` and ended
     * by `]`; mappings are started by `{` and ended by `}`.
     *
     * @param isMappingStart `true` for mapping, `false` for sequence
     */
    private fun fetchFlowCollectionStart(isMappingStart: Boolean) {
        // `[` and `{` may start a simple key.
        savePossibleSimpleKey()

        // Increase the flow level.
        flowLevel++

        // Simple keys are allowed after `[` and `{`.
        allowSimpleKey = true

        // Add FLOW-SEQUENCE-START or FLOW-MAPPING-START.
        val startMark = reader.getMark()
        reader.forward(1)
        val endMark = reader.getMark()
        val token = if (isMappingStart) {
            FlowMappingStartToken(startMark, endMark)
        } else {
            FlowSequenceStartToken(startMark, endMark)
        }
        addToken(token)
    }

    private fun fetchFlowSequenceEnd() = fetchFlowCollectionEnd(false)

    private fun fetchFlowMappingEnd() = fetchFlowCollectionEnd(true)

    /**
     * Fetch a flow-style collection end, which is either a sequence or a mapping. The type is
     * determined by the given boolean.
     *
     * A flow-style collection is in a format similar to JSON. Sequences are started by `[` and ended
     * by `]`; mappings are started by `{` and ended by `}`.
     */
    private fun fetchFlowCollectionEnd(isMappingEnd: Boolean) {
        // Reset possible simple key on the current level.
        removePossibleSimpleKey()

        // Decrease the flow level.
        flowLevel--

        // No simple keys after ']' or '}'.
        allowSimpleKey = false

        // Add FLOW-SEQUENCE-END or FLOW-MAPPING-END.
        val startMark = reader.getMark()
        reader.forward()
        val endMark = reader.getMark()
        val token = if (isMappingEnd) {
            FlowMappingEndToken(startMark, endMark)
        } else {
            FlowSequenceEndToken(startMark, endMark)
        }
        addToken(token)
    }

    /**
     * Fetch an entry in the flow style. Flow-style entries occur either immediately after the start
     * of a collection, or else after a comma.
     */
    private fun fetchFlowEntry() {
        // Simple keys are allowed after ','.
        allowSimpleKey = true

        // Reset possible simple key on the current level.
        removePossibleSimpleKey()

        // Add FLOW-ENTRY.
        val startMark = reader.getMark()
        reader.forward()
        val endMark = reader.getMark()
        val token = FlowEntryToken(startMark, endMark)
        addToken(token)
    }

    /** Fetch an entry in the block style. */
    private fun fetchBlockEntry() {
        // Block context needs additional checks.
        if (isBlockContext()) {
            // Are we allowed to start a new entry?
            if (!allowSimpleKey) {
                throw ScannerException("", Optional.empty(), "sequence entries are not allowed here", reader.getMark())
            }

            // We may need to add BLOCK-SEQUENCE-START.
            if (addIndent(reader.column)) {
                val mark = reader.getMark()
                addToken(BlockSequenceStartToken(mark, mark))
            }
        } else {
            // It's an error for the block entry to occur in the flow
            // context, but we let the scanner detect this.
        }
        // Simple keys are allowed after '-'.
        allowSimpleKey = true

        // Reset possible simple key on the current level.
        removePossibleSimpleKey()

        // Add BLOCK-ENTRY.
        val startMark = reader.getMark()
        reader.forward()
        val endMark = reader.getMark()
        val token: Token = BlockEntryToken(startMark, endMark)
        addToken(token)
    }

    /** Fetch a key in a block-style mapping. */
    private fun fetchKey() {
        // Block context needs additional checks.
        if (isBlockContext()) {
            // Are we allowed to start a key (not necessary a simple)?
            if (!allowSimpleKey) {
                throw ScannerException("mapping keys are not allowed here", reader.getMark())
            }
            // We may need to add BLOCK-MAPPING-START.
            if (addIndent(reader.column)) {
                val mark = reader.getMark()
                addToken(BlockMappingStartToken(mark, mark))
            }
        }
        // Simple keys are allowed after '?' in the block context.
        allowSimpleKey = isBlockContext()

        // Reset possible simple key on the current level.
        removePossibleSimpleKey()

        // Add KEY.
        val startMark = reader.getMark()
        reader.forward()
        val endMark = reader.getMark()
        val token: Token = KeyToken(startMark, endMark)
        addToken(token)
    }

    /** Fetch a value in a block-style mapping. */
    private fun fetchValue() {
        // Do we determine a simple key?
        val key = possibleSimpleKeys.remove(flowLevel)
        if (key != null) {
            // Add KEY.
            addToken(key.tokenNumber - tokensTaken, KeyToken(key.mark, key.mark))

            // If this key starts a new block mapping, we need to add BLOCK-MAPPING-START.
            if (isBlockContext() && addIndent(key.column)) {
                addToken(
                    key.tokenNumber - tokensTaken,
                    BlockMappingStartToken(key.mark, key.mark),
                )
            }
            // There cannot be two simple keys one after another.
            allowSimpleKey = false
        } else {
            // It must be a part of a complex key.
            // Block context needs additional checks. Do we really need them?
            // They will be caught by the scanner anyway.
            if (isBlockContext()) {
                // We are allowed to start a complex value if and only if we can
                // start a simple key.
                if (!allowSimpleKey) {
                    throw ScannerException("mapping values are not allowed here", reader.getMark())
                }
            }

            // If this value starts a new block mapping, we need to add
            // BLOCK-MAPPING-START. It will be detected as an error later by
            // the scanner.
            if (isBlockContext() && addIndent(reader.column)) {
                val mark = reader.getMark()
                addToken(BlockMappingStartToken(mark, mark))
            }

            // Simple keys are allowed after ':' in the block context.
            allowSimpleKey = isBlockContext()

            // Reset possible simple key on the current level.
            removePossibleSimpleKey()
        }
        // Add VALUE.
        val startMark = reader.getMark()
        reader.forward()
        val endMark = reader.getMark()
        val token: Token = ValueToken(startMark, endMark)
        addToken(token)
    }

    /**
     * Fetch an alias, which is a reference to an anchor. Aliases take the format:
     *
     * ```
     * *(anchor name)
     * ```
     */
    private fun fetchAlias() {
        // ALIAS could be a simple key.
        savePossibleSimpleKey()

        // No simple keys after ALIAS.
        allowSimpleKey = false

        // Scan and add ALIAS.
        val tok = scanAnchor(false)
        addToken(tok)
    }

    /**
     * Fetch an anchor. Anchors take the form:
     *
     * ```
     * &(anchor name)
     * ```
     */
    private fun fetchAnchor() {
        // ANCHOR could start a simple key.
        savePossibleSimpleKey()

        // No simple keys after ANCHOR.
        allowSimpleKey = false

        // Scan and add ANCHOR.
        val tok = scanAnchor(true)
        addToken(tok)
    }

    /** Fetch a tag. Tags take a complex form. */
    private fun fetchTag() {
        // TAG could start a simple key.
        savePossibleSimpleKey()

        // No simple keys after TAG.
        allowSimpleKey = false

        // Scan and add TAG.
        val token = scanTag()
        addToken(token)
    }


    /**
     * Fetch a literal scalar, denoted with a vertical-bar. This is the type best used for source code
     * and other content, such as binary data, which must be included verbatim.
     */
    private fun fetchLiteral() = fetchBlockScalar(ScalarStyle.LITERAL)

    /**
     * Fetch a folded scalar, denoted with a greater-than sign. This is the type best used for long
     * content, such as the text of a chapter or description.
     */
    private fun fetchFolded() = fetchBlockScalar(ScalarStyle.FOLDED)

    /**
     * Fetch a block scalar (literal or folded).
     */
    private fun fetchBlockScalar(style: ScalarStyle) {
        // A simple key may follow a block scalar.
        allowSimpleKey = true

        // Reset possible simple key on the current level.
        removePossibleSimpleKey()

        // Scan and add SCALAR.
        val tok = scanBlockScalar(style)
        addAllTokens(tok)
    }

    /** Fetch a single-quoted (') scalar. */
    private fun fetchSingle() = fetchFlowScalar(ScalarStyle.SINGLE_QUOTED)

    /** Fetch a double-quoted (") scalar. */
    private fun fetchDouble() = fetchFlowScalar(ScalarStyle.DOUBLE_QUOTED)

    /** Fetch a flow scalar (single- or double-quoted). */
    private fun fetchFlowScalar(style: ScalarStyle?) {
        // A flow scalar could be a simple key.
        savePossibleSimpleKey()

        // No simple keys after flow scalars.
        allowSimpleKey = false

        // Scan and add SCALAR.
        val tok = scanFlowScalar(style!!)
        addToken(tok)
    }

    /** Fetch a plain scalar. */
    private fun fetchPlain() {
        // A plain scalar could be a simple key.
        savePossibleSimpleKey()

        // No simple keys after plain scalars. But note that `scan_plain` will
        // change this flag if the scan is finished at the beginning of the
        // line.
        allowSimpleKey = false

        // Scan and add SCALAR. May change `allow_simple_key`.
        val tok = scanPlain()
        addToken(tok)
    }
    //endregion

    //region Checkers.
    /**
     * Returns `true` if the next thing on the reader is a directive, given that the leading `%` has
     * already been checked.
     */
    private fun checkDirective(): Boolean {
        // DIRECTIVE: ^ '%' ...
        // The '%' indicator is already checked.
        return reader.column == 0
    }

    /**
     * Returns true if the next thing on the reader is a document-start (`---`).
     * A document-start is always followed immediately by a new line.
     */
    private fun checkDocumentStart(): Boolean {
        // DOCUMENT-START: ^ '---' (' '|'\n')
        return if (reader.column == 0) {
            "---" == reader.prefix(3) && CharConstants.NULL_BL_T_LINEBR.has(reader.peek(3))
        } else false
    }


    /**
     * Returns true if the next thing on the reader is a document-end (`...`).
     * A document-end is always followed immediately by a new line.
     */
    private fun checkDocumentEnd(): Boolean {
        // DOCUMENT-END: ^ '...' (' '|'\n')
        return if (reader.column == 0) {
            "..." == reader.prefix(3) && CharConstants.NULL_BL_T_LINEBR.has(reader.peek(3))
        } else false
    }

    /** Returns `true` if the next thing on the reader is a block token. */
    private fun checkBlockEntry(): Boolean {
        // BLOCK-ENTRY: '-' (' '|'\n')
        return CharConstants.NULL_BL_T_LINEBR.has(reader.peek(1))
    }

    /**
     * Returns `true` if the next thing on the reader is a key token. This is different in SnakeYAML ->
     * `?` may start a token in the flow context
     */
    private fun checkKey(): Boolean {
        // KEY: '?' (' ' or '\n')
        return CharConstants.NULL_BL_T_LINEBR.has(reader.peek(1))
    }

    /**
     * Returns `true` if the next thing on the reader is a value token.
     */
    private fun checkValue(): Boolean {
        // VALUE(flow context): ':'
        return if (isFlowContext()) {
            true
        } else {
            // VALUE(block context): ':' (' '|'\n')
            CharConstants.NULL_BL_T_LINEBR.has(reader.peek(1))
        }
    }

    /** Returns `true` if the next thing on the reader is a plain token. */
    private fun checkPlain(): Boolean {
        // * A plain scalar may start with any non-space character except: '-', '?', ':', ',', '[', ']',
        // * '{', '}', '#', '&amp;', '*', '!', '|', '&gt;', '\'', '\&quot;', '%', '@', '`'.
        val c = reader.peek()
        // If the next char is NOT one of the forbidden chars above or
        // whitespace, then this is the start of a plain scalar.
        val notForbidden = CharConstants.NULL_BL_T_LINEBR.hasNo(c, "-?:,[]{}#&*!|>'\"%@`")
        return if (notForbidden) {
            true // plain scalar
        } else {
            if (isBlockContext()) {
                // It may also start with '-', '?', ':' if it is followed by a non-space character
                // in the block context
                CharConstants.NULL_BL_T_LINEBR.hasNo(reader.peek(1)) && "-?:".indexOf(c.toChar()) != -1
            } else {
                // It may also start with '-', '?' if it is followed by a non-space character
                // except ',' or ']' in the flow context
                CharConstants.NULL_BL_T_LINEBR.hasNo(reader.peek(1), ",]") && "-?".indexOf(c.toChar()) != -1
            }
        }
    }

    //endregion

    //region Scanners - create tokens

    /**
     * ```
     * We ignore spaces, line breaks and comments.
     * If we find a line break in the block context, we set the flag
     * `allow_simple_key` on.
     * The byte order mark is stripped if it's the first character in the
     * stream. We do not yet support BOM inside the stream as the
     * specification requires. Any such mark will be considered as a part
     * of the document.
     * TODO: We need to make tab handling rules more sane. A good rule is
     *       Tabs cannot precede tokens
     * BLOCK-SEQUENCE-START, BLOCK-MAPPING-START, BLOCK-END,
     * KEY(block), VALUE(block), BLOCK-ENTRY
     * So the checking code is
     * if <TAB>:
     * self.allow_simple_keys = False
     * We also need to add the check for `allow_simple_keys == True` to
     * `unwind_indent` before issuing BLOCK-END.
     * Scanners for block, flow, and plain scalars need to be modified.
     * </TAB>
     *```
     */
    private fun scanToNextToken() {
        // If there is a byte order mark (BOM) at the beginning of the stream,
        // forward past it.
        if (reader.index == 0 && reader.peek() == 0xFEFF) {
            reader.forward()
        }
        var found = false
        var inlineStartColumn = -1
        while (!found) {
            val startMark = reader.getMark()
            val columnBeforeComment = reader.column
            var commentSeen = false
            var ff = 0
            // Peek ahead until we find the first non-space character, then
            // move forward directly to that character.
            while (reader.peek(ff) == ' '.code) {
                ff++
            }
            if (ff > 0) {
                reader.forward(ff)
            }
            // If the character we have skipped forward to is a comment (#),
            // then peek ahead until we find the next end of line. YAML
            // comments are from a # to the next new-line. We then forward
            // past the comment.
            if (reader.peek() == '#'.code) {
                commentSeen = true
                var type: CommentType
                if (columnBeforeComment != 0
                    && !(lastToken != null && lastToken.tokenId == Token.ID.BlockEntry)
                ) {
                    type = CommentType.IN_LINE
                    inlineStartColumn = reader.column
                } else if (inlineStartColumn == reader.column) {
                    type = CommentType.IN_LINE
                } else {
                    inlineStartColumn = -1
                    type = CommentType.BLOCK
                }
                val token = scanComment(type)
                if (settings.parseComments) {
                    addToken(token)
                }
            }
            // If we scanned a line break, then (depending on flow level),
            // simple keys may be allowed.
            val breaksOpt = scanLineBreak()
            if (breaksOpt.isPresent) { // found a line-break
                if (settings.parseComments && !commentSeen) {
                    if (columnBeforeComment == 0) {
                        addToken(
                            CommentToken(
                                CommentType.BLANK_LINE, breaksOpt.get(), startMark,
                                reader.getMark(),
                            ),
                        )
                    }
                }
                if (isBlockContext()) {
                    // Simple keys are allowed at flow-level 0 after a line break
                    allowSimpleKey = true
                }
            } else {
                found = true
            }
        }
    }

    private fun scanComment(type: CommentType): CommentToken = scannerJava.scanComment(type)
    private fun scanDirective(): List<Token> = scannerJava.scanDirective()
    private fun scanDirectiveName(startMark: Optional<Mark>): String = scannerJava.scanDirectiveName(startMark)
    private fun scanYamlDirectiveValue(startMark: Optional<Mark>): List<Int> =
        scannerJava.scanYamlDirectiveValue(startMark)

    private fun scanYamlDirectiveNumber(startMark: Optional<Mark>): Int = scannerJava.scanYamlDirectiveNumber(startMark)
    private fun scanTagDirectiveValue(startMark: Optional<Mark>): List<String> =
        scannerJava.scanTagDirectiveValue(startMark)

    private fun scanTagDirectiveHandle(startMark: Optional<Mark>): String =
        scannerJava.scanTagDirectiveHandle(startMark)

    private fun scanTagDirectivePrefix(startMark: Optional<Mark>): String =
        scannerJava.scanTagDirectivePrefix(startMark)

    private fun scanDirectiveIgnoredLine(startMark: Optional<Mark>): CommentToken =
        scannerJava.scanDirectiveIgnoredLine(startMark)


    /**
     * ```
     * The YAML 1.2 specification does not restrict characters for anchors and
     * aliases. This may lead to problems.
     * see [issue 485](https://bitbucket.org/snakeyaml/snakeyaml/issues/485/alias-names-are-too-permissive-compared-to)
     * This implementation tries to follow [RFC-0003](https://github.com/yaml/yaml-spec/blob/master/rfc/RFC-0003.md)
     * ```
     */
    private fun scanAnchor(isAnchor: Boolean): Token {
        val startMark = reader.getMark()
        val indicator = reader.peek()
        val name = if (indicator == '*'.code) "alias" else "anchor"
        reader.forward()
        var length = 0
        var c = reader.peek(length)
        // Anchor may not contain ",[]{}"
        while (CharConstants.NULL_BL_T_LINEBR.hasNo(c, ",[]{}/.*&")) {
            length++
            c = reader.peek(length)
        }
        if (length == 0) {
            val s = String(Character.toChars(c))
            throw ScannerException(
                problem = "while scanning an $name",
                problemMark = startMark,
                context = "unexpected character found $s($c)",
                contextMark = reader.getMark(),
            )
        }
        val value = reader.prefixForward(length)
        c = reader.peek()
        if (CharConstants.NULL_BL_T_LINEBR.hasNo(c, "?:,]}%@`")) {
            val s = String(Character.toChars(c))
            throw ScannerException(
                problem = "while scanning an $name",
                problemMark = startMark,
                context = "unexpected character found $s($c)",
                contextMark = reader.getMark(),
            )
        }
        val endMark = reader.getMark()
        return if (isAnchor) {
            AnchorToken(Anchor(value), startMark, endMark)
        } else {
            AliasToken(Anchor(value), startMark, endMark)
        }
    }


    /**
     * Scan a Tag property. A Tag property may be specified in one of three ways: c-verbatim-tag,
     * c-ns-shorthand-tag, or c-ns-non-specific-tag
     *
     * c-verbatim-tag takes the form !<ns-uri-char></ns-uri-char>+> and must be delivered verbatim (as-is) to the
     * application. In particular, verbatim tags are not subject to tag resolution.
     *
     * c-ns-shorthand-tag is a valid tag handle followed by a non-empty suffix. If the tag handle is a
     * c-primary-tag-handle ('!') then the suffix must have all exclamation marks properly URI-escaped
     * (%21); otherwise, the string will look like a named tag handle: !foo!bar would be interpreted
     * as (handle="!foo!", suffix="bar").
     *
     * c-ns-non-specific-tag is always a lone '!'; this is only useful for plain scalars, where its
     * specification means that the scalar MUST be resolved to have type tag:yaml.org,2002:str.
     *
     * TODO Note that this method does not enforce rules about local versus global tags!
     */
    private fun scanTag(): Token {
        // See the specification for details.
        val startMark = reader.getMark()
        // Determine the type of tag property based on the first character
        // encountered
        var c = reader.peek(1)
        val handle: String?
        val suffix: String
        // Verbatim tag! (c-verbatim-tag)
        if (c == '<'.code) {
            // Skip the exclamation mark and &gt;, then read the tag suffix (as a URI).
            reader.forward(2)
            suffix = scanTagUri("tag", CharConstants.URI_CHARS_FOR_TAG_PREFIX, startMark)
            c = reader.peek()
            if (c != '>'.code) {
                // If there are any characters between the end of the tag-suffix
                // URI and the closing &gt;, then an error has occurred.
                val s = String(Character.toChars(c))
                throw ScannerException(
                    "while scanning a tag", startMark,
                    "expected '>', but found '$s' ($c)", reader.getMark(),
                )
            }
            handle = null
            reader.forward()
        } else if (CharConstants.NULL_BL_T_LINEBR.has(c)) {
            // A NUL, blank, tab, or line-break means that this was a
            // c-ns-non-specific tag.
            suffix = "!"
            handle = null
            reader.forward()
        } else {
            // Any other character implies c-ns-shorthand-tag type.

            // Look ahead in the stream to determine whether this tag property
            // is of the form !foo or !foo!bar.
            var length = 1
            var useHandle = false
            while (CharConstants.NULL_BL_LINEBR.hasNo(c)) {
                if (c == '!'.code) {
                    useHandle = true
                    break
                }
                length++
                c = reader.peek(length)
            }
            // If we need to use a handle, scan it in; otherwise, the handle is
            // presumed to be '!'.
            if (useHandle) {
                handle = scanTagHandle("tag", startMark)
            } else {
                handle = "!"
                reader.forward()
            }
            suffix = scanTagUri("tag", CharConstants.URI_CHARS_FOR_TAG_SUFFIX, startMark)
        }
        c = reader.peek()
        // Check that the next character is allowed to follow a tag-property,
        // if it is not, raise the error.
        if (CharConstants.NULL_BL_LINEBR.hasNo(c)) {
            val s = String(Character.toChars(c))
            throw ScannerException(
                problem = "while scanning a tag",
                problemMark = startMark,
                context = "expected ' ', but found '$s' ($c)",
                contextMark = reader.getMark(),
            )
        }
        val value = TagTuple(Optional.ofNullable(handle), suffix)
        val endMark = reader.getMark()
        return TagToken(value, startMark, endMark)
    }

    /**
     * Scan literal and folded scalar
     *
     * @param style - either literal or folded style
     */
    private fun scanBlockScalar(style: ScalarStyle): List<Token> {
        // See the specification for details.
        val stringBuilder = StringBuilder()
        val startMark = reader.getMark()
        // Scan the header.
        reader.forward()
        val chomping = scanBlockScalarIndicators(startMark)
        val commentToken = scanBlockScalarIgnoredLine(startMark)

        // Determine the indentation level and go to the first non-empty line.
        var minIndent = indent + 1
        if (minIndent < 1) {
            minIndent = 1
        }
        var breaks: String
        val maxIndent: Int
        val blockIndent: Int
        var endMark: Optional<Mark>
        if (chomping.increment.isPresent) {
            // increment is explicit
            blockIndent = minIndent + chomping.increment.get() - 1
            val brme = scanBlockScalarBreaks(blockIndent)
            breaks = brme.breaks
            endMark = brme.endMark
        } else {
            // increment (block indent) must be detected in the first non-empty line.
            val brme = scanBlockScalarIndentation()
            breaks = brme.breaks
            maxIndent = brme.maxIndent
            endMark = brme.endMark
            blockIndent = minIndent.coerceAtLeast(maxIndent)
        }
        var lineBreakOpt = Optional.empty<String>()
        // Scan the inner part of the block scalar.
        if (reader.column < blockIndent && indent != reader.column) {
            // it means that there is indent, but less than expected
            // fix S98Z - Block scalar with more spaces than first content line
            throw ScannerException(
                "while scanning a block scalar", startMark,
                " the leading empty lines contain more spaces (" + blockIndent
                    + ") than the first non-empty line.",
                reader.getMark(),
            )
        }
        while (reader.column == blockIndent && reader.peek() != 0) {
            stringBuilder.append(breaks)
            val leadingNonSpace = " \t".indexOf(reader.peek().toChar()) == -1
            var length = 0
            while (CharConstants.NULL_OR_LINEBR.hasNo(reader.peek(length))) {
                length++
            }
            stringBuilder.append(reader.prefixForward(length))
            lineBreakOpt = scanLineBreak()
            val brme = scanBlockScalarBreaks(blockIndent)
            breaks = brme.breaks
            endMark = brme.endMark
            if (reader.column == blockIndent && reader.peek() != 0) {

                // Unfortunately, folding rules are ambiguous.
                // This is the folding according to the specification:
                if (
                    style == ScalarStyle.FOLDED
                    && "\n" == lineBreakOpt.orElse("")
                    && leadingNonSpace
                    && " \t".indexOf(reader.peek().toChar()) == -1
                ) {
                    if (breaks.isEmpty()) {
                        stringBuilder.append(" ")
                    }
                } else {
                    stringBuilder.append(lineBreakOpt.orElse(""))
                }
            } else {
                break
            }
        }
        // Chomp the tail.
        if (chomping.value === Chomping.Indicator.CLIP || chomping.value === Chomping.Indicator.KEEP) {
            // add the final line break (if exists !) TODO find out if to add anyway
            stringBuilder.append(lineBreakOpt.orElse(""))
        }
        if (chomping.value === Chomping.Indicator.KEEP) {
            // any trailing empty lines are considered to be part of the scalar’s content
            stringBuilder.append(breaks)
        }
        // We are done.
        val scalarToken = ScalarToken(stringBuilder.toString(), false, startMark, endMark, style)
        return makeTokenList(commentToken, scalarToken)
    }


    private fun scanBlockScalarIndicators(startMark: Optional<Mark>): Chomping =
        scannerJava.scanBlockScalarIndicators(startMark)


    /**
     * Scan to the end of the line after a block scalar has been scanned; the only things that are
     * permitted at this time are comments and spaces.
     */
    private fun scanBlockScalarIgnoredLine(startMark: Optional<Mark>): CommentToken? {
        // See the specification for details.

        // Forward past any number of trailing spaces
        while (reader.peek() == ' '.code) {
            reader.forward()
        }

        // If a comment occurs, scan to just before the end of line.
        var commentToken: CommentToken? = null
        if (reader.peek() == '#'.code) {
            commentToken = scanComment(CommentType.IN_LINE)
        }
        // If the next character is not a null or line break, an error has
        // occurred.
        val c = reader.peek()
        if (!scanLineBreak().isPresent && c != 0) {
            val s = String(Character.toChars(c))
            throw ScannerException(
                ScannerImplJava.SCANNING_SCALAR, startMark,
                "expected a comment or a line break, but found $s($c)", reader.getMark(),
            )
        }
        return commentToken
    }

    private fun scanBlockScalarIndentation(): BreakIntentHolder = scannerJava.scanBlockScalarIndentation()
    private fun scanBlockScalarBreaks(indent: Int): BreakIntentHolder = scannerJava.scanBlockScalarBreaks(indent)


    /**
     * Scan a flow-style scalar. Flow scalars are presented in one of two forms; first, a flow scalar
     * may be a double-quoted string; second, a flow scalar may be a single-quoted string.
     *
     * ```
     * See the specification for details.
     * Note that we loose indentation rules for quoted scalars. Quoted
     * scalars don't need to adhere indentation because &quot; and ' clearly
     * mark the beginning and the end of them. Therefore we are less
     * restrictive then the specification requires. We only need to check
     * that document separators are not included in scalars.
     * ```
     */
    private fun scanFlowScalar(style: ScalarStyle): Token {
        // The style will be either single- or double-quoted; we determine this
        // by the first character in the entry (supplied)
        val doubleValue = style == ScalarStyle.DOUBLE_QUOTED
        val startMark = reader.getMark()
        val quote = reader.peek()
        reader.forward()
        val chunks = buildString {
            append(scanFlowScalarNonSpaces(doubleValue, startMark))
            while (reader.peek() != quote) {
                append(scanFlowScalarSpaces(startMark))
                append(scanFlowScalarNonSpaces(doubleValue, startMark))
            }
        }
        reader.forward()
        val endMark = reader.getMark()
        return ScalarToken(chunks, false, startMark, endMark, style)
    }


    /**
     * Scan some number of flow-scalar non-space characters.
     */
    fun scanFlowScalarNonSpaces(doubleQuoted: Boolean, startMark: Optional<Mark>): String {
        // See the specification for details.
        val chunks = StringBuilder()
        while (true) {
            // Scan through any number of characters which are not: NUL, blank,
            // tabs, line breaks, single-quotes, double-quotes, or backslashes.
            var length = 0
            while (CharConstants.NULL_BL_T_LINEBR.hasNo(reader.peek(length), "'\"\\")) {
                length++
            }
            if (length != 0) {
                chunks.append(reader.prefixForward(length))
            }
            // Depending on our quoting-type, the characters ', " and \ have
            // differing meanings.
            var c = reader.peek()
            if (!doubleQuoted && c == '\''.code && reader.peek(1) == '\''.code) {
                chunks.append("'")
                reader.forward(2)
            } else if (doubleQuoted && c == '\''.code || !doubleQuoted && "\"\\".indexOf(c.toChar()) != -1) {
                chunks.appendCodePoint(c)
                reader.forward()
            } else if (doubleQuoted && c == '\\'.code) {
                reader.forward()
                c = reader.peek()
                if (!Character.isSupplementaryCodePoint(c) && CharConstants.ESCAPE_REPLACEMENTS.containsKey(c.toChar())) {
                    // The character is one of the single-replacement
                    // types; these are replaced with a literal character
                    // from the mapping.
                    chunks.append(CharConstants.ESCAPE_REPLACEMENTS[c.toChar()])
                    reader.forward()
                } else if (!Character.isSupplementaryCodePoint(c) && CharConstants.ESCAPE_CODES.containsKey(c.toChar())) {
                    // The character is a multi-digit escape sequence, with
                    // length defined by the value in the ESCAPE_CODES map.
                    length = CharConstants.ESCAPE_CODES[c.toChar()]!!
                    reader.forward()
                    val hex = reader.prefix(length)
                    if (ScannerImplJava.NOT_HEXA.matcher(hex).find()) {
                        throw ScannerException(
                            "while scanning a double-quoted scalar", startMark,
                            "expected escape sequence of $length hexadecimal numbers, but found: $hex",
                            reader.getMark(),
                        )
                    }
                    val decimal = hex.toInt(16)
                    try {
                        val unicode = String(Character.toChars(decimal))
                        chunks.append(unicode)
                        reader.forward(length)
                    } catch (e: IllegalArgumentException) {
                        throw ScannerException(
                            "while scanning a double-quoted scalar", startMark,
                            "found unknown escape character $hex", reader.getMark(),
                        )
                    }
                } else if (scanLineBreak().isPresent) {
                    chunks.append(scanFlowScalarBreaks(startMark))
                } else {
                    val s = String(Character.toChars(c))
                    throw ScannerException(
                        "while scanning a double-quoted scalar", startMark,
                        "found unknown escape character $s($c)", reader.getMark(),
                    )
                }
            } else {
                return chunks.toString()
            }
        }
    }


    private fun scanFlowScalarSpaces(startMark: Optional<Mark>): String {
        // See the specification for details.
        var length = 0
        // Scan through any number of whitespace (space, tab) characters, consuming them.
        while (" \t".indexOf(reader.peek(length).toChar()) != -1) {
            length++
        }
        val whitespaces = reader.prefixForward(length)
        val c = reader.peek()
        if (c == 0) {
            // A flow scalar cannot end with an end-of-stream
            throw ScannerException(
                problem = "while scanning a quoted scalar",
                problemMark = startMark,
                context = "found unexpected end of stream",
                contextMark = reader.getMark(),
            )
        }
        // If we encounter a line break, scan it into our assembled string...
        val lineBreakOpt = scanLineBreak()
        return buildString {
            if (lineBreakOpt.isPresent) {
                val breaks = scanFlowScalarBreaks(startMark)
                if ("\n" != lineBreakOpt.get()) {
                    append(lineBreakOpt.get())
                } else if (breaks.isEmpty()) {
                    append(" ")
                }
                append(breaks)
            } else {
                append(whitespaces)
            }
        }
    }


    private fun scanFlowScalarBreaks(startMark: Optional<Mark>): String = scannerJava.scanFlowScalarBreaks(startMark)

    /**
     * Scan a plain scalar.
     *
     * See the specification for details. We add an additional restriction for the flow context: plain
     * scalars in the flow context cannot contain `,`, `:` and `?`. We also keep track of the
     * `allow_simple_key` flag here. Indentation rules are loosened for the flow context.
     */
    private fun scanPlain(): Token {
        val chunks = StringBuilder()
        val startMark: Optional<Mark> = reader.getMark()
        var endMark = startMark
        val plainIndent = indent + 1
        var spaces = ""
        while (true) {
            var c: Int
            var length = 0
            // A comment indicates the end of the scalar.
            if (reader.peek() == '#'.code) {
                break
            }
            while (true) {
                c = reader.peek(length)
                if (
                    CharConstants.NULL_BL_T_LINEBR.has(c)
                    || c == ':'.code
                    && CharConstants.NULL_BL_T_LINEBR.has(
                        reader.peek(length + 1),
                        if (isFlowContext()) ",[]{}" else "",
                    )
                    || isFlowContext() && ",[]{}".indexOf(c.toChar()) != -1
                ) {
                    break
                }
                length++
            }
            if (length == 0) {
                break
            }
            allowSimpleKey = false
            chunks.append(spaces)
            chunks.append(reader.prefixForward(length))
            endMark = reader.getMark()
            spaces = scanPlainSpaces()
            if (
                spaces.isEmpty()
                || reader.peek() == '#'.code
                || isBlockContext() && reader.column < plainIndent
            ) {
                break
            }
        }
        return ScalarToken(chunks.toString(), true, startMark, endMark)
    }


    /**
     * Helper for scanPlainSpaces method when comments are enabled.
     * The ensures that blank lines and comments following a multi-line plain token are not swallowed
     * up
     */
    private fun atEndOfPlain(): Boolean {
        // peak ahead to find end of whitespaces and the column at which it occurs
        var wsLength = 0
        var wsColumn = reader.column
        run {
            var c: Int
            while (
                reader.peek(wsLength).also { c = it } != 0
                && CharConstants.NULL_BL_T_LINEBR.has(c)
            ) {
                wsLength++
                if (!CharConstants.LINEBR.has(c) && (c != '\r'.code || reader.peek(wsLength + 1) != '\n'.code) && c != 0xFEFF
                ) {
                    wsColumn++
                } else {
                    wsColumn = 0
                }
            }
        }

        // if we see, a comment or end of string or change decrease in indent, we are done
        // Do not chomp end of lines and blanks, they will be handled by the main loop.
        if (reader.peek(wsLength) == '#'.code || reader.peek(wsLength + 1) == 0 || isBlockContext() && wsColumn < indent) {
            return true
        }

        // if we see, after the space, a key-value followed by a ':', we are done
        // Do not chomp end of lines and blanks, they will be handled by the main loop.
        if (isBlockContext()) {
            var c: Int
            var extra = 1
            while (reader.peek(wsLength + extra).also { c = it } != 0
                && !CharConstants.NULL_BL_T_LINEBR.has(c)) {
                if (c == ':'.code && CharConstants.NULL_BL_T_LINEBR.has(reader.peek(wsLength + extra + 1))) {
                    return true
                }
                extra++
            }
        }

        // None of the above so safe to chomp the spaces.
        return false
    }


    /**
     * See the specification for details. `SnakeYAML` and `libyaml` allow tabs inside plain scalar
     */
    private fun scanPlainSpaces(): String {
        var length = 0
        while (reader.peek(length) == ' '.code || reader.peek(length) == '\t'.code) {
            length++
        }
        val whitespaces = reader.prefixForward(length)
        val lineBreakOpt = scanLineBreak()
        if (lineBreakOpt.isPresent) {
            allowSimpleKey = true
            var prefix = reader.prefix(3)
            if ("---" == prefix || "..." == prefix && CharConstants.NULL_BL_T_LINEBR.has(reader.peek(3))) {
                return ""
            }
            if (settings.parseComments && atEndOfPlain()) {
                return ""
            }
            val breaks = StringBuilder()
            while (true) {
                if (reader.peek() == ' '.code) {
                    reader.forward()
                } else {
                    val lbOpt = scanLineBreak()
                    if (lbOpt.isPresent) {
                        breaks.append(lbOpt.get())
                        prefix = reader.prefix(3)
                        if ("---" == prefix || "..." == prefix && CharConstants.NULL_BL_T_LINEBR.has(reader.peek(3))) {
                            return ""
                        }
                    } else {
                        break
                    }
                }
            }
            return when {
                "\n" != lineBreakOpt.orElse("") -> lineBreakOpt.orElse("") + breaks
                breaks.isEmpty()                -> " "
                else                            -> breaks.toString()
            }
        }
        return whitespaces
    }

    private fun scanTagHandle(name: String, startMark: Optional<Mark>): String =
        scannerJava.scanTagHandle(name, startMark)

    private fun scanTagUri(name: String, range: CharConstants, startMark: Optional<Mark>): String =
        scannerJava.scanTagUri(name, range, startMark)

    private fun scanUriEscapes(name: String, startMark: Optional<Mark>): String =
        scannerJava.scanUriEscapes(name, startMark)

    private fun scanLineBreak(): Optional<String> = scannerJava.scanLineBreak()

    //endregion

    private fun makeTokenList(vararg tokens: Token?): List<Token> = scannerJava.makeTokenList(*tokens)
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
