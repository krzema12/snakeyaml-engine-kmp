package it.krzeminski.snakeyaml.engine.kmp.usecases.inherited

import it.krzeminski.snakeyaml.engine.kmp.common.Anchor
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.common.SpecVersion
import it.krzeminski.snakeyaml.engine.kmp.events.AliasEvent
import it.krzeminski.snakeyaml.engine.kmp.events.DocumentEndEvent
import it.krzeminski.snakeyaml.engine.kmp.events.DocumentStartEvent
import it.krzeminski.snakeyaml.engine.kmp.events.Event
import it.krzeminski.snakeyaml.engine.kmp.events.ImplicitTuple
import it.krzeminski.snakeyaml.engine.kmp.events.MappingEndEvent
import it.krzeminski.snakeyaml.engine.kmp.events.MappingStartEvent
import it.krzeminski.snakeyaml.engine.kmp.events.ScalarEvent
import it.krzeminski.snakeyaml.engine.kmp.events.SequenceEndEvent
import it.krzeminski.snakeyaml.engine.kmp.events.SequenceStartEvent
import it.krzeminski.snakeyaml.engine.kmp.events.StreamEndEvent
import it.krzeminski.snakeyaml.engine.kmp.events.StreamStartEvent
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag
import it.krzeminski.snakeyaml.engine.kmp.tokens.AliasToken
import it.krzeminski.snakeyaml.engine.kmp.tokens.AnchorToken
import it.krzeminski.snakeyaml.engine.kmp.tokens.ScalarToken
import it.krzeminski.snakeyaml.engine.kmp.tokens.TagToken
import it.krzeminski.snakeyaml.engine.kmp.tokens.Token

class CanonicalParser(data: String, private val label: String) {
    private val scanner = CanonicalScanner(data, label)
    private val events = mutableListOf<Event>()
    private var parsed = false

    // stream: STREAM-START document* STREAM-END
    private fun parseStream() {
        scanner.getToken(Token.ID.StreamStart)
        events.add(StreamStartEvent(null, null))
        while (!scanner.checkToken(Token.ID.StreamEnd)) {
            if (scanner.checkToken(Token.ID.Directive, Token.ID.DocumentStart)) {
                parseDocument()
            } else {
                throw CanonicalException("document is expected, got ${scanner.tokens[0]} in $label")
            }
        }
        scanner.getToken(Token.ID.StreamEnd)
        events.add(StreamEndEvent(null, null))
    }

    // document: DIRECTIVE? DOCUMENT-START node
    private fun parseDocument() {
        if (scanner.checkToken(Token.ID.Directive)) {
            scanner.getToken(Token.ID.Directive)
        }
        scanner.getToken(Token.ID.DocumentStart)
        events.add(DocumentStartEvent(true, SpecVersion(1, 2), emptyMap(), null, null))
        parseNode();
        if (scanner.checkToken(Token.ID.DocumentEnd)) {
            scanner.getToken(Token.ID.DocumentEnd)
        }
        events.add(DocumentEndEvent(true, null, null))
    }

    // node: ALIAS | ANCHOR? TAG? (SCALAR|sequence|mapping)
    private fun parseNode() {
        if (scanner.checkToken(Token.ID.Alias)) {
            val token = scanner.next() as AliasToken
            events.add(AliasEvent(token.value, null, null))
        } else {
            var anchor: Anchor? = null
            if (scanner.checkToken(Token.ID.Anchor)) {
                val token = scanner.next() as AnchorToken
                anchor = token.value
            }
            var tag: String? = null
            if (scanner.checkToken(Token.ID.Tag)) {
                var token = scanner.next() as TagToken
                tag = token.value.handle + token.value.suffix
            }
            if (scanner.checkToken(Token.ID.Scalar)) {
                val token = scanner.next() as ScalarToken
                events.add(
                    ScalarEvent(
                        anchor, tag, ImplicitTuple(false, false), token.value,
                        ScalarStyle.PLAIN, null, null
                    )
                )
            } else if (scanner.checkToken(Token.ID.FlowSequenceStart)) {
                events.add(
                    SequenceStartEvent(
                        anchor, Tag.SEQ.value, false,
                        FlowStyle.AUTO, null, null
                    )
                )
                parseSequence();
            } else if (scanner.checkToken(Token.ID.FlowMappingStart)) {
                events.add(
                    MappingStartEvent(
                        anchor, Tag.MAP.value, false,
                        FlowStyle.AUTO, null, null
                    )
                )
                parseMapping();
            } else {
                throw CanonicalException("SCALAR, '[', or '{' is expected, got ${scanner.tokens[0]}")
            }
        }
    }

    // sequence: SEQUENCE-START (node (ENTRY node)*)? ENTRY? SEQUENCE-END
    private fun parseSequence() {
        scanner.getToken(Token.ID.FlowSequenceStart)
        if (!scanner.checkToken(Token.ID.FlowSequenceEnd)) {
            parseNode()
            while (!scanner.checkToken(Token.ID.FlowSequenceEnd)) {
                scanner.getToken(Token.ID.FlowEntry)
                if (!scanner.checkToken(Token.ID.FlowSequenceEnd)) {
                    parseNode()
                }
            }
        }
        scanner.getToken(Token.ID.FlowSequenceEnd)
        events.add(SequenceEndEvent(null, null))
    }

    // mapping: MAPPING-START (map_entry (ENTRY map_entry)*)? ENTRY? MAPPING-END
    private fun parseMapping() {
        scanner.getToken(Token.ID.FlowMappingStart)
        if (!scanner.checkToken(Token.ID.FlowMappingEnd)) {
            parseMapEntry()
            while (!scanner.checkToken(Token.ID.FlowMappingEnd)) {
                scanner.getToken(Token.ID.FlowEntry)
                if (!scanner.checkToken(Token.ID.FlowMappingEnd)) {
                    parseMapEntry()
                }
            }
        }
        scanner.getToken(Token.ID.FlowMappingEnd)
        events.add(MappingEndEvent(null, null))
    }

    // map_entry: KEY node VALUE node
    private fun parseMapEntry() {
        scanner.getToken(Token.ID.Key);
        parseNode();
        scanner.getToken(Token.ID.Value);
        parseNode();
    }

    fun parse() {
        parseStream()
        parsed = true
    }

    fun next(): Event {
        if (!parsed) {
            parse()
        }
        return events.removeAt(0)
    }

    /**
     * Check the type of the next event.
     */
    fun checkEvent(choice: Event.ID): Boolean {
        if (!parsed) {
            parse()
        }
        if (!events.isEmpty()) {
            return events[0].eventId == choice
        }
        return false
    }

    /**
     * Get the next event.
     */
    fun peekEvent(): Event {
        if (!parsed) {
            parse()
        }
        if (events.isEmpty()) {
            throw NoSuchElementException("No more Events found.")
        } else {
            return events[0]
        }
    }

    fun hasNext(): Boolean {
        if (!parsed) {
            parse()
        }
        return !events.isEmpty()
    }
}
