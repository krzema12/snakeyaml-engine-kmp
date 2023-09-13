package org.snakeyaml.engine.v2.scanner

import kotlin.collections.set
import kotlin.jvm.JvmInline
import okio.Buffer
import org.snakeyaml.engine.internal.utils.Character
import org.snakeyaml.engine.internal.utils.appendCodePoint
import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.comments.CommentType
import org.snakeyaml.engine.v2.common.Anchor
import org.snakeyaml.engine.v2.common.CharConstants
import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.common.UriEncoder
import org.snakeyaml.engine.v2.exceptions.Mark
import org.snakeyaml.engine.v2.exceptions.ScannerException
import org.snakeyaml.engine.v2.exceptions.YamlEngineException
import org.snakeyaml.engine.v2.tokens.*


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
    /** List of processed tokens that are not yet emitted. */
    private val tokens: ArrayDeque<Token> = ArrayDeque(100)

    /** Past indentation levels. */
    private val indents = ArrayDeque<Int>(10)

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
    private val possibleSimpleKeys: MutableMap<Int, SimpleKey> = mutableMapOf()

    /** Had we reached the end of the stream */
    private var done = false

    /** The number of unclosed `{` and `[`. [isBlockContext] means block context. */
    private var flowLevel = 0

    /** The last added token */
    private var lastToken: Token? = null

    /**
     * Variables related to simple keys treatment.
     * Number of tokens that were emitted through the [checkToken] method.
     */
    private var tokensTaken = 0

    /** The current indentation level. */
    private var indent = -1

    /**
     * ```text
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
    private var allowSimpleKey = true

    init {
        fetchStreamStart() // Add the STREAM-START token.
    }

    /**
     * Check whether the next token is present.
     *
     * If no [choices] are provided, then any token is considered valid.
     * If any [choices] are provided, then the next token must match one of the [Token.ID]s.
     */
    override fun checkToken(vararg choices: Token.ID): Boolean {
        while (needMoreTokens()) {
            fetchMoreTokens()
        }
        val firstTokenId = tokens.firstOrNull()?.tokenId
        return firstTokenId != null && (choices.isEmpty() || firstTokenId in choices)
    }

    /** Return the next token, but do not delete it from the queue. */
    override fun peekToken(): Token {
        while (needMoreTokens()) {
            fetchMoreTokens()
        }
        return tokens.first()
    }

    override fun hasNext(): Boolean = checkToken()

    /** Return the next token, removing it from the queue. */
    override fun next(): Token {
        tokensTaken++
        return tokens.removeFirstOrNull() ?: throw NoSuchElementException("No more Tokens found.")
    }

    override fun resetDocumentIndex(): Unit = reader.resetDocumentIndex()

    //region Private methods.

    /** Add a [token] to the end of [tokens] */
    private fun addToken(token: Token) {
        lastToken = token
        tokens.addLast(token)
    }

    /** Add a [token] at a specific [index] */
    private fun addToken(index: Int, token: Token) {
        if (index == tokens.size) {
            lastToken = token
        }
        tokens.add(index, token)
    }

    private fun addAllTokens(tokens: List<Token>) {
        lastToken = tokens.last()
        this.tokens += tokens
    }

    private fun isBlockContext(): Boolean = flowLevel == 0

    private fun isFlowContext(): Boolean = !isBlockContext()

    /** Returns `true` if more tokens should be scanned. */
    private fun needMoreTokens(): Boolean {
        // If we are done, we do not require more tokens.
        if (done) return false
        // If we aren't done, but we have no tokens, we need to scan more.
        if (tokens.isEmpty()) return true
        // The current token may be a potential simple key, so we need to look further.
        stalePossibleSimpleKeys()
        return nextPossibleSimpleKey() == tokensTaken
    }

    /** Fetch one or more tokens from the [StreamReader]. */
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
        var chRepresentation = CharConstants.escapeChar(Character.toChars(c).first())
        if (c == '\t'.code) {
            chRepresentation += "(TAB)" // TAB deserves a special clarification
        }
        throw ScannerException(
            context = "while scanning for the next token",
            contextMark = null,
            problem = "found character '$chRepresentation' that cannot start any token. (Do not use $chRepresentation for indentation)",
            problemMark = reader.getMark(),
        )
    }

    //region Simple keys treatment.

    /**
     * Return the number of the nearest possible simple key. Actually we don't need to loop through
     * the whole dictionary.
     */
    private fun nextPossibleSimpleKey(): Int? {
        // Because possibleSimpleKeys is ordered we can simply take the first key
        return possibleSimpleKeys.values.firstOrNull()?.tokenNumber
    }

    /**
     * ```text
     * Remove entries that are no longer possible simple keys. According to
     * the YAML specification, simple keys
     * - should be limited to a single line,
     * - should be no longer than 1024 characters.
     * Disabling this procedure will allow simple keys of any length and
     * height (may cause problems if indentation is broken though).
     * ```
     */
    private fun stalePossibleSimpleKeys() {
        val iterator = possibleSimpleKeys.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next().value

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
    private fun fetchDocumentEnd(): Unit = fetchDocumentIndicator(false)

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

    private fun fetchFlowSequenceStart(): Unit = fetchFlowCollectionStart(false)

    private fun fetchFlowMappingStart(): Unit = fetchFlowCollectionStart(true)

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

    private fun fetchFlowSequenceEnd(): Unit = fetchFlowCollectionEnd(false)

    private fun fetchFlowMappingEnd(): Unit = fetchFlowCollectionEnd(true)

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
                throw ScannerException(
                    "",
                    null,
                    "sequence entries are not allowed here",
                    reader.getMark()
                )
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
     * ```text
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
     * ```text
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
    private fun fetchLiteral(): Unit = fetchBlockScalar(ScalarStyle.LITERAL)

    /**
     * Fetch a folded scalar, denoted with a greater-than sign. This is the type best used for long
     * content, such as the text of a chapter or description.
     */
    private fun fetchFolded(): Unit = fetchBlockScalar(ScalarStyle.FOLDED)

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
    private fun fetchSingle(): Unit = fetchFlowScalar(ScalarStyle.SINGLE_QUOTED)

    /** Fetch a double-quoted (") scalar. */
    private fun fetchDouble(): Unit = fetchFlowScalar(ScalarStyle.DOUBLE_QUOTED)

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
        return checkDirective()
            && "---" == reader.prefix(3) && CharConstants.NULL_BL_T_LINEBR.has(reader.peek(3))
    }

    /**
     * Returns true if the next thing on the reader is a document-end (`...`).
     * A document-end is always followed immediately by a new line.
     */
    private fun checkDocumentEnd(): Boolean {
        // DOCUMENT-END: ^ '...' (' '|'\n')
        return checkDirective()
            && "..." == reader.prefix(3) && CharConstants.NULL_BL_T_LINEBR.has(reader.peek(3))
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
        return isFlowContext() // VALUE(flow context): ':'
            || CharConstants.NULL_BL_T_LINEBR.has(reader.peek(1)) // VALUE(block context): ':' (' '|'\n')
    }

    /** Returns `true` if the next thing on the reader is a plain token. */
    private fun checkPlain(): Boolean {
        // * A plain scalar may start with any non-space character except:
        // '-', '?', ':', ',', '[', ']', '{', '}', '#', '&amp;', '*', '!', '|', '&gt;', '\'', '\&quot;', '%', '@', '`'.
        val c = reader.peek()
        // If the next char is NOT one of the forbidden chars above or whitespace, then this is the start of a plain scalar.
        val notForbidden = CharConstants.NULL_BL_T_LINEBR.hasNo(c, "-?:,[]{}#&*!|>'\"%@`")
        return if (notForbidden) {
            true // plain scalar
        } else {
            if (isBlockContext()) {
                // It may also start with '-', '?', ':' if it is followed by a non-space character
                // in the block context
                CharConstants.NULL_BL_T_LINEBR.hasNo(reader.peek(1)) && c.toChar() in "-?:"
            } else {
                // It may also start with '-', '?' if it is followed by a non-space character
                // except ',' or ']' in the flow context
                CharConstants.NULL_BL_T_LINEBR.hasNo(reader.peek(1), ",]") && c.toChar() in "-?"
            }
        }
    }

    //endregion

    //region Scanners - create tokens

    /**
     * ```text
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
                val type: CommentType
                if (columnBeforeComment != 0
                    && !(lastToken != null && lastToken?.tokenId == Token.ID.BlockEntry)
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
            if (breaksOpt != null) { // found a line-break
                if (settings.parseComments && !commentSeen) {
                    if (columnBeforeComment == 0) {
                        addToken(
                            CommentToken(
                                CommentType.BLANK_LINE, breaksOpt, startMark,
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

    private fun scanComment(type: CommentType): CommentToken {
        // See the specification for details.
        val startMark = reader.getMark()
        reader.forward()
        var length = 0
        while (CharConstants.NULL_OR_LINEBR.hasNo(reader.peek(length))) {
            length++
        }
        val value = reader.prefixForward(length)
        val endMark = reader.getMark()
        return CommentToken(type, value, startMark, endMark)
    }

    private fun scanDirective(): List<Token> {
        // See the specification for details.
        val startMark = reader.getMark()
        val endMark: Mark?
        reader.forward()
        val name = scanDirectiveName(startMark)
        val value: DirectiveToken.TokenValue?
        if (DirectiveToken.YAML_DIRECTIVE == name) {
            value = scanYamlDirectiveValue(startMark)
            endMark = reader.getMark()
        } else if (DirectiveToken.TAG_DIRECTIVE == name) {
            value = scanTagDirectiveValue(startMark)
            endMark = reader.getMark()
        } else {
            endMark = reader.getMark()
            var ff = 0
            while (CharConstants.NULL_OR_LINEBR.hasNo(reader.peek(ff))) {
                ff++
            }
            if (ff > 0) {
                reader.forward(ff)
            }
            value = null
        }
        val commentToken = scanDirectiveIgnoredLine(startMark)
        val token = DirectiveToken(value, startMark, endMark)
        return makeTokenList(token, commentToken)
    }

    /**
     * Scan a directive name. Directive names are a series of non-space characters.
     */
    private fun scanDirectiveName(startMark: Mark?): String {
        // See the specification for details.
        var length = 0
        // A Directive-name is a sequence of alphanumeric characters
        // (a-z,A-Z,0-9). We scan until we find something that isn't.
        // This disagrees with the specification.
        var c = reader.peek(length)
        while (CharConstants.ALPHA.has(c)) {
            length++
            c = reader.peek(length)
        }
        // If the peeked name is empty, an error occurs.
        if (length == 0) {
            val s = Character.toChars(c).concatToString()
            throw ScannerException(
                DIRECTIVE_PREFIX, startMark,
                "$EXPECTED_ALPHA_ERROR_PREFIX$s($c)", reader.getMark(),
            )
        }
        val value = reader.prefixForward(length)
        c = reader.peek()
        if (CharConstants.NULL_BL_LINEBR.hasNo(c)) {
            val s = Character.toChars(c).concatToString()
            throw ScannerException(
                DIRECTIVE_PREFIX, startMark,
                "$EXPECTED_ALPHA_ERROR_PREFIX$s($c)", reader.getMark(),
            )
        }
        return value
    }

    private fun scanYamlDirectiveValue(startMark: Mark?): DirectiveToken.YamlDirective {
        // See the specification for details.
        while (reader.peek() == ' '.code) {
            reader.forward()
        }
        val major = scanYamlDirectiveNumber(startMark)
        var c = reader.peek()
        if (c != '.'.code) {
            val s = Character.toChars(c).concatToString()
            throw ScannerException(
                problem = DIRECTIVE_PREFIX,
                problemMark = startMark,
                context = "expected a digit or '.', but found $s($c)",
                contextMark = reader.getMark(),
            )
        }
        reader.forward()
        val minor = scanYamlDirectiveNumber(startMark)
        c = reader.peek()
        if (CharConstants.NULL_BL_LINEBR.hasNo(c)) {
            val s = Character.toChars(c).concatToString()
            throw ScannerException(
                problem = DIRECTIVE_PREFIX,
                problemMark = startMark,
                context = "expected a digit or ' ', but found $s($c)",
                contextMark = reader.getMark(),
            )
        }
        return DirectiveToken.YamlDirective(major, minor)
    }

    /**
     * Read a `%YAML` directive number: this is either the major or the minor part. Stop reading at a
     * non-digit character (usually either `.` or `\n`).
     */
    private fun scanYamlDirectiveNumber(startMark: Mark?): Int {
        // See the specification for details.
        val c = reader.peek()
        if (!c.toChar().isDigit()) {
            val s = Character.toChars(c).concatToString()
            throw ScannerException(
                problem = DIRECTIVE_PREFIX,
                problemMark = startMark,
                context = "expected a digit, but found $s($c)",
                contextMark = reader.getMark(),
            )
        }
        var length = 0
        while (reader.peek(length).toChar().isDigit()) {
            length++
        }
        val number = reader.prefixForward(length)
        if (length > 3) {
            throw ScannerException(
                problem = "while scanning a YAML directive",
                problemMark = startMark,
                context = "found a number which cannot represent a valid version: $number",
                contextMark = reader.getMark(),
            )
        }
        return number.toInt()
    }

    /**
     * Read a `%TAG` directive value:
     * ```text
     * s-ignored-space+ c-tag-handle s-ignored-space+ ns-tag-prefix s-l-comments
     * ```
     */
    private fun scanTagDirectiveValue(startMark: Mark?): DirectiveToken.TagDirective {
        // See the specification for details.
        while (reader.peek() == ' '.code) {
            reader.forward()
        }
        val handle = scanTagDirectiveHandle(startMark)
        while (reader.peek() == ' '.code) {
            reader.forward()
        }
        val prefix = scanTagDirectivePrefix(startMark)
        return DirectiveToken.TagDirective(handle, prefix)
    }

    /**
     * Scan a `%TAG` directive's handle. This is YAML's `c-tag-handle`.
     *
     * @param startMark - start
     * @return the directive value
     */
    private fun scanTagDirectiveHandle(startMark: Mark?): String {
        // See the specification for details.
        val value = scanTagHandle("directive", startMark)
        val c = reader.peek()
        if (c != ' '.code) {
            val s = Character.toChars(c).concatToString()
            throw ScannerException(
                problem = DIRECTIVE_PREFIX,
                problemMark = startMark,
                context = "expected ' ', but found $s($c)",
                contextMark = reader.getMark(),
            )
        }
        return value
    }

    /**
     * Scan a `%TAG` directive's prefix. This is YAML's `ns-tag-prefix`.
     */
    private fun scanTagDirectivePrefix(startMark: Mark?): String {
        // See the specification for details.
        val value = scanTagUri("directive", CharConstants.URI_CHARS_FOR_TAG_PREFIX, startMark)
        val c = reader.peek()
        if (CharConstants.NULL_BL_LINEBR.hasNo(c)) {
            val s = Character.toChars(c).concatToString()
            throw ScannerException(
                problem = DIRECTIVE_PREFIX,
                problemMark = startMark,
                context = "expected ' ', but found $s($c)",
                contextMark = reader.getMark(),
            )
        }
        return value
    }

    private fun scanDirectiveIgnoredLine(startMark: Mark?): CommentToken? {
        // See the specification for details.
        while (reader.peek() == ' '.code) {
            reader.forward()
        }
        var commentToken: CommentToken? = null
        if (reader.peek() == '#'.code) {
            val comment = scanComment(CommentType.IN_LINE)
            if (settings.parseComments) {
                commentToken = comment
            }
        }
        val c = reader.peek()
        if (scanLineBreak() == null && c != 0) {
            val s = Character.toChars(c).concatToString()
            throw ScannerException(
                problem = DIRECTIVE_PREFIX,
                problemMark = startMark,
                context = "expected a comment or a line break, but found $s($c)",
                contextMark = reader.getMark(),
            )
        }
        return commentToken
    }

    /**
     * ```text
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
            val s = Character.toChars(c).concatToString()
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
            val s = Character.toChars(c).concatToString()
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
     * Scan a Tag property. A Tag property may be specified in one of three ways: `c-verbatim-tag`,
     * `c-ns-shorthand-tag`, or `c-ns-non-specific-tag`
     *
     * `c-verbatim-tag` takes the form `!<ns-uri-char></ns-uri-char>+>` and must be delivered verbatim (as-is) to the
     * application. In particular, verbatim tags are not subject to tag resolution.
     *
     * `c-ns-shorthand-tag` is a valid tag handle followed by a non-empty suffix. If the tag handle is a
     * `c-primary-tag-handle` (`!`) then the suffix must have all exclamation marks properly URI-escaped
     * (`%21`); otherwise, the string will look like a named tag handle: `!foo!bar` would be interpreted
     * as (`handle="!foo!", suffix="bar"`).
     *
     * `c-ns-non-specific-tag` is always a lone `!`; this is only useful for plain scalars, where its
     * specification means that the scalar MUST be resolved to have type `tag:yaml.org,2002:str`.
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
                val s = Character.toChars(c).concatToString()
                throw ScannerException(
                    problem = "while scanning a tag",
                    problemMark = startMark,
                    context = "expected '>', but found '$s' ($c)",
                    contextMark = reader.getMark(),
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
            val s = Character.toChars(c).concatToString()
            throw ScannerException(
                problem = "while scanning a tag",
                problemMark = startMark,
                context = "expected ' ', but found '$s' ($c)",
                contextMark = reader.getMark(),
            )
        }
        val value = TagTuple((handle), suffix)
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
        val minIndent = (indent + 1).coerceAtLeast(1)
        var breaks: String
        val maxIndent: Int
        val blockIndent: Int
        var endMark: Mark?
        when (val chompingIncrement = chomping.increment) {
            null -> {
                // increment (block indent) must be detected in the first non-empty line.
                val brme = scanBlockScalarIndentation()
                breaks = brme.breaks
                maxIndent = brme.maxIndent
                endMark = brme.endMark
                blockIndent = minIndent.coerceAtLeast(maxIndent)
            }

            else -> {
                // increment is explicit
                blockIndent = minIndent + chompingIncrement - 1
                val brme = scanBlockScalarBreaks(blockIndent)
                breaks = brme.breaks
                endMark = brme.endMark
            }
        }
        var lineBreak: String? = null
        // Scan the inner part of the block scalar.
        if (reader.column < blockIndent && indent != reader.column) {
            // it means that there is indent, but less than expected
            // fix S98Z - Block scalar with more spaces than first content line
            throw ScannerException(
                problem = "while scanning a block scalar",
                problemMark = startMark,
                context = " the leading empty lines contain more spaces ($blockIndent) than the first non-empty line.",
                contextMark = reader.getMark(),
            )
        }
        while (reader.column == blockIndent && reader.peek() != 0) {
            stringBuilder.append(breaks)
            val leadingNonSpace = reader.peek().toChar() !in " \t"
            var length = 0
            while (CharConstants.NULL_OR_LINEBR.hasNo(reader.peek(length))) {
                length++
            }
            stringBuilder.append(reader.prefixForward(length))
            lineBreak = scanLineBreak()
            val brme = scanBlockScalarBreaks(blockIndent)
            breaks = brme.breaks
            endMark = brme.endMark
            if (reader.column == blockIndent && reader.peek() != 0) {

                // Unfortunately, folding rules are ambiguous.
                // This is the folding according to the specification:
                if (
                    style == ScalarStyle.FOLDED
                    && "\n" == lineBreak
                    && leadingNonSpace
                    && reader.peek().toChar() !in " \t"
                ) {
                    if (breaks.isEmpty()) {
                        stringBuilder.append(" ")
                    }
                } else {
                    stringBuilder.append(lineBreak ?: "")
                }
            } else {
                break
            }
        }
        // Chomp the tail.
        if (chomping.addExistingFinalLineBreak) {
            // add the final line break (if exists !) TODO find out if to add anyway
            stringBuilder.append(lineBreak ?: "")
        }
        if (chomping.retainTrailingEmptyLines) {
            // any trailing empty lines are considered to be part of the scalar’s content
            stringBuilder.append(breaks)
        }
        // We are done.
        val scalarToken = ScalarToken(stringBuilder.toString(), false, startMark, endMark, style)
        return makeTokenList(commentToken, scalarToken)
    }

    /**
     * Scan a block scalar indicator. The block scalar indicator includes two optional components,
     * which may appear in either order.
     *
     * A block indentation indicator is a non-zero digit describing the indentation level of the block
     * scalar to follow. This indentation is an additional number of spaces relative to the current
     * indentation level.
     *
     * A block chomping indicator is a + or -, selecting the chomping mode away from the default
     * (clip) to either -(strip) or +(keep).
     */
    private fun scanBlockScalarIndicators(startMark: Mark?): Chomping {
        // See the specification for details.
        val indicator: Int?
        val increment: Int?
        var c = reader.peek()
        if (c == '-'.code || c == '+'.code) {
            indicator = c
            reader.forward()
            c = reader.peek()
            if (c.toChar().isDigit()) {
                val incr = Character.toChars(c).concatToString().toInt()
                if (incr == 0) {
                    throw ScannerException(
                        problem = SCANNING_SCALAR,
                        problemMark = startMark,
                        context = "expected indentation indicator in the range 1-9, but found 0",
                        contextMark = reader.getMark(),
                    )
                }
                increment = incr
                reader.forward()
            } else {
                increment = null
            }
        } else if (c.toChar().isDigit()) {
            val incr = Character.toChars(c).concatToString().toInt()
            if (incr == 0) {
                throw ScannerException(
                    problem = SCANNING_SCALAR,
                    problemMark = startMark,
                    context = "expected indentation indicator in the range 1-9, but found 0",
                    contextMark = reader.getMark(),
                )
            }
            increment = incr
            reader.forward()
            c = reader.peek()
            if (c == '-'.code || c == '+'.code) {
                indicator = c
                reader.forward()
            } else {
                indicator = null
            }
        } else {
            increment = null
            indicator = null
        }
        c = reader.peek()
        if (CharConstants.NULL_BL_LINEBR.hasNo(c)) {
            val s = Character.toChars(c).concatToString()
            throw ScannerException(
                problem = SCANNING_SCALAR,
                problemMark = startMark,
                context = "expected chomping or indentation indicators, but found $s($c)",
                contextMark = reader.getMark(),
            )
        }
        return Chomping(indicator, increment)
            ?: throw IllegalArgumentException("Unexpected block chomping indicator: $indicator")
    }

    /**
     * Scan to the end of the line after a block scalar has been scanned; the only things that are
     * permitted at this time are comments and spaces.
     */
    private fun scanBlockScalarIgnoredLine(startMark: Mark?): CommentToken? {
        // See the specification for details.

        // Forward past any number of trailing spaces
        while (reader.peek() == ' '.code) {
            reader.forward()
        }

        // If a comment occurs, scan to just before the end of line.
        val commentToken: CommentToken? =
            if (reader.peek() == '#'.code) {
                scanComment(CommentType.IN_LINE)
            } else {
                null
            }
        // If the next character is not a null or line break, an error has
        // occurred.
        val c = reader.peek()
        if (scanLineBreak() == null && c != 0) {
            val s = Character.toChars(c).concatToString()
            throw ScannerException(
                problem = SCANNING_SCALAR,
                problemMark = startMark,
                context = "expected a comment or a line break, but found $s($c)",
                contextMark = reader.getMark(),
            )
        }
        return commentToken
    }

    /**
     * Scans for the indentation of a block scalar implicitly. This mechanism is used only if the
     * block did not explicitly state an indentation to be used.
     */
    private fun scanBlockScalarIndentation(): BreakIntentHolder {
        // See the specification for details.
        val chunks = StringBuilder()
        var maxIndent = 0
        var endMark: Mark? = reader.getMark()
        // Look ahead some number of lines until the first non-blank character
        // occurs; the determined indentation will be the maximum number of
        // leading spaces on any of these lines.
        while (CharConstants.LINEBR.has(reader.peek(), " \r")) {
            if (reader.peek() != ' '.code) {
                // If the character isn't a space, it must be some kind of
                // line-break; scan the line break and track it.
                chunks.append(scanLineBreak() ?: "")
                endMark = reader.getMark()
            } else {
                // If the character is a space, move forward to the next
                // character; if we surpass our previous maximum for indent
                // level, update that too.
                reader.forward()
                if (reader.column > maxIndent) {
                    maxIndent = reader.column
                }
            }
        }
        // Pass several results back together (Java 8 does not have records)
        return BreakIntentHolder(chunks.toString(), maxIndent, endMark)
    }

    private fun scanBlockScalarBreaks(indent: Int): BreakIntentHolder {
        // See the specification for details.
        val chunks = StringBuilder()
        var endMark: Mark? = reader.getMark()
        var col = reader.column
        // Scan for up to the expected indentation-level of spaces, then move
        // forward past that amount.
        while (col < indent && reader.peek() == ' '.code) {
            reader.forward()
            col++
        }
        // Consume one or more line breaks followed by any amount of spaces,
        // until we find something that isn't a line-break.
        while (true) {
            val lineBreak = scanLineBreak() ?: break
            chunks.append(lineBreak)
            endMark = reader.getMark()
            // Scan past up to (indent) spaces on the next line, then forward past them.
            col = reader.column
            while (col < indent && reader.peek() == ' '.code) {
                reader.forward()
                col++
            }
        }
        // Return both the assembled intervening string and the end-mark.
        return BreakIntentHolder(chunks.toString(), -1, endMark)
    }

    /**
     * Scan a flow-style scalar. Flow scalars are presented in one of two forms; first, a flow scalar
     * may be a double-quoted string; second, a flow scalar may be a single-quoted string.
     *
     * ```text
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
    private fun scanFlowScalarNonSpaces(doubleQuoted: Boolean, startMark: Mark?): String {
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
            } else if (doubleQuoted && c == '\''.code || !doubleQuoted && c.toChar() in "\"\\") {
                chunks.appendCodePoint(c)
                reader.forward()
            } else if (doubleQuoted && c == '\\'.code) {
                reader.forward()
                c = reader.peek()
                if (!Character.isSupplementaryCodePoint(c) && c.toChar() in CharConstants.ESCAPE_REPLACEMENTS) {
                    // The character is one of the single-replacement
                    // types; these are replaced with a literal character
                    // from the mapping.
                    chunks.append(CharConstants.ESCAPE_REPLACEMENTS[c.toChar()])
                    reader.forward()
                } else if (!Character.isSupplementaryCodePoint(c) && c.toChar() in CharConstants.ESCAPE_CODES) {
                    // The character is a multi-digit escape sequence, with
                    // length defined by the value in the ESCAPE_CODES map.
                    length = CharConstants.ESCAPE_CODES[c.toChar()]!!
                    reader.forward()
                    val hex = reader.prefix(length)
                    if (NOT_HEXA.containsMatchIn(hex)) {
                        throw ScannerException(
                            problem = "while scanning a double-quoted scalar",
                            problemMark = startMark,
                            context = "expected escape sequence of $length hexadecimal numbers, but found: $hex",
                            contextMark = reader.getMark(),
                        )
                    }
                    try {
                        val decimal = hex.toInt(16)
                        val unicode = Character.toChars(decimal)
                        chunks.append(unicode)
                        reader.forward(length)
                    } catch (e: IllegalArgumentException) {
                        throw ScannerException(
                            problem = "while scanning a double-quoted scalar",
                            problemMark = startMark,
                            context = "found unknown escape character $hex",
                            contextMark = reader.getMark(),
                        )
                    }
                } else if (scanLineBreak() != null) {
                    chunks.append(scanFlowScalarBreaks(startMark))
                } else {
                    val s = Character.toChars(c).concatToString()
                    throw ScannerException(
                        problem = "while scanning a double-quoted scalar",
                        problemMark = startMark,
                        context = "found unknown escape character $s($c)",
                        contextMark = reader.getMark(),
                    )
                }
            } else {
                return chunks.toString()
            }
        }
    }

    private fun scanFlowScalarSpaces(startMark: Mark?): String {
        // See the specification for details.
        var length = 0
        // Scan through any number of whitespace (space, tab) characters, consuming them.
        while (reader.peek(length).toChar() in " \t") {
            length++
        }
        val whitespaces = reader.prefixForward(length)
        if (reader.peek() == 0) {
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
            if (lineBreakOpt != null) {
                val breaks = scanFlowScalarBreaks(startMark)
                if ("\n" != lineBreakOpt) {
                    append(lineBreakOpt)
                } else if (breaks.isEmpty()) {
                    append(" ")
                }
                append(breaks)
            } else {
                append(whitespaces)
            }
        }
    }

    private fun scanFlowScalarBreaks(startMark: Mark?): String {
        // See the specification for details.
        val chunks = StringBuilder()
        while (true) {
            // Instead of checking indentation, we check for document separators.
            val prefix = reader.prefix(3)
            if (
                ("---" == prefix || "..." == prefix)
                && CharConstants.NULL_BL_T_LINEBR.has(reader.peek(3))
            ) {
                throw ScannerException(
                    problem = "while scanning a quoted scalar",
                    problemMark = startMark,
                    context = "found unexpected document separator",
                    contextMark = reader.getMark(),
                )
            }
            // Scan past any number of spaces and tabs, ignoring them
            while (reader.peek().toChar() in " \t") {
                reader.forward()
            }
            // If we stopped at a line break, add that;
            // otherwise, end the loop
            val lineBreakOpt = scanLineBreak() ?: break
            chunks.append(lineBreakOpt)
        }

        // return the assembled set of scalar breaks
        return chunks.toString()
    }

    /**
     * Scan a plain scalar.
     *
     * See the specification for details. We add an additional restriction for the flow context: plain
     * scalars in the flow context cannot contain `,`, `:` and `?`. We also keep track of the
     * `allow_simple_key` flag here. Indentation rules are loosened for the flow context.
     */
    private fun scanPlain(): Token {
        val chunks = StringBuilder()
        val startMark: Mark? = reader.getMark()
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
                    || isFlowContext() && c.toChar() in ",[]{}"
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
     * Helper for [scanPlainSpaces] method when comments are enabled.
     * The ensures that blank lines and comments following a multi-line plain token are not swallowed
     * up
     */
    private fun atEndOfPlain(): Boolean {
        // peak ahead to find end of whitespaces and the column at which it occurs
        var wsLength = 0
        var wsColumn = reader.column
        while(true) {
            val c = reader.peek(wsLength)
            if (c == 0 || !CharConstants.NULL_BL_T_LINEBR.has(c)) {
                break
            } else {
                wsLength++
                if (
                    !CharConstants.LINEBR.has(c)
                    && (c != '\r'.code || reader.peek(wsLength + 1) != '\n'.code)
                    && c != 0xFEFF
                ) {
                    wsColumn++
                } else {
                    wsColumn = 0
                }
            }
        }

        // if we see, a comment or end of string or change decrease in indent, we are done
        // Do not chomp end of lines and blanks, they will be handled by the main loop.
        if (
            reader.peek(wsLength) == '#'.code
            || reader.peek(wsLength + 1) == 0
            || isBlockContext() && wsColumn < indent
        ) {
            return true
        }

        // if we see, after the space, a key-value followed by a ':', we are done
        // Do not chomp end of lines and blanks, they will be handled by the main loop.
        if (isBlockContext()) {
            var extra = 1
            while (true) {
                val c = reader.peek(wsLength + extra)
                if (c == 0 || CharConstants.NULL_BL_T_LINEBR.has(c)) {
                    break
                } else if (c == ':'.code) {
                    val nextC = reader.peek(wsLength + extra + 1)
                    if (CharConstants.NULL_BL_T_LINEBR.has(nextC)) {
                        return true
                    }
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
        val lineBreak = scanLineBreak() ?: return whitespaces

        allowSimpleKey = true
        var prefix = reader.prefix(3)
        if ("---" == prefix || "..." == prefix && CharConstants.NULL_BL_T_LINEBR.has(reader.peek(3))) {
            return ""
        } else if (settings.parseComments && atEndOfPlain()) {
            return ""
        } else {
            val breaks = StringBuilder()
            while (true) {
                if (reader.peek() == ' '.code) {
                    reader.forward()
                } else {
                    val lbOpt = scanLineBreak()
                    if (lbOpt != null) {
                        breaks.append(lbOpt)
                        prefix = reader.prefix(3)
                        if ("---" == prefix || "..." == prefix && CharConstants.NULL_BL_T_LINEBR.has(
                                reader.peek(3)
                            )
                        ) {
                            return ""
                        }
                    } else {
                        break
                    }
                }
            }
            return when {
                "\n" != lineBreak -> lineBreak + breaks
                breaks.isEmpty()  -> " "
                else              -> breaks.toString()
            }
        }
    }

    /**
     * Scan a Tag handle. A Tag handle takes one of three forms:
     *
     * ```text
     * "!" (c-primary-tag-handle)
     * "!!" (ns-secondary-tag-handle)
     * "!(name)!" (c-named-tag-handle)
     * ```
     *
     * Where (name) must be formatted as an `ns-word-char`.
     *
     * ```text
     * See the specification for details.
     * For some strange reasons, the specification does not allow '_' in
     * tag handles. I have allowed it anyway.
     * ```
     */
    private fun scanTagHandle(name: String, startMark: Mark?): String {
        var c = reader.peek()
        if (c != '!'.code) {
            val s = Character.toChars(c).concatToString()
            throw ScannerException(
                problem = SCANNING_PREFIX + name,
                problemMark = startMark,
                context = "expected '!', but found $s($c)",
                contextMark = reader.getMark(),
            )
        }
        // Look for the next '!' in the stream, stopping if we hit a
        // non-word-character. If the first character is a space, then the
        // tag-handle is a c-primary-tag-handle ('!').
        var length = 1
        c = reader.peek(length)
        if (c != ' '.code) {
            // Scan through 0+ alphabetic characters.
            // According to the specification, these should be
            // ns-word-char only, which prohibits '_'. This might be a
            // candidate for a configuration option.
            while (CharConstants.ALPHA.has(c)) {
                length++
                c = reader.peek(length)
            }
            // Found the next non-word-char. If this is not a space and not an
            // '!', then this is an error, as the tag-handle was specified as:
            // !(name) or similar; the trailing '!' is missing.
            if (c != '!'.code) {
                reader.forward(length)
                val s = Character.toChars(c).concatToString()
                throw ScannerException(
                    SCANNING_PREFIX + name, startMark,
                    "expected '!', but found $s($c)", reader.getMark(),
                )
            }
            length++
        }
        return reader.prefixForward(length)
    }

    /**
     * Scan a Tag URI. This scanning is valid for both local and global tag directives, because both
     * appear to be valid URIs as far as scanning is concerned. The difference may be distinguished
     * later, in parsing. This method will scan for ns-uri-char*, which covers both cases.
     *
     * This method performs no verification that the scanned URI conforms to any particular kind of
     * URI specification.
     */
    private fun scanTagUri(name: String, range: CharConstants, startMark: Mark?): String {
        // See the specification for details.
        // Note: we do not check if URI is well-formed.
        val chunks = StringBuilder()
        // Scan through accepted URI characters, which includes the standard
        // URI characters, plus the start-escape character ('%'). When we get
        // to a start-escape, scan the escaped sequence, then return.
        var length = 0
        var c = reader.peek(length)
        while (range.has(c)) {
            if (c == '%'.code) {
                chunks.append(reader.prefixForward(length))
                length = 0
                chunks.append(scanUriEscapes(name, startMark))
            } else {
                length++
            }
            c = reader.peek(length)
        }
        // Consume the last "chunk", which would not otherwise be consumed by
        // the loop above.
        if (length != 0) {
            chunks.append(reader.prefixForward(length))
        }
        if (chunks.isEmpty()) {
            // If no URI was found, an error has occurred.
            val s = Character.toChars(c).concatToString()
            throw ScannerException(
                problem = SCANNING_PREFIX + name,
                problemMark = startMark,
                context = "expected URI, but found $s($c)",
                contextMark = reader.getMark(),
            )
        }
        return chunks.toString()
    }

    /**
     * Scan a sequence of `%`-escaped URI escape codes and convert them into a String representing the
     * unescaped values.
     *
     * This method fails for more than 256 bytes' worth of URI-encoded characters in a row. Is this
     * possible? Is this a use-case?
     */
    private fun scanUriEscapes(name: String, startMark: Mark?): String {
        // First, look ahead to see how many URI-escaped characters we should
        // expect, so we can use the correct buffer size.
        var length = 1
        while (reader.peek(length * 3) == '%'.code) {
            length++
        }
        // See the specification for details.
        // URIs containing 16 and 32 bit Unicode characters are encoded in UTF-8,
        // and then each octet is written as a separate character.
        val beginningMark = reader.getMark()
        val buff = Buffer()
        while (reader.peek() == '%'.code) {
            reader.forward()
            try {
                val code = reader.prefix(2).toInt(16)
                buff.writeByte(code)
            } catch (nfe: NumberFormatException) {
                val c1 = reader.peek()
                val s1 = Character.toChars(c1).concatToString()
                val c2 = reader.peek(1)
                val s2 = Character.toChars(c2).concatToString()
                throw ScannerException(
                    problem = SCANNING_PREFIX + name,
                    problemMark = startMark,
                    context = "expected URI escape sequence of 2 hexadecimal numbers, but found $s1($c1) and $s2($c2)",
                    contextMark = reader.getMark(),
                )
            }
            reader.forward(2)
        }
        buff.flush()
        try {
            return UriEncoder.decode(buff)
        } catch (e: CharacterCodingException) {
            throw ScannerException(
                problem = SCANNING_PREFIX + name,
                problemMark = startMark,
                context = "expected URI in UTF-8: " + e.message,
                contextMark = beginningMark,
            )
        }
    }

    /**
     * Scan a line break, transforming:
     *
     * ```text
     * '\r\n'   : '\n'
     * '\r'     : '\n'
     * '\n'     : '\n'
     * '\x85'   : '\n'
     * '\u2028' : '\u2028'
     * '\u2029  : '\u2029'
     * default : ''
     * ```
     * @returns transformed character, or `null`` if no line break detected
     */
    private fun scanLineBreak(): String? {
        val c = reader.peek()
        if (c == '\r'.code || c == '\n'.code || c == '\u0085'.code) {
            if (c == '\r'.code && '\n'.code == reader.peek(1)) {
                reader.forward(2)
            } else {
                reader.forward()
            }
            return "\n"
        } else if (c == '\u2028'.code || c == '\u2029'.code) {
            reader.forward()
            return Character.toChars(c).concatToString()
        }
        return null
    }

    /**
     * Ignore Comment token if they are null, or Comments should not be parsed
     *
     * @param tokens - token types
     * @return tokens to be used
     */
    private fun makeTokenList(vararg tokens: Token?): List<Token> {
        val notNullTokens = tokens.filterNotNull()

        return if (!settings.parseComments) {
            notNullTokens.filter { token -> token !is CommentToken }
        } else {
            notNullTokens
        }
    }

    //endregion

    //endregion

    companion object {

        private const val DIRECTIVE_PREFIX = "while scanning a directive"
        private const val EXPECTED_ALPHA_ERROR_PREFIX =
            "expected alphabetic or numeric character, but found "
        private const val SCANNING_SCALAR = "while scanning a block scalar"
        private const val SCANNING_PREFIX = "while scanning a "

        /**
         * A regular expression matching characters which are not in the hexadecimal set (`0-9`, `A-F`, `a-f`).
         */
        private val NOT_HEXA = Regex("[^0-9A-Fa-f]")
    }
}


private class BreakIntentHolder(
    val breaks: String,
    val maxIndent: Int,
    val endMark: Mark?,
)

//region Chomping

/**
 * Chomping controls how final line breaks and trailing empty lines are interpreted.
 * YAML provides three chomping methods:
 *
 * * [Chomping.Strip]
 * * [Chomping.Clip]
 * * [Chomping.Keep]
 */
private sealed interface Chomping {
    val increment: Int?

    /** Whether to add the final line break (if it exists) */
    val addExistingFinalLineBreak: Boolean

    /** Whether any trailing empty lines are considered to be part of the scalar’s content */
    val retainTrailingEmptyLines: Boolean

    /**
     * Clipping is the default behavior used if no explicit chomping indicator is specified.
     * In this case, the final line break character is preserved in the scalar’s content.
     * However, any trailing empty lines are excluded from the scalar’s content.
     */
    @JvmInline
    value class Clip(override val increment: Int?) : Chomping {
        override val addExistingFinalLineBreak: Boolean get() = true
        override val retainTrailingEmptyLines: Boolean get() = false
    }

    /**
     * Stripping is specified by the `-` chomping indicator.
     * In this case, the final line break and any trailing empty lines are excluded from the scalar’s content.
     */
    @JvmInline
    value class Strip(override val increment: Int?) : Chomping {
        override val addExistingFinalLineBreak: Boolean get() = false
        override val retainTrailingEmptyLines: Boolean get() = false
    }

    /**
     * Keeping is specified by the `+` chomping indicator.
     * In this case, the final line break and any trailing empty lines are considered to be part of the scalar’s
     * content. These additional lines are not subject to folding.
     */
    @JvmInline
    value class Keep(override val increment: Int?) : Chomping {
        override val addExistingFinalLineBreak: Boolean get() = true
        override val retainTrailingEmptyLines: Boolean get() = true
    }
}


/**
 * Create a new [Chomping] instance based on the [indicatorCodePoint].
 *
 * @returns `null` if [indicatorCodePoint] is unknown, else a matching [Chomping] instance.
 */
private fun Chomping(
    indicatorCodePoint: Int?,
    increment: Int?,
): Chomping? {
    return when (indicatorCodePoint) {
        '+'.code -> Chomping.Keep(increment)
        '-'.code -> Chomping.Strip(increment)
        null     -> Chomping.Clip(increment)
        else     -> null
    }
}

//endregion
