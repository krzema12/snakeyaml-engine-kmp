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
package org.snakeyaml.engine.v2.emitter

import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.api.StreamDataWriter
import org.snakeyaml.engine.v2.comments.CommentEventsCollector
import org.snakeyaml.engine.v2.comments.CommentLine
import org.snakeyaml.engine.v2.comments.CommentType
import org.snakeyaml.engine.v2.common.Anchor
import org.snakeyaml.engine.v2.common.CharConstants
import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.common.SpecVersion
import org.snakeyaml.engine.v2.events.AliasEvent
import org.snakeyaml.engine.v2.events.CollectionEndEvent
import org.snakeyaml.engine.v2.events.CollectionStartEvent
import org.snakeyaml.engine.v2.events.CommentEvent
import org.snakeyaml.engine.v2.events.DocumentEndEvent
import org.snakeyaml.engine.v2.events.DocumentStartEvent
import org.snakeyaml.engine.v2.events.Event
import org.snakeyaml.engine.v2.events.MappingStartEvent
import org.snakeyaml.engine.v2.events.NodeEvent
import org.snakeyaml.engine.v2.events.ScalarEvent
import org.snakeyaml.engine.v2.events.SequenceStartEvent
import org.snakeyaml.engine.v2.events.StreamEndEvent
import org.snakeyaml.engine.v2.events.StreamStartEvent
import org.snakeyaml.engine.v2.exceptions.EmitterException
import org.snakeyaml.engine.v2.exceptions.YamlEngineException
import org.snakeyaml.engine.v2.nodes.Tag
import org.snakeyaml.engine.v2.scanner.StreamReader
import java.util.Optional
import java.util.TreeSet
import java.util.regex.Pattern

// TODO check if single-use functions can be put inside the state classes

/**
 * <pre>
 * Emitter expects events obeying the following grammar:
 * stream ::= STREAM-START document* STREAM-END
 * document ::= DOCUMENT-START node DOCUMENT-END
 * node ::= SCALAR | sequence | mapping
 * sequence ::= SEQUENCE-START node* SEQUENCE-END
 * mapping ::= MAPPING-START (node node)* MAPPING-END
 * </pre>
 */
class Emitter(
    private val opts: DumpSettings,
    private val stream: StreamDataWriter,
) : Emitable {
//    private val emitterJava = EmitterJava(opts, stream)

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

    private val allowUnicode = opts.isUseUnicodeEncoding
    private val bestIndent = if (opts.indent in MIN_INDENT..MAX_INDENT) opts.indent else 2
    private val indicatorIndent = opts.indicatorIndent
    private val indentWithIndicator = opts.indentWithIndicator
    private val bestWidth = opts.width.coerceAtMost(80)
    private val bestLineBreak = opts.bestLineBreak
    private val splitLines = opts.isSplitLines
    private val maxSimpleKeyLength = opts.maxSimpleKeyLength
    private val emitComments = opts.dumpComments
    //endregion

    /** Tag prefixes. */
    private var tagPrefixes: MutableMap<String?, String> = mutableMapOf()

    private var preparedAnchor: Optional<Anchor> = Optional.empty()
    private var preparedTag: String? = null

    /** Scalar analysis */
    private var analysis: ScalarAnalysis? = null

    /** Scalar style */
    private var scalarStyle: Optional<ScalarStyle> = Optional.empty()

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
        val iter: Iterator<Event> = events.iterator()
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

            else                  -> if (emitComments) {
                // To collect any comment events
                needEvents(iter, 1)
            } else {
                false
            }
        }
    }

    private fun needEvents(iter: Iterator<Event>, count: Int): Boolean {
        var level = 0
        var actualCount = 0
        while (iter.hasNext()) {
            val event = iter.next()
            if (event is CommentEvent) {
                continue
            }
            actualCount++
            when (event) {
                is DocumentStartEvent, is CollectionStartEvent -> {
                    level++
                }

                is DocumentEndEvent, is CollectionEndEvent     -> {
                    level--
                }

                is StreamEndEvent                              -> {
                    level = -1
                }
            }
            if (level < 0) {
                return false
            }
        }
        return actualCount < count
    }

    private fun increaseIndent(isFlow: Boolean, indentless: Boolean) {
        indents.addLast(indent)
        if (indent == null) {
            indent = if (isFlow) {
                bestIndent
            } else {
                0
            }
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
            if (event!!.eventId == Event.ID.DocumentStart) {
                val ev = event as DocumentStartEvent
                handleDocumentStartEvent(ev)
                state = ExpectDocumentRoot()
            } else if (event!!.eventId == Event.ID.StreamEnd) {
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
            if ((ev.specVersion.isPresent || ev.tags.isNotEmpty()) && openEnded) {
                writeIndicator(indicator = "...", needWhitespace = true, whitespace = false, indentation = false)
                writeIndent()
            }
            ev.specVersion.ifPresent { version: SpecVersion? ->
                writeVersionDirective(prepareVersion(version!!))
            }
            tagPrefixes = LinkedHashMap(DEFAULT_TAG_PREFIXES)
            if (ev.tags.isNotEmpty()) {
                handleTagDirectives(ev.tags)
            }
            val implicit = (first && !ev.isExplicit && !canonical && !ev.specVersion.isPresent
                && ev.tags.isEmpty() && !checkEmptyDocument())
            if (!implicit) {
                writeIndent()
                writeIndicator(indicator = "---", needWhitespace = true, whitespace = false, indentation = false)
                if (canonical) {
                    writeIndent()
                }
            }
        }

        private fun handleTagDirectives(tags: Map<String, String>) {
            val handles: Set<String> = TreeSet(tags.keys)
            for (handle in handles) {
                val prefix = tags[handle]
                tagPrefixes[prefix] = handle
                val handleText = prepareTagHandle(handle)
                val prefixText = prepareTagPrefix(prefix!!)
                writeTagDirective(handleText, prefixText)
            }
        }

        private fun checkEmptyDocument(): Boolean {
            if (event!!.eventId != Event.ID.DocumentStart || events.isEmpty()) {
                return false
            }
            val nextEvent = events.first()
            if (nextEvent.eventId == Event.ID.Scalar) {
                val e = nextEvent as ScalarEvent
                return (!e.anchor.isPresent
                    && !e.tag.isPresent && e.value.isEmpty())
            }
            return false
        }
    }

    private inner class ExpectDocumentEnd : EmitterState {
        override fun expect() {

            event = blockCommentsCollector.collectEventsAndPoll(event)
            writeBlockComment()
            if (event!!.eventId == Event.ID.DocumentEnd) {
                writeIndent()
                if ((event as DocumentEndEvent).isExplicit) {
                    writeIndicator(indicator = "...", needWhitespace = true, whitespace = false, indentation = false)
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
            expectNode(root = true, mapping = false, simpleKey = false)
        }
    }

    //endregion

    //region Node handlers.

    private fun expectNode(root: Boolean, mapping: Boolean, simpleKey: Boolean) {
        rootContext = root
        mappingContext = mapping
        simpleKeyContext = simpleKey
        when (event!!.eventId) {
            Event.ID.Alias                                                 -> {
                expectAlias()
            }

            Event.ID.Scalar, Event.ID.SequenceStart, Event.ID.MappingStart -> {
                processAnchor("&")
                processTag()
                handleNodeEvent(event!!.eventId)
            }

            else                                                           -> {
                throw EmitterException("expected NodeEvent, but got " + event!!.eventId)
            }
        }
    }

    private fun handleNodeEvent(id: Event.ID) {
        when (id) {
            Event.ID.Scalar        -> expectScalar()
            Event.ID.SequenceStart -> if (flowLevel != 0 || canonical || (event as SequenceStartEvent).isFlow()
                || checkEmptySequence()
            ) {
                expectFlowSequence()
            } else {
                expectBlockSequence()
            }

            Event.ID.MappingStart  -> if (flowLevel != 0 || canonical || (event as MappingStartEvent).isFlow()
                || checkEmptyMapping()
            ) {
                expectFlowMapping()
            } else {
                expectBlockMapping()
            }

            else                   -> throw IllegalStateException()
        }
    }

    private fun expectAlias() {
        state = if (event is AliasEvent) {
            processAnchor("*")
            states.removeLast()
        } else {
            throw EmitterException("Expecting Alias.")
        }
    }

    private fun expectScalar() {
        increaseIndent(true, indentless = false)
        processScalar()
        indent = indents.removeLastOrNull()
        state = states.removeLastOrNull()!!
    }


    //endregion

    //region Flow sequence handlers.

    private fun expectFlowSequence() {
        writeIndicator(indicator = "[", needWhitespace = true, whitespace = true, indentation = false)
        flowLevel++
        increaseIndent(isFlow = true, indentless = false)
        if (multiLineFlow) {
            writeIndent()
        }
        state = ExpectFirstFlowSequenceItem()
    }


    private inner class ExpectFirstFlowSequenceItem : EmitterState {
        override fun expect() {
            if (event!!.eventId == Event.ID.SequenceEnd) {
                indent = indents.removeLastOrNull()
                flowLevel--
                writeIndicator(indicator = "]", needWhitespace = false, whitespace = false, indentation = false)
                inlineCommentsCollector.collectEvents()
                writeInlineComments()
                state = states.removeLastOrNull()!!
            } else if (event is CommentEvent) {
                blockCommentsCollector.collectEvents(event)
                writeBlockComment()
            } else {
                if (canonical || column > bestWidth && splitLines || multiLineFlow) {
                    writeIndent()
                }
                states.addLast(ExpectFlowSequenceItem())
                expectNode(root = false, mapping = false, simpleKey = false)
                event = inlineCommentsCollector.collectEvents(event)
                writeInlineComments()
            }

        }
    }

    private inner class ExpectFlowSequenceItem : EmitterState {
        override fun expect() {
            if (event!!.eventId == Event.ID.SequenceEnd) {
                indent = indents.removeLastOrNull()
                flowLevel--
                if (canonical) {
                    writeIndicator(indicator = ",", needWhitespace = false, whitespace = false, indentation = false)
                    writeIndent()
                } else if (multiLineFlow) {
                    writeIndent()
                }
                writeIndicator(indicator = "]", needWhitespace = false, whitespace = false, indentation = false)
                inlineCommentsCollector.collectEvents()
                writeInlineComments()
                if (multiLineFlow) {
                    writeIndent()
                }
                state = states.removeLastOrNull()!!
            } else if (event is CommentEvent) {
                event = blockCommentsCollector.collectEvents(event)
            } else {
                writeIndicator(indicator = ",", needWhitespace = false, whitespace = false, indentation = false)
                writeBlockComment()
                if (canonical || column > bestWidth && splitLines || multiLineFlow) {
                    writeIndent()
                }
                states.addLast(ExpectFlowSequenceItem())
                expectNode(root = false, mapping = false, simpleKey = false)
                event = inlineCommentsCollector.collectEvents(event)
                writeInlineComments()
            }
        }
    }

    //endregion

    //region Flow mapping handlers.

    private fun expectFlowMapping() {
        writeIndicator(indicator = "{", needWhitespace = true, whitespace = true, indentation = false)
        flowLevel++
        increaseIndent(isFlow = true, indentless = false)
        if (multiLineFlow) {
            writeIndent()
        }
        state = ExpectFirstFlowMappingKey()
    }

    private inner class ExpectFirstFlowMappingKey : EmitterState {
        override fun expect() {
            event = blockCommentsCollector.collectEventsAndPoll(event)
            writeBlockComment()
            if (event!!.eventId == Event.ID.MappingEnd) {
                indent = indents.removeLastOrNull()
                flowLevel--
                writeIndicator(indicator = "}", needWhitespace = false, whitespace = false, indentation = false)
                inlineCommentsCollector.collectEvents()
                writeInlineComments()
                state = states.removeLastOrNull()!!
            } else {
                if (canonical || column > bestWidth && splitLines || multiLineFlow) {
                    writeIndent()
                }
                if (!canonical && checkSimpleKey()) {
                    states.addLast(ExpectFlowMappingSimpleValue())
                    expectNode(root = false, mapping = true, simpleKey = true)
                } else {
                    writeIndicator(indicator = "?", needWhitespace = true, whitespace = false, indentation = false)
                    states.addLast(ExpectFlowMappingValue())
                    expectNode(false, mapping = true, simpleKey = false)
                }
            }

        }
    }

    private inner class ExpectFlowMappingKey : EmitterState {
        override fun expect() {
            if (event!!.eventId == Event.ID.MappingEnd) {
                indent = indents.removeLastOrNull()
                flowLevel--
                if (canonical) {
                    writeIndicator(indicator = ",", needWhitespace = false, whitespace = false, indentation = false)
                    writeIndent()
                }
                if (multiLineFlow) {
                    writeIndent()
                }
                writeIndicator(indicator = "}", needWhitespace = false, whitespace = false, indentation = false)
                inlineCommentsCollector.collectEvents()
                writeInlineComments()
                state = states.removeLastOrNull()!!
            } else {
                writeIndicator(indicator = ",", needWhitespace = false, whitespace = false, indentation = false)
                event = blockCommentsCollector.collectEventsAndPoll(event)
                writeBlockComment()
                if (canonical || column > bestWidth && splitLines || multiLineFlow) {
                    writeIndent()
                }
                if (!canonical && checkSimpleKey()) {
                    states.addLast(ExpectFlowMappingSimpleValue())
                    expectNode(false, mapping = true, simpleKey = true)
                } else {
                    writeIndicator(indicator = "?", needWhitespace = true, whitespace = false, indentation = false)
                    states.addLast(ExpectFlowMappingValue())
                    expectNode(false, mapping = true, simpleKey = false)
                }
            }

        }
    }

    private inner class ExpectFlowMappingSimpleValue : EmitterState {
        override fun expect() {
            writeIndicator(indicator = ":", needWhitespace = false, whitespace = false, indentation = false)
            event = inlineCommentsCollector.collectEventsAndPoll(event)
            writeInlineComments()
            states.addLast(ExpectFlowMappingKey())
            expectNode(root = false, mapping = true, simpleKey = false)
            inlineCommentsCollector.collectEvents(event)
            writeInlineComments()
        }
    }

    private inner class ExpectFlowMappingValue : EmitterState {
        override fun expect() {
            if (canonical || column > bestWidth || multiLineFlow) {
                writeIndent()
            }
            writeIndicator(indicator = ":", needWhitespace = true, whitespace = false, indentation = false)
            event = inlineCommentsCollector.collectEventsAndPoll(event)
            writeInlineComments()
            states.addLast(ExpectFlowMappingKey())
            expectNode(root = false, mapping = true, simpleKey = false)
            inlineCommentsCollector.collectEvents(event)
            writeInlineComments()

        }
    }
    //endregion

    //region Block sequence handlers.
    private fun expectBlockSequence() {
        val indentless = mappingContext && !indention
        increaseIndent(false, indentless)
        state = ExpectFirstBlockSequenceItem()
    }

    private inner class ExpectFirstBlockSequenceItem : EmitterState {
        override fun expect() {
            ExpectBlockSequenceItem(true).expect()
        }
    }

    private inner class ExpectBlockSequenceItem(private val first: Boolean) : EmitterState {
        override fun expect() {

            if (!first && event!!.eventId == Event.ID.SequenceEnd) {
                indent = indents.removeLastOrNull()
                state = states.removeLastOrNull()!!
            } else if (event is CommentEvent) {
                blockCommentsCollector.collectEvents(event)
            } else {
                writeIndent()
                if (!indentWithIndicator || first) {
                    writeWhitespace(indicatorIndent)
                }
                writeIndicator(indicator = "-", needWhitespace = true, whitespace = false, indentation = true)
                if (indentWithIndicator && first) {
                    indent = indent!! + indicatorIndent
                }
                if (!blockCommentsCollector.isEmpty()) {
                    increaseIndent(isFlow = false, indentless = false)
                    writeBlockComment()
                    if (event is ScalarEvent) {
                        analysis = analyzeScalar((event as ScalarEvent).value)
                        if (!analysis!!.isEmpty()) {
                            writeIndent()
                        }
                    }
                    indent = indents.removeLastOrNull()
                }
                states.addLast(ExpectBlockSequenceItem(false))
                expectNode(root = false, mapping = false, simpleKey = false)
                inlineCommentsCollector.collectEvents()
                writeInlineComments()
            }
        }
    }

    //endregion

    // region Block mapping handlers.

    private fun expectBlockMapping() {
        increaseIndent(isFlow = false, indentless = false)
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
            if (!first && event!!.eventId == Event.ID.MappingEnd) {
                indent = indents.removeLastOrNull()
                state = states.removeLastOrNull()!!
            } else {
                writeIndent()
                if (checkSimpleKey()) {
                    states.addLast(ExpectBlockMappingSimpleValue())
                    expectNode(root = false, mapping = true, simpleKey = true)
                } else {
                    writeIndicator(indicator = "?", needWhitespace = true, whitespace = false, indentation = true)
                    states.addLast(ExpectBlockMappingValue())
                    expectNode(root = false, mapping = true, simpleKey = false)
                }
            }
        }
    }

    private inner class ExpectBlockMappingSimpleValue : EmitterState {
        override fun expect() {

            writeIndicator(indicator = ":", needWhitespace = false, whitespace = false, indentation = false)
            event = inlineCommentsCollector.collectEventsAndPoll(event)
            if (!isFoldedOrLiteral(event!!)) {
                if (writeInlineComments()) {
                    increaseIndent(isFlow = true, indentless = false)
                    writeIndent()
                    indent = indents.removeLastOrNull()
                }
            }
            event = blockCommentsCollector.collectEventsAndPoll(event)
            if (!blockCommentsCollector.isEmpty()) {
                increaseIndent(isFlow = true, indentless = false)
                writeBlockComment()
                writeIndent()
                indent = indents.removeLastOrNull()
            }
            states.addLast(ExpectBlockMappingKey(false))
            expectNode(root = false, mapping = true, simpleKey = false)
            inlineCommentsCollector.collectEvents()
            writeInlineComments()
        }

        private fun isFoldedOrLiteral(event: Event): Boolean {
            if (event.eventId != Event.ID.Scalar) {
                return false
            }
            val scalarEvent = event as ScalarEvent
            val style = scalarEvent.scalarStyle
            return style == ScalarStyle.FOLDED || style == ScalarStyle.LITERAL
        }
    }

    private inner class ExpectBlockMappingValue : EmitterState {
        override fun expect() {

            writeIndent()
            writeIndicator(indicator = ":", needWhitespace = true, whitespace = false, indentation = true)
            event = inlineCommentsCollector.collectEventsAndPoll(event)
            writeInlineComments()
            event = blockCommentsCollector.collectEventsAndPoll(event)
            writeBlockComment()
            states.addLast(ExpectBlockMappingKey(false))
            expectNode(root = false, mapping = true, simpleKey = false)
            inlineCommentsCollector.collectEvents(event)
            writeInlineComments()
        }
    }


    //endregion

    //region Checkers.

    private fun checkEmptySequence(): Boolean {
        return event!!.eventId == Event.ID.SequenceStart && !events.isEmpty() && events.first()
            .eventId == Event.ID.SequenceEnd
    }

    private fun checkEmptyMapping(): Boolean {
        return event!!.eventId == Event.ID.MappingStart && !events.isEmpty() && events.first()
            .eventId == Event.ID.MappingEnd
    }

    private fun checkSimpleKey(): Boolean {
        var length = 0
        if (event is NodeEvent) {
            val anchorOpt: Optional<Anchor> = (event as NodeEvent).anchor
            if (anchorOpt.isPresent) {
                if (!preparedAnchor.isPresent) {
                    preparedAnchor = anchorOpt
                }
                length += anchorOpt.get().value.length
            }
        }
        var tag: Optional<String> = Optional.empty()
        if (event!!.eventId == Event.ID.Scalar) {
            tag = (event as ScalarEvent).tag
        } else if (event is CollectionStartEvent) {
            tag = (event as CollectionStartEvent).tag
        }
        if (tag.isPresent) {
            if (preparedTag == null) {
                preparedTag = prepareTag(tag.get())
            }
            length += preparedTag!!.length
        }
        if (event!!.eventId == Event.ID.Scalar) {
            if (analysis == null) {
                analysis = analyzeScalar((event as ScalarEvent).value)
            }
            length += analysis!!.getScalar().length
        }
        return length < maxSimpleKeyLength && (event!!.eventId == Event.ID.Alias || event!!.eventId == Event.ID.Scalar && !analysis!!.isEmpty() && !analysis!!.isMultiline()
            || checkEmptySequence() || checkEmptyMapping())
    }

    //endregion

    //region Anchor, Tag, and Scalar processors.

    private fun processAnchor(indicator: String) {
        val ev = event as NodeEvent
        val anchorOption: Optional<Anchor> = ev.anchor
        if (anchorOption.isPresent) {
            val anchor = anchorOption.get()
            if (!preparedAnchor.isPresent) {
                preparedAnchor = anchorOption
            }
            writeIndicator(
                indicator = indicator + anchor,
                needWhitespace = true,
                whitespace = false,
                indentation = false,
            )
        }
        preparedAnchor = Optional.empty()
    }

    private fun processTag() {
        var tag: Optional<String>
        if (event!!.eventId == Event.ID.Scalar) {
            val ev = event as ScalarEvent
            tag = ev.tag
            if (!scalarStyle.isPresent) {
                scalarStyle = chooseScalarStyle(ev)
            }
            if ((!canonical || !tag.isPresent)
                && (!scalarStyle.isPresent && ev.implicit
                    .canOmitTagInPlainScalar() || scalarStyle.isPresent && ev.implicit
                    .canOmitTagInNonPlainScalar())
            ) {
                preparedTag = null
                return
            }
            if (ev.implicit.canOmitTagInPlainScalar() && !tag.isPresent) {
                tag = Optional.of("!")
                preparedTag = null
            }
        } else {
            val ev = event as CollectionStartEvent
            tag = ev.tag
            if ((!canonical || !tag.isPresent) && ev.isImplicit()) {
                preparedTag = null
                return
            }
        }
        if (!tag.isPresent) {
            throw EmitterException("tag is not specified")
        }
        if (preparedTag == null) {
            preparedTag = prepareTag(tag.get())
        }
        writeIndicator(
            indicator = preparedTag!!,
            needWhitespace = true,
            whitespace = false,
            indentation = false,
        )
        preparedTag = null
    }

    private fun chooseScalarStyle(ev: ScalarEvent): Optional<ScalarStyle> {
        if (analysis == null) {
            analysis = analyzeScalar(ev.value)
        }
        if (!ev.isPlain && ev.scalarStyle == ScalarStyle.DOUBLE_QUOTED || canonical) {
            return Optional.of(ScalarStyle.DOUBLE_QUOTED)
        }
        if (ev.isPlain && ev.implicit.canOmitTagInPlainScalar()) {
            if (!(simpleKeyContext && (analysis!!.isEmpty() || analysis!!.isMultiline()))
                && (flowLevel != 0 && analysis!!.isAllowFlowPlain() || flowLevel == 0 && analysis!!.isAllowBlockPlain())
            ) {
                return Optional.empty()
            }
        }
        if (!ev.isPlain && (ev.scalarStyle == ScalarStyle.LITERAL
                || ev.scalarStyle == ScalarStyle.FOLDED)
        ) {
            if (flowLevel == 0 && !simpleKeyContext && analysis!!.isAllowBlock()) {
                return Optional.of<ScalarStyle?>(ev.scalarStyle)
            }
        }
        if (ev.isPlain || ev.scalarStyle == ScalarStyle.SINGLE_QUOTED) {
            if (analysis!!.isAllowSingleQuoted() && !(simpleKeyContext && analysis!!.isMultiline())) {
                return Optional.of(ScalarStyle.SINGLE_QUOTED)
            }
        }
        return Optional.of(ScalarStyle.DOUBLE_QUOTED)
    }

    private fun processScalar() {
        val ev = event as ScalarEvent
        if (analysis == null) {
            analysis = analyzeScalar(ev.value)
        }
        if (!scalarStyle.isPresent) {
            scalarStyle = chooseScalarStyle(ev)
        }
        val split = !simpleKeyContext && splitLines
        if (!scalarStyle.isPresent) {
            writePlain(analysis!!.getScalar(), split)
        } else {
            when (scalarStyle.get()) {
                ScalarStyle.DOUBLE_QUOTED -> writeDoubleQuoted(analysis!!.getScalar(), split)
                ScalarStyle.SINGLE_QUOTED -> writeSingleQuoted(analysis!!.getScalar(), split)
                ScalarStyle.FOLDED        -> writeFolded(analysis!!.getScalar(), split)
                ScalarStyle.LITERAL       -> writeLiteral(analysis!!.getScalar())
                else                      -> throw YamlEngineException("Unexpected scalarStyle: $scalarStyle")
            }
        }
        analysis = null
        scalarStyle = Optional.empty()
    }


    //endregion

    //region Analyzers.
    private fun prepareVersion(version: SpecVersion): String {
        if (version.major != 1) {
            throw EmitterException("unsupported YAML version: $version")
        }
        return version.representation
    }

    private fun prepareTagHandle(handle: String): String {
        if (handle.isEmpty()) {
            throw EmitterException("tag handle must not be empty")
        } else if (handle[0] != '!' || handle[handle.length - 1] != '!') {
            throw EmitterException("tag handle must start and end with '!': $handle")
        } else if ("!" != handle && !HANDLE_FORMAT.matcher(handle).matches()) {
            throw EmitterException("invalid character in the tag handle: $handle")
        }
        return handle
    }

    private fun prepareTagPrefix(prefix: String): String {
        if (prefix.isEmpty()) {
            throw EmitterException("tag prefix must not be empty")
        }
        val chunks = java.lang.StringBuilder()
        val start = 0
        var end = 0
        if (prefix[0] == '!') {
            end = 1
        }
        while (end < prefix.length) {
            end++
        }
        chunks.append(prefix, start, end)
        return chunks.toString()
    }

    private fun prepareTag(tag: String): String {
        if (tag.isEmpty()) {
            throw EmitterException("tag must not be empty")
        }
        if ("!" == tag) {
            return tag
        }
        var handle: String? = null
        var suffix = tag
        // shall the tag prefixes be sorted as in PyYAML?
        for (prefix in tagPrefixes.keys) {
            if (prefix != null && tag.startsWith(prefix) && ("!" == prefix || prefix.length < tag.length)) {
                handle = prefix
            }
        }
        if (handle != null) {
            suffix = tag.substring(handle.length)
            handle = tagPrefixes[handle]
        }
        val end = suffix.length
        val suffixText = if (end > 0) suffix.substring(0, end) else ""
        return if (handle != null) {
            handle + suffixText
        } else "!<$suffixText>"
    }

    private fun analyzeScalar(scalar: String): ScalarAnalysis {
        // Empty scalar is a special case.
        if (scalar.isEmpty()) {
            return ScalarAnalysis(
                scalar, empty = true, false, allowFlowPlain = false, allowBlockPlain = true,
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
                if ("#,[]{}&*!|>'\"%@`".indexOf(c.toChar()) != -1) {
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
                if (",?[]{}".indexOf(c.toChar()) != -1) {
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
        // Spaces at the beginning of a new line are only acceptable for block
        // scalars.
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

    private fun flushStream() {
        stream.flush()
    }

    private fun writeStreamStart() { // TODO maybe remove this function...?
        // BOM is written by Writer.
    }

    private fun writeStreamEnd() {
        flushStream()
    }

    private fun writeIndicator(
        indicator: String, needWhitespace: Boolean, whitespace: Boolean,
        indentation: Boolean,
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

    private fun writeIndent() {
        val indentToWrite = indent ?: 0
        if (!indention || column > indentToWrite || column == indentToWrite && !whitespace) {
            writeLineBreak(null)
        }
        writeWhitespace(indentToWrite - column)
    }

    private fun writeWhitespace(length: Int) {
        if (length <= 0) {
            return
        }
        whitespace = true
        for (i in 0 until length) {
            stream.write(" ")
        }
        column += length
    }

    private fun writeLineBreak(data: String?) {
        whitespace = true
        indention = true
        column = 0
        if (data == null) {
            stream.write(bestLineBreak)
        } else {
            stream.write(data)
        }
    }

    fun writeVersionDirective(versionText: String?) {
        stream.write("%YAML ")
        stream.write(versionText!!)
        writeLineBreak(null)
    }

    fun writeTagDirective(handleText: String, prefixText: String) {
        // XXX: not sure 4 invocations better than StringBuilders created by str
        // + str
        stream.write("%TAG ")
        stream.write(handleText)
        stream.write(SPACE)
        stream.write(prefixText)
        writeLineBreak(null)
    }


    //endregion

    //region Scalar streams.
    private fun writeSingleQuoted(text: String, split: Boolean) {
        writeIndicator(
            indicator = "'",
            needWhitespace = true,
            whitespace = false,
            indentation = false,
        )
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
                        writeLineBreak(null)
                    }
                    val data = text.substring(start, end)
                    for (br in data.toCharArray()) {
                        if (br == '\n') {
                            writeLineBreak(null)
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
        writeIndicator(indicator = "'", needWhitespace = false, whitespace = false, indentation = false)
    }

    private fun writeDoubleQuoted(text: String, split: Boolean) {
        writeIndicator(indicator = "\"", needWhitespace = true, whitespace = false, indentation = false)
        var start = 0
        var end = 0
        while (end <= text.length) {
            var ch: Char? = null
            if (end < text.length) {
                ch = text[end]
            }
            if (ch == null || "\"\\\u0085\u2028\u2029\uFEFF".indexOf(ch) != -1 || !('\u0020' <= ch && ch <= '\u007E')) {
                if (start < end) {
                    val len = end - start
                    column += len
                    stream.write(text, start, len)
                    start = end
                }
                if (ch != null) {
                    var data: String
                    if (ESCAPE_REPLACEMENTS.containsKey(ch)) {
                        data = "\\" + ESCAPE_REPLACEMENTS[ch]
                    } else {
                        val codePoint: Int = if (Character.isHighSurrogate(ch) && end + 1 < text.length) {
                            val ch2 = text[end + 1]
                            Character.toCodePoint(ch, ch2)
                        } else {
                            ch.code
                        }
                        if (allowUnicode && StreamReader.isPrintable(codePoint)) {
                            data = String(Character.toChars(codePoint))
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
                                val s = "000" + java.lang.Long.toHexString(codePoint.toLong())
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
                data = if (start >= end) {
                    "\\"
                } else {
                    text.substring(start, end) + "\\"
                }
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
        writeIndicator(indicator = "\"", needWhitespace = false, whitespace = false, indentation = false)
    }

    private fun writeCommentLines(commentLines: List<CommentLine>): Boolean {
        var wroteComment = false
        if (emitComments) {
            var indentColumns = 0
            var firstComment = true
            for (commentLine in commentLines) {
                if (commentLine.commentType != CommentType.BLANK_LINE) {
                    if (firstComment) {
                        firstComment = false
                        writeIndicator(
                            "#", commentLine.commentType == CommentType.IN_LINE,
                            whitespace = false,
                            indentation = false,
                        )
                        indentColumns = if (column > 0) column - 1 else 0
                    } else {
                        writeWhitespace(indentColumns)
                        writeIndicator(indicator = "#", needWhitespace = false, whitespace = false, indentation = false)
                    }
                    stream.write(commentLine.value)
                    writeLineBreak(null)
                } else {
                    writeLineBreak(null)
                    writeIndent()
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
        if (CharConstants.LINEBR.has(text[0].code, " ")) {
            hints.append(bestIndent)
        }
        val ch1 = text[text.length - 1]
        if (CharConstants.LINEBR.hasNo(ch1.code)) {
            hints.append("-")
        } else if (text.length == 1 || CharConstants.LINEBR.has(text[text.length - 2].code)) {
            hints.append("+")
        }
        return hints.toString()
    }

    private fun writeFolded(text: String, split: Boolean) {
        val hints = determineBlockHints(text)
        writeIndicator(indicator = ">$hints", needWhitespace = true, whitespace = false, indentation = false)
        if (hints.isNotEmpty() && hints[hints.length - 1] == '+') {
            openEnded = true
        }
        if (!writeInlineComments()) {
            writeLineBreak(null)
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
                        writeLineBreak(null)
                    }
                    leadingSpace = ch == ' '
                    val data = text.substring(start, end)
                    for (br in data.toCharArray()) {
                        if (br == '\n') {
                            writeLineBreak(null)
                        } else {
                            writeLineBreak(br.toString())
                        }
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
                        writeLineBreak(null)
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
        writeIndicator(indicator = "|$hints", needWhitespace = true, whitespace = false, indentation = false)
        if (hints.isNotEmpty() && hints[hints.length - 1] == '+') {
            openEnded = true
        }
        if (!writeInlineComments()) {
            writeLineBreak(null)
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
                            writeLineBreak(null)
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
                        writeLineBreak(null)
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
                        writeLineBreak(null)
                    }
                    val data = text.substring(start, end)
                    for (br in data.toCharArray()) {
                        if (br == '\n') {
                            writeLineBreak(null)
                        } else {
                            writeLineBreak(br.toString())
                        }
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
            '\u0000' to "0",
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
            '\u2028' to "L",
            '\u2029' to "P",
        )

        private val DEFAULT_TAG_PREFIXES: Map<String, String> = mapOf(
            "!" to "!",
            Tag.PREFIX to "!!",
        )

        /** indent cannot be zero spaces */
        const val MIN_INDENT = 1

        /** indent should not be more than 10 spaces */
        const val MAX_INDENT = 10

        private const val SPACE = " "

        private val HANDLE_FORMAT = Pattern.compile("^![-_\\w]*!$")
    }
}
