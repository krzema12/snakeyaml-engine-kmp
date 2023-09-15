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
package org.snakeyaml.engine.v2.parser

import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.common.Anchor
import org.snakeyaml.engine.v2.common.FlowStyle
import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.common.SpecVersion
import org.snakeyaml.engine.v2.events.*
import org.snakeyaml.engine.v2.exceptions.Mark
import org.snakeyaml.engine.v2.exceptions.ParserException
import org.snakeyaml.engine.v2.exceptions.YamlEngineException
import org.snakeyaml.engine.v2.nodes.Tag
import org.snakeyaml.engine.v2.scanner.Scanner
import org.snakeyaml.engine.v2.scanner.ScannerImpl
import org.snakeyaml.engine.v2.scanner.StreamReader
import org.snakeyaml.engine.v2.tokens.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * ```
 * # The following YAML grammar is LL(1) and is parsed by a recursive descent parser.
 *
 * stream            ::= STREAM-START implicit_document? explicit_document* STREAM-END
 * implicit_document ::= block_node DOCUMENT-END*
 * explicit_document ::= DIRECTIVE* DOCUMENT-START block_node? DOCUMENT-END*
 * block_node_or_indentless_sequence ::=
 *                       ALIAS
 *                       | properties (block_content | indentless_block_sequence)?
 *                       | block_content
 *                       | indentless_block_sequence
 * block_node        ::= ALIAS
 *                       | properties block_content?
 *                       | block_content
 * flow_node         ::= ALIAS
 *                       | properties flow_content?
 *                       | flow_content
 * properties        ::= TAG ANCHOR? | ANCHOR TAG?
 * block_content     ::= block_collection | flow_collection | SCALAR
 * flow_content      ::= flow_collection | SCALAR
 * block_collection  ::= block_sequence | block_mapping
 * flow_collection   ::= flow_sequence | flow_mapping
 * block_sequence    ::= BLOCK-SEQUENCE-START (BLOCK-ENTRY block_node?)* BLOCK-END
 * indentless_sequence   ::= (BLOCK-ENTRY block_node?)+
 * block_mapping     ::= BLOCK-MAPPING_START
 *                       ((KEY block_node_or_indentless_sequence?)?
 *                       (VALUE block_node_or_indentless_sequence?)?)*
 *                       BLOCK-END
 * flow_sequence     ::= FLOW-SEQUENCE-START
 *                       (flow_sequence_entry FLOW-ENTRY)*
 *                       flow_sequence_entry?
 *                       FLOW-SEQUENCE-END
 * flow_sequence_entry   ::= flow_node | KEY flow_node? (VALUE flow_node?)?
 * flow_mapping      ::= FLOW-MAPPING-START
 *                       (flow_mapping_entry FLOW-ENTRY)*
 *                       flow_mapping_entry?
 *                       FLOW-MAPPING-END
 * flow_mapping_entry    ::= flow_node | KEY flow_node? (VALUE flow_node?)?
 * #
 * FIRST sets:
 * #
 * stream: { STREAM-START }
 * explicit_document: { DIRECTIVE DOCUMENT-START }
 * implicit_document: FIRST(block_node)
 * block_node: { ALIAS TAG ANCHOR SCALAR BLOCK-SEQUENCE-START BLOCK-MAPPING-START FLOW-SEQUENCE-START FLOW-MAPPING-START }
 * flow_node: { ALIAS ANCHOR TAG SCALAR FLOW-SEQUENCE-START FLOW-MAPPING-START }
 * block_content: { BLOCK-SEQUENCE-START BLOCK-MAPPING-START FLOW-SEQUENCE-START FLOW-MAPPING-START SCALAR }
 * flow_content: { FLOW-SEQUENCE-START FLOW-MAPPING-START SCALAR }
 * block_collection: { BLOCK-SEQUENCE-START BLOCK-MAPPING-START }
 * flow_collection: { FLOW-SEQUENCE-START FLOW-MAPPING-START }
 * block_sequence: { BLOCK-SEQUENCE-START }
 * block_mapping: { BLOCK-MAPPING-START }
 * block_node_or_indentless_sequence: { ALIAS ANCHOR TAG SCALAR BLOCK-SEQUENCE-START BLOCK-MAPPING-START FLOW-SEQUENCE-START FLOW-MAPPING-START BLOCK-ENTRY }
 * indentless_sequence: { ENTRY }
 * flow_collection: { FLOW-SEQUENCE-START FLOW-MAPPING-START }
 * flow_sequence: { FLOW-SEQUENCE-START }
 * flow_mapping: { FLOW-MAPPING-START }
 * flow_sequence_entry: { ALIAS ANCHOR TAG SCALAR FLOW-SEQUENCE-START FLOW-MAPPING-START KEY }
 * flow_mapping_entry: { ALIAS ANCHOR TAG SCALAR FLOW-SEQUENCE-START FLOW-MAPPING-START KEY }
 * ```
 *
 * Since writing a recursive-descendant parser is a straightforward task, we do not give many comments here.
 *
 * @param[settings] tokenizer
 */
class ParserImpl(
    private val settings: LoadSettings,
    private val scanner: Scanner,
) : Parser {

    private val states: ArrayDeque<Production> = ArrayDeque(100)
    private val marksStack: ArrayDeque<Mark?> = ArrayDeque(100)

    /** parsed event */
    private var currentEvent: Event? = null

    private var state: Production? = ParseStreamStart() // prepare the next state

    private var directiveTags: MutableMap<String, String> = DEFAULT_TAGS.toMutableMap()

    constructor(
        settings: LoadSettings,
        reader: StreamReader,
    ) : this(settings, ScannerImpl(settings, reader))

    /** Check the ID of the next event. */
    override fun checkEvent(choice: Event.ID): Boolean {
        peekEvent()
        return currentEvent?.eventId == choice
    }

    /** Get the next event (and keep it). Produce the event if not yet present. */
    override fun peekEvent(): Event {
        produce()
        return currentEvent ?: throw NoSuchElementException("No more Events found.")
    }

    /** Consume the event (get the next event and removed it). */
    override fun next(): Event {
        val value = peekEvent()
        currentEvent = null
        return value
    }

    /**
     * Produce the event if not yet present.
     *
     * @returns `true` if there is another event
     */
    override fun hasNext(): Boolean {
        produce()
        return currentEvent != null
    }

    private fun produce() {
        if (currentEvent == null) {
            state?.let { production -> currentEvent = production.produce() }
        }
    }

    private fun produceCommentEvent(token: CommentToken): CommentEvent {
        // state = state, that no change in state
        return CommentEvent(token.commentType, token.value, token.startMark, token.endMark)
    }

    private fun processDirectives(): VersionTagsTuple {
        var yamlSpecVersion: SpecVersion? = null
        val tagHandles = mutableMapOf<String, String>()

        while (scanner.checkToken(Token.ID.Directive)) {
            val directive = scanner.next() as DirectiveToken

            if (directive.value == null) continue

            when (directive.value) {
                is DirectiveToken.TagDirective  -> {
                    val (handle, prefix) = directive.value
                    if (tagHandles.containsKey(handle)) {
                        throw ParserException("duplicate tag handle $handle", directive.startMark)
                    }
                    tagHandles[handle] = prefix
                }

                is DirectiveToken.YamlDirective -> {
                    if (yamlSpecVersion != null) {
                        throw ParserException("found duplicate YAML directive", directive.startMark)
                    }
                    val (major, minor) = directive.value
                    yamlSpecVersion = settings.versionFunction(SpecVersion(major, minor))
                }
            }
        }
        val detectedTagHandles = mutableMapOf<String, String>()
        if (tagHandles.isNotEmpty()) {
            // copy from tagHandles
            detectedTagHandles.putAll(tagHandles)
        }
        for ((key, value) in DEFAULT_TAGS) {
            // do not overwrite re-defined tags
            if (!tagHandles.containsKey(key)) {
                tagHandles[key] = value
            }
        }
        directiveTags = tagHandles
        // data for the event (no default tags added)
        return VersionTagsTuple(yamlSpecVersion, detectedTagHandles)
    }

    private fun parseFlowNode(): Event = parseNode(block = false, indentlessSequence = false)

    private fun parseBlockNodeOrIndentlessSequence(): Event = parseNode(block = true, indentlessSequence = true)

    private fun parseNode(block: Boolean, indentlessSequence: Boolean): Event {
        var startMark: Mark? = null
        var endMark: Mark? = null
        var tagMark: Mark? = null
        if (scanner.checkToken(Token.ID.Alias)) {
            val token = scanner.next() as AliasToken
            state = states.removeLast()
            return AliasEvent(token.value, token.startMark, token.endMark)
        } else {
            val anchor: Anchor?
            val tagTupleValue: TagTuple?
            if (scanner.checkToken(Token.ID.Anchor)) {
                val token = scanner.next() as AnchorToken
                startMark = token.startMark
                endMark = token.endMark
                anchor = token.value
                if (scanner.checkToken(Token.ID.Tag)) {
                    val tagToken = scanner.next() as TagToken
                    tagMark = tagToken.startMark
                    endMark = tagToken.endMark
                    tagTupleValue = tagToken.value
                } else {
                    tagTupleValue = null
                }
            } else if (scanner.checkToken(Token.ID.Tag)) {
                val tagToken = scanner.next() as TagToken
                startMark = tagToken.startMark
                tagMark = startMark
                endMark = tagToken.endMark
                tagTupleValue = tagToken.value
                if (scanner.checkToken(Token.ID.Anchor)) {
                    val token = scanner.next() as AnchorToken
                    endMark = token.endMark
                    anchor = token.value
                } else {
                    anchor = null
                }
            } else {
                tagTupleValue = null
                anchor = null
            }
            val tag: String?
            if (tagTupleValue != null) {
                val handle = tagTupleValue.handle
                tag = if (handle != null) {
                    if (!directiveTags.containsKey(handle)) {
                        throw ParserException(
                            problem = "found undefined tag handle $handle",
                            contextMark = startMark,
                            context = "while parsing a node",
                            problemMark = tagMark,
                        )
                    }
                    directiveTags[handle] + tagTupleValue.suffix
                } else {
                    tagTupleValue.suffix
                }
            } else {
                tag = null
            }
            if (startMark == null) {
                startMark = scanner.peekToken().startMark
                endMark = startMark
            }
            val implicit = tag == null
            return when {
                indentlessSequence && scanner.checkToken(Token.ID.BlockEntry) -> {

                    endMark = scanner.peekToken().endMark
                    state = ParseIndentlessSequenceEntryKey()
                    SequenceStartEvent(anchor, tag, implicit, FlowStyle.BLOCK, startMark, endMark)
                }

                scanner.checkToken(Token.ID.Scalar)                           -> {
                    val token = scanner.next() as ScalarToken
                    endMark = token.endMark
                    val implicitValues = when {
                        token.plain && tag == null -> ImplicitTuple(plain = true, nonPlain = false)
                        tag == null                -> ImplicitTuple(plain = false, nonPlain = true)
                        else                       -> ImplicitTuple(plain = false, nonPlain = false)
                    }
                    state = states.removeLast()
                    ScalarEvent(anchor, tag, implicitValues, token.value, token.style, startMark, endMark)
                }

                scanner.checkToken(Token.ID.FlowSequenceStart)                -> {
                    endMark = scanner.peekToken().endMark
                    state = ParseFlowSequenceFirstEntry()
                    SequenceStartEvent(anchor, tag, implicit, FlowStyle.FLOW, startMark, endMark)
                }

                scanner.checkToken(Token.ID.FlowMappingStart)                 -> {
                    endMark = scanner.peekToken().endMark
                    state = ParseFlowMappingFirstKey()
                    MappingStartEvent(anchor, tag, implicit, FlowStyle.FLOW, startMark, endMark)
                }

                block && scanner.checkToken(Token.ID.BlockSequenceStart)      -> {
                    endMark = scanner.peekToken().startMark
                    state = ParseBlockSequenceFirstEntry()
                    SequenceStartEvent(anchor, tag, implicit, FlowStyle.BLOCK, startMark, endMark)
                }

                block && scanner.checkToken(Token.ID.BlockMappingStart)       -> {
                    endMark = scanner.peekToken().startMark
                    state = ParseBlockMappingFirstKey()
                    MappingStartEvent(anchor, tag, implicit, FlowStyle.BLOCK, startMark, endMark)
                }

                anchor != null || tag != null                                 -> {
                    // Empty scalars are allowed even if a tag or an anchor is specified.
                    state = states.removeLast()
                    val nonPlainImplicit = ImplicitTuple(implicit, false)
                    ScalarEvent(anchor, tag, nonPlainImplicit, "", ScalarStyle.PLAIN, startMark, endMark)
                }

                else                                                          -> {
                    val token = scanner.peekToken()
                    throw ParserException(
                        problem = "expected the node content, but found '" + token.tokenId + "'",
                        contextMark = startMark,
                        context = "while parsing a " + (if (block) "block" else "flow") + " node",
                        problemMark = token.startMark,
                    )
                }
            }
        }
    }


    /**
     * ```text
     * block_mapping     ::= BLOCK-MAPPING_START
     * ((KEY block_node_or_indentless_sequence?)?
     * (VALUE block_node_or_indentless_sequence?)?)*
     * BLOCK-END
     * ```
     */
    private fun processEmptyScalar(mark: Mark?): Event {
        return ScalarEvent(
            anchor = null,
            tag = null,
            implicit = ImplicitTuple(plain = true, nonPlain = false),
            value = "",
            scalarStyle = ScalarStyle.PLAIN,
            startMark = mark,
            endMark = mark,
        )
    }

    private fun markPop(): Mark? = marksStack.removeLast()

    private fun markPush(mark: Mark?): Unit = marksStack.addLast(mark)

    private inner class ParseStreamStart : Production {
        override fun produce(): Event {
            // Parse the stream start.
            val token = scanner.next() as StreamStartToken
            val event: Event = StreamStartEvent(token.startMark, token.endMark)
            // Prepare the next state.
            state = ParseImplicitDocumentStart()
            return event
        }
    }

    private inner class ParseImplicitDocumentStart : Production {
        override fun produce(): Event {
            if (scanner.checkToken(Token.ID.Comment)) {
                state = ParseImplicitDocumentStart()
                return produceCommentEvent(scanner.next() as CommentToken)
            }
            return if (!scanner.checkToken(Token.ID.Directive, Token.ID.DocumentStart, Token.ID.StreamEnd)) {
                // Parse an implicit document.
                val token = scanner.peekToken()
                val startMark = token.startMark
                // Prepare the next state.
                states.addLast(ParseDocumentEnd())
                state = ParseBlockNode()
                DocumentStartEvent(false, null, emptyMap(), startMark, startMark)
            } else {
                // explicit document detected
                ParseDocumentStart().produce()
            }
        }
    }

    private inner class ParseDocumentStart : Production {
        override fun produce(): Event {
            if (scanner.checkToken(Token.ID.Comment)) {
                state = ParseDocumentStart()
                return produceCommentEvent((scanner.next() as CommentToken))
            }
            // Parse any extra document end indicators.
            while (scanner.checkToken(Token.ID.DocumentEnd)) {
                scanner.next()
            }
            if (scanner.checkToken(Token.ID.Comment)) {
                state = ParseDocumentStart()
                return produceCommentEvent((scanner.next() as CommentToken))
            }
            // Parse an explicit document.
            if (!scanner.checkToken(Token.ID.StreamEnd)) {
                scanner.resetDocumentIndex()
                val tuple = processDirectives()
                while (scanner.checkToken(Token.ID.Comment)) {
                    scanner.next()
                }
                return if (!scanner.checkToken(Token.ID.StreamEnd)) {
                    if (!scanner.checkToken(Token.ID.DocumentStart)) {
                        throw ParserException(
                            problem = "expected '<document start>', but found '${scanner.peekToken().tokenId}'",
                            contextMark = scanner.peekToken().startMark,
                        )
                    }
                    val token = scanner.next()
                    val startMark = token.startMark
                    val endMark = token.endMark
                    states.addLast(ParseDocumentEnd())
                    state = ParseDocumentContent()
                    DocumentStartEvent(true, tuple.specVersion, tuple.tags, startMark, endMark)
                } else {
                    throw ParserException(
                        problem = "expected '<document start>', but found '${scanner.peekToken().tokenId}'",
                        contextMark = scanner.peekToken().startMark,
                    )
                }
            }
            // Parse the end of the stream.
            val token = scanner.next() as StreamEndToken
            if (!states.isEmpty()) {
                throw YamlEngineException("Unexpected end of stream. States left: $states")
            }
            if (!marksStack.isEmpty()) {
                throw YamlEngineException("Unexpected end of stream. Marks left: $marksStack")
            }
            state = null
            return StreamEndEvent(token.startMark, token.endMark)
        }
    }

    private inner class ParseDocumentEnd : Production {
        override fun produce(): Event {
            // Parse the document end.
            var token = scanner.peekToken()
            val startMark: Mark? = token.startMark
            val endMark: Mark?
            val explicit: Boolean
            if (scanner.checkToken(Token.ID.DocumentEnd)) {
                token = scanner.next()
                endMark = token.endMark
                explicit = true
            } else if (scanner.checkToken(Token.ID.Directive)) {
                throw ParserException(
                    problem = "expected '<document end>' before directives, but found '${scanner.peekToken().tokenId}'",
                    contextMark = scanner.peekToken().startMark,
                )
            } else {
                endMark = token.startMark
                explicit = false
            }
            directiveTags.clear() // directive tags do not survive between the documents

            // Prepare the next state.
            state = ParseDocumentStart()
            return DocumentEndEvent(explicit, startMark, endMark)
        }
    }

    private inner class ParseDocumentContent : Production {
        override fun produce(): Event {
            return if (scanner.checkToken(Token.ID.Comment)) {
                state = ParseDocumentContent()
                return produceCommentEvent((scanner.next() as CommentToken))
            } else if (
                scanner.checkToken(Token.ID.Directive, Token.ID.DocumentStart, Token.ID.DocumentEnd, Token.ID.StreamEnd)
            ) {
                state = states.removeLast()
                val event = processEmptyScalar(scanner.peekToken().startMark)
                event
            } else {
                ParseBlockNode().produce()
            }
        }
    }

    /**
     * ```text
     *  block_node_or_indentless_sequence ::= ALIAS
     *                | properties (block_content | indentless_block_sequence)?
     *                | block_content
     *                | indentless_block_sequence
     *  block_node    ::= ALIAS
     *                    | properties block_content?
     *                    | block_content
     *  flow_node     ::= ALIAS
     *                    | properties flow_content?
     *                    | flow_content
     *  properties    ::= TAG ANCHOR? | ANCHOR TAG?
     *  block_content     ::= block_collection | flow_collection | SCALAR
     *  flow_content      ::= flow_collection | SCALAR
     *  block_collection  ::= block_sequence | block_mapping
     *  flow_collection   ::= flow_sequence | flow_mapping
     * ```
     */
    private inner class ParseBlockNode : Production {
        override fun produce(): Event = parseNode(block = true, indentlessSequence = false)
    }

    // indentless_sequence ::= (BLOCK-ENTRY block_node?)+
    private inner class ParseBlockSequenceFirstEntry : Production {
        override fun produce(): Event {
            val token = scanner.next()
            markPush(token.startMark)
            return ParseBlockSequenceEntryKey().produce()
        }
    }

    private inner class ParseBlockSequenceEntryKey : Production {
        override fun produce(): Event {
            return when {
                scanner.checkToken(Token.ID.Comment)    -> {
                    state = ParseBlockSequenceEntryKey()
                    produceCommentEvent((scanner.next() as CommentToken))
                }

                scanner.checkToken(Token.ID.BlockEntry) -> {
                    val token = scanner.next() as BlockEntryToken
                    ParseBlockSequenceEntryValue(token).produce()
                }

                !scanner.checkToken(Token.ID.BlockEnd)  -> {
                    val token = scanner.peekToken()
                    throw ParserException(
                        problem = "expected <block end>, but found '${token.tokenId}'",
                        contextMark = markPop(),
                        context = "while parsing a block collection",
                        problemMark = token.startMark,
                    )
                }

                else                                    -> {
                    val token = scanner.next()
                    state = states.removeLast()
                    markPop()
                    SequenceEndEvent(token.startMark, token.endMark)
                }
            }
        }
    }

    private inner class ParseBlockSequenceEntryValue(
        private val token: BlockEntryToken,
    ) : Production {
        override fun produce(): Event {
            if (scanner.checkToken(Token.ID.Comment)) {
                state = ParseBlockSequenceEntryValue(token)
                return produceCommentEvent((scanner.next() as CommentToken))
            }
            return if (!scanner.checkToken(Token.ID.BlockEntry, Token.ID.BlockEnd)) {
                states.addLast(ParseBlockSequenceEntryKey())
                ParseBlockNode().produce()
            } else {
                state = ParseBlockSequenceEntryKey()
                processEmptyScalar(token.endMark)
            }
        }
    }

    private inner class ParseIndentlessSequenceEntryKey : Production {
        override fun produce(): Event {
            if (scanner.checkToken(Token.ID.Comment)) {
                state = ParseIndentlessSequenceEntryKey()
                return produceCommentEvent((scanner.next() as CommentToken))
            }
            if (scanner.checkToken(Token.ID.BlockEntry)) {
                val token = scanner.next() as BlockEntryToken
                return ParseIndentlessSequenceEntryValue(token).produce()
            }
            val token = scanner.peekToken()
            state = states.removeLast()
            return SequenceEndEvent(token.startMark, token.endMark)
        }
    }

    private inner class ParseIndentlessSequenceEntryValue(
        private val token: BlockEntryToken,
    ) : Production {
        override fun produce(): Event {
            return if (scanner.checkToken(Token.ID.Comment)) {
                state = ParseIndentlessSequenceEntryValue(token)
                produceCommentEvent((scanner.next() as CommentToken))
            } else if (!scanner.checkToken(Token.ID.BlockEntry, Token.ID.Key, Token.ID.Value, Token.ID.BlockEnd)) {
                states.addLast(ParseIndentlessSequenceEntryKey())
                ParseBlockNode().produce()
            } else {
                state = ParseIndentlessSequenceEntryKey()
                processEmptyScalar(token.endMark)
            }
        }
    }

    private inner class ParseBlockMappingFirstKey : Production {
        override fun produce(): Event {
            val token = scanner.next()
            markPush(token.startMark)
            return ParseBlockMappingKey().produce()
        }
    }

    private inner class ParseBlockMappingKey : Production {
        override fun produce(): Event {
            return when {
                scanner.checkToken(Token.ID.Comment)   -> {
                    state = ParseBlockMappingKey()
                    produceCommentEvent(scanner.next() as CommentToken)
                }

                scanner.checkToken(Token.ID.Key)       -> {
                    val token = scanner.next()
                    if (!scanner.checkToken(Token.ID.Key, Token.ID.Value, Token.ID.BlockEnd)) {
                        states.addLast(ParseBlockMappingValue())
                        parseBlockNodeOrIndentlessSequence()
                    } else {
                        state = ParseBlockMappingValue()
                        processEmptyScalar(token.endMark)
                    }
                }

                !scanner.checkToken(Token.ID.BlockEnd) -> {
                    val token = scanner.peekToken()
                    throw ParserException(
                        problem = "expected <block end>, but found '${token.tokenId}'",
                        contextMark = markPop(),
                        context = "while parsing a block mapping",
                        problemMark = token.startMark,
                    )
                }

                else                                   -> {
                    val token = scanner.next()
                    state = states.removeLast()
                    markPop()
                    MappingEndEvent(token.startMark, token.endMark)
                }
            }
        }
    }

    private inner class ParseBlockMappingValue : Production {
        override fun produce(): Event {
            if (scanner.checkToken(Token.ID.Value)) {
                val token = scanner.next()
                return if (scanner.checkToken(Token.ID.Comment)) {
                    val p: Production = ParseBlockMappingValueComment()
                    state = p
                    p.produce()
                } else if (!scanner.checkToken(Token.ID.Key, Token.ID.Value, Token.ID.BlockEnd)) {
                    states.addLast(ParseBlockMappingKey())
                    parseBlockNodeOrIndentlessSequence()
                } else {
                    state = ParseBlockMappingKey()
                    processEmptyScalar(token.endMark)
                }
            } else if (scanner.checkToken(Token.ID.Scalar)) {
                states.addLast(ParseBlockMappingKey())
                return parseBlockNodeOrIndentlessSequence()
            }
            state = ParseBlockMappingKey()
            val token = scanner.peekToken()
            return processEmptyScalar(token.startMark)
        }
    }

    private inner class ParseBlockMappingValueComment : Production {

        private val tokens: ArrayDeque<CommentToken> = ArrayDeque()

        override fun produce(): Event {
            return if (scanner.checkToken(Token.ID.Comment)) {
                tokens.add(scanner.next() as CommentToken)
                produce()
            } else if (!scanner.checkToken(Token.ID.Key, Token.ID.Value, Token.ID.BlockEnd)) {
                if (tokens.isNotEmpty()) {
                    produceCommentEvent(tokens.removeFirst())
                } else {
                    states.addLast(ParseBlockMappingKey())
                    parseBlockNodeOrIndentlessSequence()
                }
            } else {
                state = ParseBlockMappingValueCommentList(tokens)
                processEmptyScalar(scanner.peekToken().startMark)
            }
        }
    }

    private inner class ParseBlockMappingValueCommentList(
        private val tokens: ArrayDeque<CommentToken>,
    ) : Production {
        override fun produce(): Event {
            return if (tokens.isNotEmpty()) {
                produceCommentEvent(tokens.removeFirst())
            } else {
                ParseBlockMappingKey().produce()
            }
        }
    }

    /**
     * <pre>
     * flow_sequence     ::= FLOW-SEQUENCE-START
     *                       (flow_sequence_entry FLOW-ENTRY)*
     *                       flow_sequence_entry?
     *                       FLOW-SEQUENCE-END
     * flow_sequence_entry   ::= flow_node | KEY flow_node? (VALUE flow_node?)?
     * Note that while production rules for both flow_sequence_entry and
     * flow_mapping_entry are equal, their interpretations are different.
     * For `flow_sequence_entry`, the part `KEY flow_node? (VALUE flow_node?)?`
     * generate an inline mapping (set syntax).
     * </pre>
     */
    private inner class ParseFlowSequenceFirstEntry : Production {
        override fun produce(): Event {
            val token = scanner.next()
            markPush(token.startMark)
            return ParseFlowSequenceEntry(true).produce()
        }
    }

    private inner class ParseFlowSequenceEntry(
        private val first: Boolean,
    ) : Production {
        override fun produce(): Event {
            if (scanner.checkToken(Token.ID.Comment)) {
                state = ParseFlowSequenceEntry(first)
                return produceCommentEvent((scanner.next() as CommentToken))
            }
            if (!scanner.checkToken(Token.ID.FlowSequenceEnd)) {
                if (!first) {
                    if (scanner.checkToken(Token.ID.FlowEntry)) {
                        scanner.next()
                        if (scanner.checkToken(Token.ID.Comment)) {
                            state = ParseFlowSequenceEntry(true)
                            return produceCommentEvent((scanner.next() as CommentToken))
                        }
                    } else {
                        val token = scanner.peekToken()
                        throw ParserException(
                            problem = "expected ',' or ']', but got ${token.tokenId}",
                            contextMark = markPop(),
                            context = "while parsing a flow sequence",
                            problemMark = token.startMark,
                        )
                    }
                }
                if (scanner.checkToken(Token.ID.Key)) {
                    val token = scanner.peekToken()
                    state = ParseFlowSequenceEntryMappingKey()
                    return MappingStartEvent(
                        null,
                        null,
                        true,
                        FlowStyle.FLOW,
                        token.startMark,
                        token.endMark,
                    )
                } else if (!scanner.checkToken(Token.ID.FlowSequenceEnd)) {
                    states.addLast(ParseFlowSequenceEntry(false))
                    return parseFlowNode()
                }
            }
            val token = scanner.next()
            state = if (!scanner.checkToken(Token.ID.Comment)) {
                states.removeLast()
            } else {
                ParseFlowEndComment()
            }
            markPop()
            return SequenceEndEvent(token.startMark, token.endMark)
        }
    }

    private inner class ParseFlowEndComment : Production {
        override fun produce(): Event {
            val event = produceCommentEvent(scanner.next() as CommentToken)
            if (!scanner.checkToken(Token.ID.Comment)) {
                state = states.removeLast()
            }
            return event
        }
    }

    private inner class ParseFlowSequenceEntryMappingKey : Production {
        override fun produce(): Event {
            val token = scanner.next()
            return if (!scanner.checkToken(Token.ID.Value, Token.ID.FlowEntry, Token.ID.FlowSequenceEnd)) {
                states.addLast(ParseFlowSequenceEntryMappingValue())
                parseFlowNode()
            } else {
                state = ParseFlowSequenceEntryMappingValue()
                processEmptyScalar(token.endMark)
            }
        }
    }

    private inner class ParseFlowSequenceEntryMappingValue : Production {
        override fun produce(): Event {
            return if (scanner.checkToken(Token.ID.Value)) {
                val token = scanner.next()
                if (!scanner.checkToken(Token.ID.FlowEntry, Token.ID.FlowSequenceEnd)) {
                    states.addLast(ParseFlowSequenceEntryMappingEnd())
                    parseFlowNode()
                } else {
                    state = ParseFlowSequenceEntryMappingEnd()
                    processEmptyScalar(token.endMark)
                }
            } else {
                state = ParseFlowSequenceEntryMappingEnd()
                val token = scanner.peekToken()
                processEmptyScalar(token.startMark)
            }
        }
    }

    private inner class ParseFlowSequenceEntryMappingEnd : Production {
        override fun produce(): Event {
            state = (ParseFlowSequenceEntry(false))
            val token = scanner.peekToken()
            return MappingEndEvent(token.startMark, token.endMark)
        }
    }

    /**
     * <pre>
     *   flow_mapping  ::= FLOW-MAPPING-START
     *          (flow_mapping_entry FLOW-ENTRY)*
     *          flow_mapping_entry?
     *          FLOW-MAPPING-END
     *   flow_mapping_entry    ::= flow_node | KEY flow_node? (VALUE flow_node?)?
     * </pre>
     */
    private inner class ParseFlowMappingFirstKey : Production {
        override fun produce(): Event {
            val token = scanner.next()
            markPush(token.startMark)
            return ParseFlowMappingKey(true).produce()
        }
    }

    private inner class ParseFlowMappingKey(
        private val first: Boolean,
    ) : Production {
        override fun produce(): Event {
            if (!scanner.checkToken(Token.ID.FlowMappingEnd)) {
                if (!first) {
                    if (scanner.checkToken(Token.ID.FlowEntry)) {
                        scanner.next()
                    } else {
                        val token = scanner.peekToken()
                        throw ParserException(
                            problem = "expected ',' or '}', but got ${token.tokenId}",
                            contextMark = markPop(),
                            context = "while parsing a flow mapping",
                            problemMark = token.startMark,
                        )
                    }
                }
                if (scanner.checkToken(Token.ID.Key)) {
                    val token = scanner.next()
                    return if (!scanner.checkToken(Token.ID.Value, Token.ID.FlowEntry, Token.ID.FlowMappingEnd)) {
                        states.addLast(ParseFlowMappingValue())
                        parseFlowNode()
                    } else {
                        state = ParseFlowMappingValue()
                        processEmptyScalar(token.endMark)
                    }
                } else if (!scanner.checkToken(Token.ID.FlowMappingEnd)) {
                    states.addLast(ParseFlowMappingEmptyValue())
                    return parseFlowNode()
                }
            }
            val token = scanner.next()
            markPop()
            state = if (!scanner.checkToken(Token.ID.Comment)) {
                states.removeLast()
            } else {
                ParseFlowEndComment()
            }
            return MappingEndEvent(token.startMark, token.endMark)
        }
    }

    private inner class ParseFlowMappingValue : Production {
        override fun produce(): Event {
            return if (scanner.checkToken(Token.ID.Value)) {
                val token = scanner.next()
                if (!scanner.checkToken(Token.ID.FlowEntry, Token.ID.FlowMappingEnd)) {
                    states.addLast(ParseFlowMappingKey(false))
                    parseFlowNode()
                } else {
                    state = (ParseFlowMappingKey(false))
                    processEmptyScalar(token.endMark)
                }
            } else {
                state = (ParseFlowMappingKey(false))
                val token = scanner.peekToken()
                processEmptyScalar(token.startMark)
            }
        }
    }

    private inner class ParseFlowMappingEmptyValue : Production {
        override fun produce(): Event {
            state = (ParseFlowMappingKey(false))
            return processEmptyScalar(scanner.peekToken().startMark)
        }
    }

    companion object {
        private val DEFAULT_TAGS: Map<String, String> = mapOf(
            "!" to "!",
            "!!" to Tag.PREFIX,
        )
    }
}
