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
package it.krzeminski.snakeyaml.engine.kmp.emitter

import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.StreamDataWriter
import it.krzeminski.snakeyaml.engine.kmp.comments.CommentEventsCollector
import it.krzeminski.snakeyaml.engine.kmp.comments.CommentLine
import it.krzeminski.snakeyaml.engine.kmp.comments.CommentType
import it.krzeminski.snakeyaml.engine.kmp.common.Anchor
import it.krzeminski.snakeyaml.engine.kmp.common.CharConstants
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.common.SpecVersion
import it.krzeminski.snakeyaml.engine.kmp.events.*
import it.krzeminski.snakeyaml.engine.kmp.exceptions.EmitterException
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException
import it.krzeminski.snakeyaml.engine.kmp.internal.utils.Character
import it.krzeminski.snakeyaml.engine.kmp.internal.utils.codePointAt
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader
import kotlin.jvm.JvmField

/**
 * ```text
 * Emitter expects events obeying the following grammar:
 * stream ::= STREAM-START document* STREAM-END
 * document ::= DOCUMENT-START node DOCUMENT-END
 * node ::= SCALAR | sequence | mapping
 * sequence ::= SEQUENCE-START node* SEQUENCE-END
 * mapping ::= MAPPING-START (node node)* MAPPING-END
 * ```
 */
class Emitter(
    private val opts: DumpSettings,
    private val stream: StreamDataWriter,
) : Emitable {

    /** [Emitter] is a state machine with a stack of states to handle nested structures. */
    private val states: ArrayDeque<EmitterState> = ArrayDeque(100)

    /** current state */
    private var state: EmitterState = ExpectStreamStart()

    /** The event queue */
    private val events: ArrayDeque<Event> = ArrayDeque(100)

    /** Current event */
    private var event: Event? = null

    /** The stack of previous indents */
    private val indents: ArrayDeque<Int?> = ArrayDeque(100)

    /** The current indentation level. Can be `null` to choose the best */
    private var indent: Int? = null

    /** Flow level */
    private var flowLevel = 0

    //region Contexts
    private var rootContext = false
    private var mappingContext = false
    private var simpleKeyContext = false
    //endregion

    //region Characteristics of the last emitted character
    /** current position of the last emitted character */
    private var column = 0

    /** is the last emitted character whitespace? */
    private var whitespace = true

    /** is the last emitted character an indention character (indentation space, `-`, `?`, or `:`)? */
    private var indention = true
    //endregion

    /** Whether the document requires an explicit document indicator */
    private var openEnded = false

    //region Formatting details.
    private val canonical: Boolean = opts.isCanonical

    /** pretty print flow by adding extra line breaks */
    private val multiLineFlow: Boolean = opts.isMultiLineFlow

    private val allowUnicode: Boolean = opts.isUseUnicodeEncoding
    private val bestIndent: Int = if (opts.indent in VALID_INDENT_RANGE) opts.indent else DEFAULT_INDENT
    private val indicatorIndent: Int get() = opts.indicatorIndent
    private val indentWithIndicator: Boolean get() = opts.indentWithIndicator
    private val bestWidth: Int = if (opts.width > this.bestIndent * 2) opts.width else DEFAULT_WIDTH
    private val bestLineBreak: String get() = opts.bestLineBreak
    private val splitLines: Boolean get() = opts.isSplitLines
    private val maxSimpleKeyLength: Int get() = opts.maxSimpleKeyLength
    private val emitComments: Boolean get() = opts.dumpComments
    //endregion

    /** Tag prefixes. */
    private var tagPrefixes: MutableMap<String?, String> = mutableMapOf()

    private var preparedAnchor: Anchor? = null
    private var preparedTag: String? = null

    /** Scalar analysis */
    private var analysis: ScalarAnalysis? = null

    /** Scalar style */
    private var scalarStyle: ScalarStyle? = null

    //region Comment processing
    private val blockCommentsCollector = CommentEventsCollector(events, CommentType.BLANK_LINE, CommentType.BLOCK)
    private val inlineCommentsCollector = CommentEventsCollector(events, CommentType.IN_LINE)
    //endregion

    override fun emit(event: Event) {
        this.events.add(event)
        while (!needMoreEvents()) {
            this.event = this.events.removeFirst()
            this.state.expect()
            this.event = null
        }
    }

    // In some cases, we wait for a few next events before emitting.
    private fun needMoreEvents(): Boolean {
        if (events.isEmpty()) {
            return true
        }
        val iter = events.iterator()
        var event = iter.next()
        while (event is CommentEvent) {
            if (!iter.hasNext()) {
                return true
            }
            event = iter.next()
        }
        return when (event) {
            is DocumentStartEvent -> needEvents(iter, 1)

            is SequenceStartEvent -> needEvents(iter, 2)

            is MappingStartEvent  -> needEvents(iter, 3)

            is StreamStartEvent   -> needEvents(iter, 2)

            is StreamEndEvent     -> false

            else                  ->
                // To collect any comment events
                if (emitComments) needEvents(iter, 1) else false
        }
    }

    private fun needEvents(iter: Iterator<Event>, count: Int): Boolean {
        var level = 0
        var actualCount = 0
        for (event in iter) {
            if (event is CommentEvent) {
                continue
            }
            actualCount++
            when (event) {
                is DocumentStartEvent, is CollectionStartEvent -> level++
                is DocumentEndEvent, is CollectionEndEvent     -> level--
                is StreamEndEvent                              -> level = -1
            }
            if (level < 0) {
                return false
            }
        }
        return actualCount < count
    }

    private fun increaseIndent(
        isFlow: Boolean = false,
        indentless: Boolean = false,
    ) {
        indents.addLast(indent)
        if (indent == null) {
            indent = if (isFlow) bestIndent else 0
        } else if (!indentless) {
            indent = indent!! + bestIndent
        }
    }

    //region States

    //region Stream handlers.
    private inner class ExpectStreamStart : EmitterState {
        override fun expect() {
            if (event?.eventId == Event.ID.StreamStart) {
                writeStreamStart()
                state = ExpectFirstDocumentStart()
            } else {
                throw EmitterException("expected StreamStartEvent, but got $event")
            }
        }
    }

    private inner class ExpectNothing : EmitterState {
        override fun expect(): Unit = throw EmitterException("expecting nothing, but got $event")
    }

    //endregion

    //region Document handlers.
    private inner class ExpectFirstDocumentStart : EmitterState {
        override fun expect(): Unit = ExpectDocumentStart(true).expect()
    }

    private inner class ExpectDocumentStart(private val first: Boolean) : EmitterState {
        override fun expect() {
            if (event?.eventId == Event.ID.DocumentStart) {
                val ev = event as DocumentStartEvent
                handleDocumentStartEvent(ev)
                state = ExpectDocumentRoot()
            } else if (event?.eventId == Event.ID.StreamEnd) {
                writeStreamEnd()
                state = ExpectNothing()
            } else if (event is CommentEvent) {
                blockCommentsCollector.collectEvents(event)
                writeBlockComment()
                // state = state; remains unchanged
            } else {
                throw EmitterException("expected DocumentStartEvent, but got $event")
            }
        }

        private fun handleDocumentStartEvent(ev: DocumentStartEvent) {
            if ((ev.specVersion != null || ev.tags.isNotEmpty()) && openEnded) {
                writeIndicator(indicator = "...", needWhitespace = true)
                writeIndent()
            }
            if (ev.specVersion != null) {
                writeVersionDirective(prepareVersion(ev.specVersion))
            }
            tagPrefixes = DEFAULT_TAG_PREFIXES.toMutableMap()
            if (ev.tags.isNotEmpty()) {
                handleTagDirectives(ev.tags)
            }
            val implicit = first
                && !ev.explicit
                && !canonical
                && ev.specVersion == null
                && ev.tags.isEmpty()
                && !checkEmptyDocument()
            if (!implicit) {
                writeIndent()
                writeIndicator(indicator = "---", needWhitespace = true)
                if (canonical) {
                    writeIndent()
                }
            }
        }

        private fun handleTagDirectives(tags: Map<String, String>) {
            for ((handle, prefix) in tags) {
                tagPrefixes[prefix] = handle
                checkTagHandle(handle)
                checkTagPrefix(prefix)
                writeTagDirective(handle, prefix)
            }
        }

        private fun checkTagHandle(handle: String) {
            when {
                handle.isEmpty()                                  ->
                    throw EmitterException("tag handle must not be empty")

                !(handle.startsWith('!') && handle.endsWith('!')) ->
                    throw EmitterException("tag handle must start and end with '!': $handle")

                handle != "!" && !HANDLE_FORMAT.matches(handle)   ->
                    throw EmitterException("invalid character in the tag handle: $handle")
            }
        }

        private fun checkTagPrefix(prefix: String) {
            if (prefix.isEmpty()) {
                throw EmitterException("tag prefix must not be empty")
            }
        }

        private fun checkEmptyDocument(): Boolean {
            if (event?.eventId != Event.ID.DocumentStart || events.isEmpty()) {
                return false
            }
            val nextEvent = events.first()
            if (nextEvent.eventId == Event.ID.Scalar) {
                val e = nextEvent as ScalarEvent
                return e.anchor == null
                    && e.tag == null
                    && e.value.isEmpty()
            }
            return false
        }
    }

    private inner class ExpectDocumentEnd : EmitterState {
        override fun expect() {
            event = blockCommentsCollector.collectEventsAndPoll(event)
            writeBlockComment()
            if (event?.eventId == Event.ID.DocumentEnd) {
                writeIndent()
                if ((event as DocumentEndEvent).isExplicit) {
                    writeIndicator(indicator = "...", needWhitespace = true)
                    writeIndent()
                }
                flushStream()
                state = ExpectDocumentStart(false)
            } else {
                throw EmitterException("expected DocumentEndEvent, but got $event")
            }
        }
    }

    private inner class ExpectDocumentRoot : EmitterState {
        override fun expect() {
            event = blockCommentsCollector.collectEventsAndPoll(event)
            if (!blockCommentsCollector.isEmpty()) {
                writeBlockComment()
                if (event is DocumentEndEvent) {
                    ExpectDocumentEnd().expect()
                    return
                }
            }
            states.addLast(ExpectDocumentEnd())
            expectNode(root = true)
        }
    }

    //endregion

    //region Node handlers.

    private fun expectNode(
        root: Boolean = false,
        mapping: Boolean = false,
        simpleKey: Boolean = false,
    ) {
        rootContext = root
        mappingContext = mapping
        simpleKeyContext = simpleKey
        when (event?.eventId) {
            Event.ID.Alias                                                 -> {
                expectAlias(simpleKey)
            }

            Event.ID.Scalar, Event.ID.SequenceStart, Event.ID.MappingStart -> {
                processAnchor()
                processTag()
                handleNodeEvent(event!!.eventId)
            }

            else                                                           -> {
                throw EmitterException("expected NodeEvent, but got ${event?.eventId}")
            }
        }
    }

    private fun handleNodeEvent(id: Event.ID) {
        when (id) {
            Event.ID.Scalar        -> expectScalar()
            Event.ID.SequenceStart ->
                if (flowLevel != 0
                    || canonical
                    || (event as SequenceStartEvent).isFlow()
                    || checkEmptySequence()
                ) {
                    expectFlowSequence()
                } else {
                    expectBlockSequence()
                }

            Event.ID.MappingStart  ->
                if (flowLevel != 0
                    || canonical
                    || (event as MappingStartEvent).isFlow()
                    || checkEmptyMapping()
                ) {
                    expectFlowMapping()
                } else {
                    expectBlockMapping()
                }

            else                   -> error("Unexpected Event.ID $id")
        }
    }

    /**
     * @param simpleKey true when this is the alias for a simple key
     */
    private fun expectAlias(simpleKey: Boolean) {
        state = if (event is AliasEvent) {
            processAlias(simpleKey)
            states.removeLast()
        } else {
            throw EmitterException("Expecting Alias.")
        }
    }

    private fun expectScalar() {
        increaseIndent(isFlow = true)
        processScalar(event as ScalarEvent)
        indent = indents.removeLastOrNull()
        state = states.removeLast()
    }

    //endregion

    //region Flow sequence handlers.

    private fun expectFlowSequence() {
        writeIndicator(indicator = "[", needWhitespace = true, whitespace = true)
        flowLevel++
        increaseIndent(isFlow = true)
        if (multiLineFlow) {
            writeIndent()
        }
        state = ExpectFirstFlowSequenceItem()
    }

    private inner class ExpectFirstFlowSequenceItem : EmitterState {
        override fun expect() {
            if (event?.eventId == Event.ID.SequenceEnd) {
                indent = indents.removeLastOrNull()
                flowLevel--
                writeIndicator(indicator = "]")
                inlineCommentsCollector.collectEvents()
                writeInlineComments()
                state = states.removeLast()
            } else if (event is CommentEvent) {
                blockCommentsCollector.collectEvents(event)
                writeBlockComment()
            } else {
                if (canonical || column > bestWidth && splitLines || multiLineFlow) {
                    writeIndent()
                }
                states.addLast(ExpectFlowSequenceItem())
                expectNode()
                event = inlineCommentsCollector.collectEvents(event)
                writeInlineComments()
            }
        }
    }

    private inner class ExpectFlowSequenceItem : EmitterState {
        override fun expect() {
            if (event?.eventId == Event.ID.SequenceEnd) {
                indent = indents.removeLastOrNull()
                flowLevel--
                if (canonical) {
                    writeIndicator(indicator = ",")
                    writeIndent()
                } else if (multiLineFlow) {
                    writeIndent()
                }
                writeIndicator(indicator = "]")
                inlineCommentsCollector.collectEvents()
                writeInlineComments()
                if (multiLineFlow) {
                    writeIndent()
                }
                state = states.removeLast()
            } else if (event is CommentEvent) {
                event = blockCommentsCollector.collectEvents(event)
            } else {
                writeIndicator(indicator = ",")
                writeBlockComment()
                if (canonical || column > bestWidth && splitLines || multiLineFlow) {
                    writeIndent()
                }
                states.addLast(ExpectFlowSequenceItem())
                expectNode()
                event = inlineCommentsCollector.collectEvents(event)
                writeInlineComments()
            }
        }
    }

    //endregion

    //region Flow mapping handlers.

    private fun expectFlowMapping() {
        writeIndicator(indicator = "{", needWhitespace = true, whitespace = true)
        flowLevel++
        increaseIndent(isFlow = true)
        if (multiLineFlow) {
            writeIndent()
        }
        state = ExpectFirstFlowMappingKey()
    }

    private inner class ExpectFirstFlowMappingKey : EmitterState {
        override fun expect() {
            event = blockCommentsCollector.collectEventsAndPoll(event)
            writeBlockComment()
            if (event?.eventId == Event.ID.MappingEnd) {
                indent = indents.removeLastOrNull()
                flowLevel--
                writeIndicator(indicator = "}")
                inlineCommentsCollector.collectEvents()
                writeInlineComments()
                state = states.removeLast()
            } else {
                if (canonical || column > bestWidth && splitLines || multiLineFlow) {
                    writeIndent()
                }
                if (!canonical && checkSimpleKey()) {
                    states.addLast(ExpectFlowMappingSimpleValue())
                    expectNode(mapping = true, simpleKey = true)
                } else {
                    writeIndicator(indicator = "?", needWhitespace = true)
                    states.addLast(ExpectFlowMappingValue())
                    expectNode(mapping = true)
                }
            }

        }
    }

    private inner class ExpectFlowMappingKey : EmitterState {
        override fun expect() {
            if (event?.eventId == Event.ID.MappingEnd) {
                indent = indents.removeLastOrNull()
                flowLevel--
                if (canonical) {
                    writeIndicator(indicator = ",")
                    writeIndent()
                }
                if (multiLineFlow) {
                    writeIndent()
                }
                writeIndicator(indicator = "}")
                inlineCommentsCollector.collectEvents()
                writeInlineComments()
                state = states.removeLast()
            } else {
                writeIndicator(indicator = ",")
                event = blockCommentsCollector.collectEventsAndPoll(event)
                writeBlockComment()
                if (canonical || column > bestWidth && splitLines || multiLineFlow) {
                    writeIndent()
                }
                if (!canonical && checkSimpleKey()) {
                    states.addLast(ExpectFlowMappingSimpleValue())
                    expectNode(mapping = true, simpleKey = true)
                } else {
                    writeIndicator(indicator = "?", needWhitespace = true)
                    states.addLast(ExpectFlowMappingValue())
                    expectNode(mapping = true)
                }
            }

        }
    }

    private inner class ExpectFlowMappingSimpleValue : EmitterState {
        override fun expect() {
            writeIndicator(indicator = ":")
            event = inlineCommentsCollector.collectEventsAndPoll(event)
            writeInlineComments()
            states.addLast(ExpectFlowMappingKey())
            expectNode(mapping = true)
            inlineCommentsCollector.collectEvents()
            writeInlineComments()
        }
    }

    private inner class ExpectFlowMappingValue : EmitterState {
        override fun expect() {
            if (canonical || column > bestWidth || multiLineFlow) {
                writeIndent()
            }
            writeIndicator(indicator = ":", needWhitespace = true)
            event = inlineCommentsCollector.collectEventsAndPoll(event)
            writeInlineComments()
            states.addLast(ExpectFlowMappingKey())
            expectNode(mapping = true)
            inlineCommentsCollector.collectEvents(event)
            writeInlineComments()

        }
    }
    //endregion

    //region Block sequence handlers.
    private fun expectBlockSequence() {
        val indentless = mappingContext && !indention
        increaseIndent(indentless = indentless)
        state = ExpectFirstBlockSequenceItem()
    }

    private inner class ExpectFirstBlockSequenceItem : EmitterState {
        override fun expect(): Unit = ExpectBlockSequenceItem(true).expect()
    }

    private inner class ExpectBlockSequenceItem(private val first: Boolean) : EmitterState {
        override fun expect() {
            if (!first && event?.eventId == Event.ID.SequenceEnd) {
                indent = indents.removeLastOrNull()
                state = states.removeLast()
            } else if (event is CommentEvent) {
                blockCommentsCollector.collectEvents(event)
            } else {
                writeIndent()
                if (!indentWithIndicator || first) {
                    writeWhitespace(indicatorIndent)
                }
                writeIndicator(indicator = "-", needWhitespace = true, indentation = true)
                if (indentWithIndicator && first) {
                    indent = indent!! + indicatorIndent
                }
                if (!blockCommentsCollector.isEmpty()) {
                    increaseIndent()
                    writeBlockComment()
                    if (event is ScalarEvent) {
                        analysis = analyzeScalar((event as ScalarEvent).value)
                        if (!analysis!!.empty || ((event as ScalarEvent).tag ?: "null") == Tag.STR.value) {
                            writeIndent()
                        }
                    }
                    indent = indents.removeLastOrNull()
                }
                states.addLast(ExpectBlockSequenceItem(false))
                expectNode()
                inlineCommentsCollector.collectEvents()
                writeInlineComments()
            }
        }
    }

    //endregion

    // region Block mapping handlers.

    private fun expectBlockMapping() {
        increaseIndent()
        state = ExpectFirstBlockMappingKey()
    }

    private inner class ExpectFirstBlockMappingKey : EmitterState {
        override fun expect() {
            ExpectBlockMappingKey(true).expect()
        }
    }

    private inner class ExpectBlockMappingKey(
        private val first: Boolean,
    ) : EmitterState {
        override fun expect() {

            event = blockCommentsCollector.collectEventsAndPoll(event)
            writeBlockComment()
            if (!first && event?.eventId == Event.ID.MappingEnd) {
                indent = indents.removeLastOrNull()
                state = states.removeLast()
            } else {
                writeIndent()
                if (checkSimpleKey()) {
                    states.addLast(ExpectBlockMappingSimpleValue())
                    expectNode(mapping = true, simpleKey = true)
                } else {
                    writeIndicator(indicator = "?", needWhitespace = true, indentation = true)
                    states.addLast(ExpectBlockMappingValue())
                    expectNode(mapping = true)
                }
            }
        }
    }

    private inner class ExpectBlockMappingSimpleValue : EmitterState {
        override fun expect() {
            writeIndicator(indicator = ":")
            event = inlineCommentsCollector.collectEventsAndPoll(event)
            if (!isFoldedOrLiteral(event!!)) {
                if (writeInlineComments()) {
                    increaseIndent(isFlow = true)
                    writeIndent()
                    indent = indents.removeLastOrNull()
                }
            }
            event = blockCommentsCollector.collectEventsAndPoll(event)
            if (!blockCommentsCollector.isEmpty()) {
                increaseIndent(isFlow = true)
                writeBlockComment()
                writeIndent()
                indent = indents.removeLastOrNull()
            }
            states.addLast(ExpectBlockMappingKey(false))
            expectNode(mapping = true)
            inlineCommentsCollector.collectEvents()
            writeInlineComments()
        }

        private fun isFoldedOrLiteral(event: Event): Boolean {
            return event is ScalarEvent
                && (event.scalarStyle == ScalarStyle.FOLDED || event.scalarStyle == ScalarStyle.LITERAL)
        }
    }

    private inner class ExpectBlockMappingValue : EmitterState {
        override fun expect() {
            writeIndent()
            writeIndicator(indicator = ":", needWhitespace = true, indentation = true)
            event = inlineCommentsCollector.collectEventsAndPoll(event)
            writeInlineComments()
            event = blockCommentsCollector.collectEventsAndPoll(event)
            writeBlockComment()
            states.addLast(ExpectBlockMappingKey(false))
            expectNode(mapping = true)
            inlineCommentsCollector.collectEvents(event)
            writeInlineComments()
        }
    }
    //endregion

    //region Checkers.

    private fun checkEmptySequence(): Boolean {
        return event?.eventId == Event.ID.SequenceStart
            && !events.isEmpty()
            && events.first().eventId == Event.ID.SequenceEnd
    }

    private fun checkEmptyMapping(): Boolean {
        return event?.eventId == Event.ID.MappingStart
            && !events.isEmpty()
            && events.first().eventId == Event.ID.MappingEnd
    }

    private fun checkSimpleKey(): Boolean {
        var length = 0
        if (event is NodeEvent) {
            val anchor = (event as NodeEvent).anchor
            if (anchor != null) {
                if (preparedAnchor == null) {
                    preparedAnchor = anchor
                }
                length += anchor.value.length
            }
        }
        val tag: String? =
            if (event?.eventId == Event.ID.Scalar) {
                (event as ScalarEvent).tag
            } else if (event is CollectionStartEvent) {
                (event as CollectionStartEvent).tag
            } else {
                null
            }
        if (tag != null) {
            if (preparedTag == null) {
                preparedTag = prepareTag(tag)
            }
            length += preparedTag!!.length
        }
        if (event?.eventId == Event.ID.Scalar) {
            if (analysis == null) {
                analysis = analyzeScalar((event as ScalarEvent).value)
            }
            length += analysis!!.scalar.length
        }
        return length < maxSimpleKeyLength
            && (
            event?.eventId == Event.ID.Alias
                || event?.eventId == Event.ID.Scalar
                && !analysis!!.empty
                && !analysis!!.multiline
                || checkEmptySequence()
                || checkEmptyMapping()
            )
    }

    //endregion

    //region Anchor, Tag, and Scalar processors.

    private fun processAnchorOrAlias(indicator: String, trailingWhitespace: Boolean) {
        val ev = event as NodeEvent
        val anchor: Anchor? = ev.anchor
        if (anchor != null) {
            if (preparedAnchor == null) {
                preparedAnchor = anchor
            }
            writeIndicator(indicator = indicator + anchor, needWhitespace = true)
        }
        preparedAnchor = null
        if (trailingWhitespace) {
            writeWhitespace(1)
        }
    }

    private fun processAnchor() {
        // no need for trailing space
        processAnchorOrAlias("&", false)
    }

    private fun processAlias(simpleKey: Boolean) {
        // because of ':' it needs to add trailing space for simple keys
        processAnchorOrAlias("*", simpleKey)
    }

    /**
     * Emit the tag for the current event
     */
    private fun processTag() {
        var tag: String?
        if (event?.eventId == Event.ID.Scalar) {
            val ev = event as ScalarEvent
            tag = ev.tag
            if (scalarStyle == null) {
                scalarStyle = chooseScalarStyle(ev)
            }
            // check when no tag is required
            if (
                (!canonical || tag == null)
                && ((scalarStyle == ScalarStyle.PLAIN && ev.implicit.canOmitTagInPlainScalar())
                        || (scalarStyle != ScalarStyle.PLAIN && ev.implicit.canOmitTagInNonPlainScalar()))
            ) {
                preparedTag = null
                return // no tag required
            } else if (ev.implicit.canOmitTagInPlainScalar() && tag == null) {
                tag = "!"
                preparedTag = null
            }
        } else {
            val ev = event as CollectionStartEvent
            tag = ev.tag
            if ((!canonical || tag == null) && ev.isImplicit()) {
                preparedTag = null
                return // no tag required
            }
        }
        if (tag == null) {
            throw EmitterException("tag is not specified")
        }
        val indicator = preparedTag ?: prepareTag(tag)
        writeIndicator(indicator = indicator, needWhitespace = true)
    }

    /**
     * Choose the scalar style based on the contents of the scalar and scalar style chosen by
     * Representer.
     *
     * @return [ScalarStyle] to apply for this scalar event
     */
    private fun chooseScalarStyle(ev: ScalarEvent): ScalarStyle? {
        if (analysis == null) {
            analysis = analyzeScalar(ev.value)
        }
        if (!ev.plain && ev.dQuoted || canonical) {
            return ScalarStyle.DOUBLE_QUOTED
        }
        if (ev.json && ev.tag == Tag.STR.value) {
            // special case for strings which are always double-quoted in JSON
            return ScalarStyle.DOUBLE_QUOTED;
        }
        if ((ev.plain || ev.json) && ev.implicit.canOmitTagInPlainScalar()) {
            if (!(simpleKeyContext && (analysis!!.empty || analysis!!.multiline))
                && (flowLevel != 0 && analysis!!.allowFlowPlain || flowLevel == 0 && analysis!!.allowBlockPlain)
            ) {
                return ScalarStyle.PLAIN
            }
        }
        if (ev.literal || ev.folded) {
            if (flowLevel == 0 && !simpleKeyContext && analysis!!.allowBlock) {
                return ev.scalarStyle
            }
        }
        if (ev.plain || ev.sQuoted) {
            if (analysis!!.allowSingleQuoted && !(simpleKeyContext && analysis!!.multiline)) {
                return ScalarStyle.SINGLE_QUOTED
            }
        }
        return ScalarStyle.DOUBLE_QUOTED
    }

    private fun processScalar(ev: ScalarEvent) {
        if (analysis == null) {
            analysis = analyzeScalar(ev.value)
        }
        val split = !simpleKeyContext && splitLines
        when (scalarStyle) {
            ScalarStyle.PLAIN         -> writePlain(analysis!!.scalar, split)
            ScalarStyle.DOUBLE_QUOTED -> writeDoubleQuoted(analysis!!.scalar, split)
            ScalarStyle.SINGLE_QUOTED -> writeSingleQuoted(analysis!!.scalar, split)
            ScalarStyle.FOLDED        -> writeFolded(analysis!!.scalar, split)
            ScalarStyle.LITERAL       -> writeLiteral(analysis!!.scalar)
            else                      -> throw YamlEngineException("Unexpected scalarStyle: $scalarStyle")
        }
        // reset scalar style for another scalar
        analysis = null
        scalarStyle = null
    }

    //endregion

    //region Analyzers.
    private fun prepareVersion(version: SpecVersion): String {
        if (version.major != 1) {
            throw EmitterException("unsupported YAML version: $version")
        }
        return version.representation
    }

    /**
     * Detect whether the tag starts with a standard handle and add ! when it does not
     *
     * @param tag - raw (complete tag)
     * @return formatted tag ready to emit
     */
    private fun prepareTag(tag: String): String {
        if (tag.isEmpty()) {
            throw EmitterException("tag must not be empty")
        } else if ("!" == tag) {
            return tag
        }
        val matchedPrefix = tagPrefixes.keys.firstOrNull { prefix ->
            // if tag starts with prefix and contains more than just prefix
            prefix != null
                && tag.startsWith(prefix)
                && ("!" == prefix || prefix.length < tag.length)
        }
        val handle: String?
        val suffix: String
        if (matchedPrefix != null) {
            handle = tagPrefixes[matchedPrefix]
            suffix = tag.substring(matchedPrefix.length)
        } else {
            handle = null
            suffix = tag
        }
        return if (handle != null) handle + suffix else "!<$suffix>"
    }

    private fun analyzeScalar(scalar: String): ScalarAnalysis {
        // Empty scalar is a special case.
        if (scalar.isEmpty()) {
            return ScalarAnalysis(
                scalar = scalar,
                empty = true,
                multiline = false,
                allowFlowPlain = false,
                allowBlockPlain = true,
                allowSingleQuoted = true,
                allowBlock = false,
            )
        }
        // Indicators and special characters.
        var blockIndicators = false
        var flowIndicators = false
        var lineBreaks = false
        var specialCharacters = false

        // Important whitespace combinations.
        var leadingSpace = false
        var leadingBreak = false
        var trailingSpace = false
        var trailingBreak = false
        var breakSpace = false
        var spaceBreak = false

        // Check document indicators.
        if (scalar.startsWith("---") || scalar.startsWith("...")) {
            blockIndicators = true
            flowIndicators = true
        }
        // First character or preceded by a whitespace.
        var precededByWhitespace = true
        var followedByWhitespace = scalar.length == 1 || CharConstants.NULL_BL_T_LINEBR.has(scalar.codePointAt(1))
        // The previous character is a space.
        var previousSpace = false

        // The previous character is a break.
        var previousBreak = false
        var index = 0
        while (index < scalar.length) {
            val c = scalar.codePointAt(index)
            // Check for indicators.
            if (index == 0) {
                // Leading indicators are special characters.
                if (c.toChar() in "#,[]{}&*!|>'\"%@`") {
                    flowIndicators = true
                    blockIndicators = true
                }
                if (c == '?'.code || c == ':'.code) {
                    flowIndicators = true
                    if (followedByWhitespace) {
                        blockIndicators = true
                    }
                }
                if (c == '-'.code && followedByWhitespace) {
                    flowIndicators = true
                    blockIndicators = true
                }
            } else {
                // Some indicators cannot appear within a scalar as well.
                if (c.toChar() in ",?[]{}") {
                    flowIndicators = true
                }
                if (c == ':'.code) {
                    flowIndicators = true
                    if (followedByWhitespace) {
                        blockIndicators = true
                    }
                }
                if (c == '#'.code && precededByWhitespace) {
                    flowIndicators = true
                    blockIndicators = true
                }
            }
            // Check for line breaks, special, and unicode characters.
            val isLineBreak = CharConstants.LINEBR.has(c)
            if (isLineBreak) {
                lineBreaks = true
            }
            if (!(c == '\n'.code || c in 0x20..0x7E)) {
                if (c == 0x85
                    || c in 0xA0..0xD7FF
                    || c in 0xE000..0xFFFD
                    || c in 0x10000..0x10FFFF
                ) {
                    // unicode is used
                    if (!allowUnicode) {
                        specialCharacters = true
                    }
                } else {
                    specialCharacters = true
                }
            }
            // Detect important whitespace combinations.
            if (c == ' '.code) {
                if (index == 0) {
                    leadingSpace = true
                }
                if (index == scalar.length - 1) {
                    trailingSpace = true
                }
                if (previousBreak) {
                    breakSpace = true
                }
                previousSpace = true
                previousBreak = false
            } else if (isLineBreak) {
                if (index == 0) {
                    leadingBreak = true
                }
                if (index == scalar.length - 1) {
                    trailingBreak = true
                }
                if (previousSpace) {
                    spaceBreak = true
                }
                previousSpace = false
                previousBreak = true
            } else {
                previousSpace = false
                previousBreak = false
            }

            // Prepare for the next character.
            index += Character.charCount(c)
            precededByWhitespace = CharConstants.NULL_BL_T.has(c) || isLineBreak
            followedByWhitespace = true
            if (index + 1 < scalar.length) {
                val nextIndex = index + Character.charCount(scalar.codePointAt(index))
                if (nextIndex < scalar.length) {
                    followedByWhitespace = CharConstants.NULL_BL_T.has(scalar.codePointAt(nextIndex)) || isLineBreak
                }
            }
        }
        // Let's decide what styles are allowed.
        var allowFlowPlain = true
        var allowBlockPlain = true
        var allowSingleQuoted = true
        var allowBlock = true
        // Leading and trailing whitespaces are bad for plain scalars.
        if (leadingSpace || leadingBreak || trailingSpace || trailingBreak) {
            allowBlockPlain = false
            allowFlowPlain = false
        }
        // We do not permit trailing spaces for block scalars.
        if (trailingSpace) {
            allowBlock = false
        }
        // Spaces at the beginning of a new line are only acceptable for block scalars.
        if (breakSpace) {
            allowSingleQuoted = false
            allowBlockPlain = false
            allowFlowPlain = false
        }
        // Spaces followed by breaks, as well as special character are only allowed for double-quoted scalars.
        if (spaceBreak || specialCharacters) {
            allowBlock = false
            allowSingleQuoted = false
            allowBlockPlain = false
            allowFlowPlain = false
        }
        // Although the plain scalar writer supports breaks, we never emit
        // multiline plain scalars in the flow context.
        if (lineBreaks) {
            allowFlowPlain = false
        }
        // Flow indicators are forbidden for flow plain scalars.
        if (flowIndicators) {
            allowFlowPlain = false
        }
        // Block indicators are forbidden for block plain scalars.
        if (blockIndicators) {
            allowBlockPlain = false
        }
        return ScalarAnalysis(
            scalar = scalar,
            empty = false,
            multiline = lineBreaks,
            allowFlowPlain = allowFlowPlain,
            allowBlockPlain = allowBlockPlain,
            allowSingleQuoted = allowSingleQuoted,
            allowBlock = allowBlock,
        )
    }

    //endregion

    //region Writers.

    private fun flushStream(): Unit = stream.flush()

    private fun writeStreamStart(): Unit = Unit // BOM is written by Writer.

    private fun writeStreamEnd(): Unit = flushStream()

    private fun writeIndicator(
        indicator: String,
        needWhitespace: Boolean = false,
        whitespace: Boolean = false,
        indentation: Boolean = false,
    ) {
        if (!this.whitespace && needWhitespace) {
            column++
            stream.write(SPACE)
        }
        this.whitespace = whitespace
        indention = indention && indentation
        column += indicator.length
        openEnded = false
        stream.write(indicator)
    }

    private fun writeIndent(): Int {
        val indentToWrite = indent ?: 0
        if (!indention || column > indentToWrite || column == indentToWrite && !whitespace) {
            writeLineBreak()
        }
        val whitespaces = indentToWrite - this.column
        writeWhitespace(whitespaces)
        return whitespaces
    }

    private fun writeWhitespace(length: Int) {
        if (length <= 0) return
        whitespace = true
        stream.write(SPACE.repeat(length))
        column += length
    }

    private fun writeLineBreak(data: String? = null) {
        whitespace = true
        indention = true
        column = 0
        stream.write(data ?: bestLineBreak)
    }

    fun writeVersionDirective(versionText: String) {
        stream.write("%YAML $versionText")
        writeLineBreak()
    }

    fun writeTagDirective(handleText: String, prefixText: String) {
        // XXX: not sure 4 invocations better than StringBuilders created by str + str
        stream.write("%TAG $handleText $prefixText")
        writeLineBreak()
    }

    //endregion

    //region Scalar streams.
    private fun writeSingleQuoted(text: String, split: Boolean) {
        writeIndicator(indicator = "'", needWhitespace = true)
        var spaces = false
        var breaks = false
        var start = 0
        var end = 0
        var ch: Char
        while (end <= text.length) {
            ch = 0.toChar()
            if (end < text.length) {
                ch = text[end]
            }
            if (spaces) {
                if (ch != ' ') {
                    if (start + 1 == end && column > bestWidth && split && start != 0 && end != text.length) {
                        writeIndent()
                    } else {
                        val len = end - start
                        column += len
                        stream.write(text, start, len)
                    }
                    start = end
                }
            } else if (breaks) {
                if (ch.code == 0 || CharConstants.LINEBR.hasNo(ch.code)) {
                    if (text[start] == '\n') {
                        writeLineBreak()
                    }
                    val data = text.substring(start, end)
                    for (br in data.toCharArray()) {
                        if (br == '\n') {
                            writeLineBreak()
                        } else {
                            writeLineBreak(br.toString())
                        }
                    }
                    writeIndent()
                    start = end
                }
            } else {
                if (CharConstants.LINEBR.has(ch.code, "\u0000 '")) {
                    if (start < end) {
                        val len = end - start
                        column += len
                        stream.write(text, start, len)
                        start = end
                    }
                }
            }
            if (ch == '\'') {
                column += 2
                stream.write("''")
                start = end + 1
            }
            if (ch.code != 0) {
                spaces = ch == ' '
                breaks = CharConstants.LINEBR.has(ch.code)
            }
            end++
        }
        writeIndicator(indicator = "'")
    }

    private fun writeDoubleQuoted(text: String, split: Boolean) {
        writeIndicator(indicator = "\"", needWhitespace = true)
        var start = 0
        var end = 0
        while (end <= text.length) {
            var ch: Char? = null
            if (end < text.length) {
                ch = text[end]
            }
            if (ch == null || ch in "\"\\\u0085\u2028\u2029\uFEFF" || !('\u0020' <= ch && ch <= '\u007E')) {
                if (start < end) {
                    val len = end - start
                    column += len
                    stream.write(text, start, len)
                    start = end
                }
                if (ch != null) {
                    var data: String
                    if (ch in ESCAPE_REPLACEMENTS) {
                        data = "\\" + ESCAPE_REPLACEMENTS[ch]
                    } else {
                        val codePoint: Int = if (ch.isHighSurrogate() && end + 1 < text.length) {
                            val ch2 = text[end + 1]
                            Character.toCodePoint(ch, ch2)
                        } else {
                            ch.code
                        }
                        if (allowUnicode && StreamReader.isPrintable(codePoint)) {
                            data = Character.toChars(codePoint).concatToString()
                            if (Character.charCount(codePoint) == 2) {
                                end++
                            }
                        } else {
                            // if !allowUnicode or the character is not printable,
                            // we must encode it
                            data = if (ch <= '\u00FF') {
                                val s = "0" + ch.code.toString(16)
                                "\\x" + s.substring(s.length - 2)
                            } else if (Character.charCount(codePoint) == 2) {
                                end++
                                val s = "000" + codePoint.toString(16)
                                "\\U" + s.substring(s.length - 8)
                            } else {
                                val s = "000" + ch.code.toString(16)
                                "\\u" + s.substring(s.length - 4)
                            }
                        }
                    }
                    column += data.length
                    stream.write(data)
                    start = end + 1
                }
            }
            if (0 < end && end < text.length - 1 && (ch == ' ' || start >= end) && column + (end - start) > bestWidth && split) {
                var data: String
                data = (if (start >= end) "\\" else text.substring(start, end) + "\\")
                if (start < end) {
                    start = end
                }
                column += data.length
                stream.write(data)
                writeIndent()
                whitespace = false
                indention = false
                if (text[start] == ' ') {
                    data = "\\"
                    column += data.length
                    stream.write(data)
                }
            }
            end += 1
        }
        writeIndicator(indicator = "\"")
    }

    private fun writeCommentLines(commentLines: List<CommentLine>): Boolean {
        var wroteComment = false
        if (emitComments) {
            var indentColumns = 0
            var prevColumns = 0
            var firstComment = true
            for (commentLine in commentLines) {
                if (commentLine.commentType != CommentType.BLANK_LINE) {
                    if (firstComment) {
                        firstComment = false
                        writeIndicator(
                            indicator = "#",
                            needWhitespace = commentLine.commentType == CommentType.IN_LINE,
                        )
                        indentColumns = if (column > 0) column - 1 else 0
                    } else {
                        writeWhitespace(indentColumns - prevColumns)
                        writeIndicator(indicator = "#")
                    }
                    stream.write(commentLine.value)
                    writeLineBreak()
                    prevColumns = 0
                } else {
                    writeLineBreak()
                    prevColumns = writeIndent()
                }
                wroteComment = true
            }
        }
        return wroteComment
    }

    private fun writeBlockComment() {
        if (!blockCommentsCollector.isEmpty()) {
            writeIndent()
            writeCommentLines(blockCommentsCollector.consume())
        }
    }

    private fun writeInlineComments(): Boolean {
        return writeCommentLines(inlineCommentsCollector.consume())
    }

    private fun determineBlockHints(text: String): String {
        val hints = StringBuilder()
        if (CharConstants.LINEBR.has(text.first().code, " ")) {
            hints.append(bestIndent)
        }
        val ch1 = text.last()
        if (CharConstants.LINEBR.hasNo(ch1.code)) {
            hints.append("-")
        } else if (text.length == 1 || CharConstants.LINEBR.has(text[text.length - 2].code)) {
            hints.append("+")
        }
        return hints.toString()
    }

    private fun writeFolded(text: String, split: Boolean) {
        val hints = determineBlockHints(text)
        writeIndicator(indicator = ">$hints", needWhitespace = true)
        if (hints.endsWith('+')) {
            openEnded = true
        }
        if (!writeInlineComments()) {
            writeLineBreak()
        }
        var leadingSpace = true
        var spaces = false
        var breaks = true
        var start = 0
        var end = 0
        while (end <= text.length) {
            var ch = 0.toChar()
            if (end < text.length) {
                ch = text[end]
            }
            if (breaks) {
                if (ch.code == 0 || CharConstants.LINEBR.hasNo(ch.code)) {
                    if (!leadingSpace && ch.code != 0 && ch != ' ' && text[start] == '\n') {
                        writeLineBreak()
                    }
                    leadingSpace = ch == ' '
                    val data = text.substring(start, end)
                    for (br in data.toCharArray()) {
                        writeLineBreak(if (br == '\n') null else br.toString())
                    }
                    if (ch.code != 0) {
                        writeIndent()
                    }
                    start = end
                }
            } else if (spaces) {
                if (ch != ' ') {
                    if (start + 1 == end && column > bestWidth && split) {
                        writeIndent()
                    } else {
                        val len = end - start
                        column += len
                        stream.write(text, start, len)
                    }
                    start = end
                }
            } else {
                if (CharConstants.LINEBR.has(ch.code, "\u0000 ")) {
                    val len = end - start
                    column += len
                    stream.write(text, start, len)
                    if (ch.code == 0) {
                        writeLineBreak()
                    }
                    start = end
                }
            }
            if (ch.code != 0) {
                breaks = CharConstants.LINEBR.has(ch.code)
                spaces = ch == ' '
            }
            end++
        }
    }

    private fun writeLiteral(text: String) {
        val hints = determineBlockHints(text)
        writeIndicator(indicator = "|$hints", needWhitespace = true)
        if (hints.endsWith('+')) {
            openEnded = true
        }
        if (!writeInlineComments()) {
            writeLineBreak()
        }
        var breaks = true
        var start = 0
        var end = 0
        while (end <= text.length) {
            var ch = 0.toChar()
            if (end < text.length) {
                ch = text[end]
            }
            if (breaks) {
                if (ch.code == 0 || CharConstants.LINEBR.hasNo(ch.code)) {
                    val data = text.substring(start, end)
                    for (br in data.toCharArray()) {
                        if (br == '\n') {
                            writeLineBreak()
                        } else {
                            writeLineBreak(br.toString())
                        }
                    }
                    if (ch.code != 0) {
                        writeIndent()
                    }
                    start = end
                }
            } else {
                if (ch.code == 0 || CharConstants.LINEBR.has(ch.code)) {
                    stream.write(text, start, end - start)
                    if (ch.code == 0) {
                        writeLineBreak()
                    }
                    start = end
                }
            }
            if (ch.code != 0) {
                breaks = CharConstants.LINEBR.has(ch.code)
            }
            end++
        }
    }

    private fun writePlain(text: String, split: Boolean) {
        if (rootContext) {
            openEnded = true
        }
        if (text.isEmpty()) {
            return
        }
        if (!whitespace) {
            column++
            stream.write(SPACE)
        }
        whitespace = false
        indention = false
        var spaces = false
        var breaks = false
        var start = 0
        var end = 0
        while (end <= text.length) {
            var ch = 0.toChar()
            if (end < text.length) {
                ch = text[end]
            }
            if (spaces) {
                if (ch != ' ') {
                    if (start + 1 == end && column > bestWidth && split) {
                        writeIndent()
                        whitespace = false
                        indention = false
                    } else {
                        val len = end - start
                        column += len
                        stream.write(text, start, len)
                    }
                    start = end
                }
            } else if (breaks) {
                if (CharConstants.LINEBR.hasNo(ch.code)) {
                    if (text[start] == '\n') {
                        writeLineBreak()
                    }
                    val data = text.substring(start, end)
                    for (br in data.toCharArray()) {
                        writeLineBreak(if (br == '\n') null else br.toString())
                    }
                    writeIndent()
                    whitespace = false
                    indention = false
                    start = end
                }
            } else {
                if (CharConstants.LINEBR.has(ch.code, "\u0000 ")) {
                    val len = end - start
                    column += len
                    stream.write(text, start, len)
                    start = end
                }
            }
            if (ch.code != 0) {
                spaces = ch == ' '
                breaks = CharConstants.LINEBR.has(ch.code)
            }
            end++
        }
    }

    //endregion

    //endregion

    companion object {
        private val ESCAPE_REPLACEMENTS: Map<Char, String> = mapOf(
            0.toChar() to "0",
            '\u0007' to "a",
            '\u0008' to "b",
            '\u0009' to "t",
            '\n' to "n",
            '\u000B' to "v",
            '\u000C' to "f",
            '\r' to "r",
            '\u001B' to "e",
            '"' to "\"",
            '\\' to "\\",
            '\u0085' to "N",
            '\u00A0' to "_",
        )

        private val DEFAULT_TAG_PREFIXES: Map<String, String> = mapOf(
            "!" to "!",
            Tag.PREFIX to "!!",
        )

        /** indent cannot be zero spaces and should not be more than 10 spaces */
        @JvmField
        val VALID_INDENT_RANGE = 1..10

        @JvmField
        val VALID_INDICATOR_INDENT_RANGE = (VALID_INDENT_RANGE.first - 1) until VALID_INDENT_RANGE.last

        private const val DEFAULT_INDENT = 2

        private const val DEFAULT_WIDTH = 80

        private const val SPACE = " "

        private val HANDLE_FORMAT = Regex("""^![-_\w]*!$""")
    }
}
