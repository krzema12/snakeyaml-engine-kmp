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
package org.snakeyaml.engine.v2.emitter;

import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.api.StreamDataWriter
import org.snakeyaml.engine.v2.comments.CommentEventsCollector
import org.snakeyaml.engine.v2.comments.CommentType
import org.snakeyaml.engine.v2.common.Anchor
import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.events.Event
import org.snakeyaml.engine.v2.nodes.Tag
import java.util.Optional


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
    private val emitterJava = EmitterJava(opts, stream)

    /** [Emitter] is a state machine with a stack of states to handle nested structures. */
    private val states: ArrayDeque<EmitterState> = ArrayDeque(100)

    /** current state */
    private var state: EmitterState = ExpectStreamStart()

    /** The event queue */
    private val events: ArrayDeque<Event> = ArrayDeque(100)

    /** Current event */
    private var event: Event? = null

    /** The stack of previous indents */
    private val indents: ArrayDeque<Int> = ArrayDeque(100)

    /** The current indentation level. Can be `null` to choose the best */
    private var indent: Int? = null

    /** Flow level. */
    private var flowLevel = 0

    //region Contexts
    private val rootContext = false
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
    private val bestIndent = if (opts.indent in (MIN_INDENT + 1) until MAX_INDENT) opts.indent else 2
    private val indicatorIndent = opts.indicatorIndent
    private val indentWithIndicator = opts.indentWithIndicator
    private val bestWidth = if (opts.width > 80 * 2) opts.width else 80
    private val bestLineBreak = opts.bestLineBreak
    private val splitLines = opts.isSplitLines
    private val maxSimpleKeyLength = opts.maxSimpleKeyLength
    private val emitComments = opts.dumpComments
    //endregion

    /** Tag prefixes. */
    private val tagPrefixes: Map<String, String> = emptyMap()

    /** Prepared anchor and tag. */
    private var preparedAnchor: Optional<Anchor> = Optional.empty()
    private var preparedTag: String? = null

    /** Scalar analysis */
    private var analysis: ScalarAnalysis? = null

    /** Scalar style */
    private var scalarStyle: Optional<ScalarStyle> = Optional.empty()

    /** Comment processing */
    private var blockCommentsCollector: CommentEventsCollector? = null
    private var inlineCommentsCollector: CommentEventsCollector? = null

    init {
        // Comment processing
        blockCommentsCollector = CommentEventsCollector(events, CommentType.BLANK_LINE, CommentType.BLOCK)
        inlineCommentsCollector = CommentEventsCollector(events, CommentType.IN_LINE)
    }

    override fun emit(event: Event): Unit = emitterJava.emit(event)

    //region States
    //region Stream handlers.
    private inner class ExpectStreamStart : EmitterState {
        override fun expect() = TODO("Not yet implemented")
    }

    private inner class ExpectNothing : EmitterState {
        override fun expect() = TODO("Not yet implemented")
    }

    //endregion
    //region Document handlers.
    private inner class ExpectFirstDocumentStart : EmitterState {
        override fun expect() = TODO("Not yet implemented")
    }

    private inner class ExpectDocumentStart : EmitterState {
        override fun expect() = TODO("Not yet implemented")
    }

    private inner class ExpectDocumentEnd : EmitterState {
        override fun expect() = TODO("Not yet implemented")
    }

    private inner class ExpectDocumentRoot : EmitterState {
        override fun expect() = TODO("Not yet implemented")
    }

    //endregion
    //region Node handlers.
    //endregion
    //region Flow sequence handlers.
    private inner class ExpectFirstFlowSequenceItem : EmitterState {
        override fun expect() = TODO("Not yet implemented")
    }

    private inner class ExpectFlowSequenceItem : EmitterState {
        override fun expect() = TODO("Not yet implemented")
    }
    //endregion
    //region Flow mapping handlers.
    private inner class ExpectFirstFlowMappingKey : EmitterState {
        override fun expect() = TODO("Not yet implemented")
    }

    private inner class ExpectFlowMappingKey : EmitterState {
        override fun expect() = TODO("Not yet implemented")
    }


    private inner class ExpectFlowMappingSimpleValue : EmitterState {
        override fun expect() = TODO("Not yet implemented")
    }

    private inner class ExpectFlowMappingValue : EmitterState {
        override fun expect() = TODO("Not yet implemented")
    }
    //endregion
    //region Block sequence handlers.

    private inner class ExpectFirstBlockMappingKey : EmitterState {
        override fun expect() = TODO("Not yet implemented")
    }

    private inner class ExpectBlockMappingKey : EmitterState {
        override fun expect() = TODO("Not yet implemented")
    }

    private inner class ExpectBlockMappingSimpleValue : EmitterState {
        override fun expect() = TODO("Not yet implemented")
    }

    private inner class ExpectBlockMappingValue : EmitterState {
        override fun expect() = TODO("Not yet implemented")
    }


    private inner class ExpectFirstBlockSequenceItem : EmitterState {
        override fun expect() = TODO("Not yet implemented")
    }

    private inner class ExpectBlockSequenceItem : EmitterState {
        override fun expect() = TODO("Not yet implemented")
    }
    //endregion
    //region Checkers.
    //endregion
    //region Anchor, Tag, and Scalar processors.
    //endregion
    //region Analyzers.
    //endregion
    //region Writers.
    //endregion
    //region Scalar streams.
    //endregion
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
    }
}
