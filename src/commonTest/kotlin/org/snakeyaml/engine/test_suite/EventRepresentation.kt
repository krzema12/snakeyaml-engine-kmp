package org.snakeyaml.engine.test_suite

import org.snakeyaml.engine.v2.common.FlowStyle
import org.snakeyaml.engine.v2.events.*
import org.snakeyaml.engine.v2.nodes.Tag

/**
 * Event representation for the external test suite
 */
class EventRepresentation(private val event: Event) {
    val representation: String
        get() = event.toString()

    fun isSameAs(data: String): Boolean {
        val split: List<String> =data.split(" ")
        if (!event.toString().startsWith(split[0])) {
            return false
        }

//        when (event) {
//            is CollectionEndEvent -> TODO()
//            is CommentEvent -> TODO()
//            is DocumentEndEvent -> TODO()
//            is DocumentStartEvent -> TODO()
//            is NodeEvent -> TODO()
//            is StreamEndEvent -> TODO()
//            is StreamStartEvent -> TODO()
//        }

        if (event is MappingStartEvent) {
            val e = event as CollectionStartEvent
            val tagIsPresent = e.tag != null
            val mapTag = Tag.MAP.value
            if (tagIsPresent && mapTag != e.tag) {
                val last = split[split.size - 1]
                if (last != "<${e.tag}>") {
                    return false
                }
            }
        }
        if (event is SequenceStartEvent) {
            val e = event
            if (e.tag != null && Tag.SEQ.value != e.tag) {
                val last = split[split.size - 1]
                if (last != "<${e.tag}>") {
                    return false
                }
            }
        }
        if (event is NodeEvent) {
            if (event.anchor != null) {
                var indexOfAlias = 1
                if (event.eventId == Event.ID.SequenceStart || event.eventId == Event.ID.MappingStart) {
                    val start = event as CollectionStartEvent
                    if (start.flowStyle === FlowStyle.FLOW) {
                        indexOfAlias = 2
                    }
                }
                if (event is AliasEvent) {
                    if (!split[indexOfAlias].startsWith("*")) {
                        return false
                    }
                } else {
                    if (!split[indexOfAlias].startsWith("&")) {
                        return false
                    }
                }
            }
        }
        if (event is ScalarEvent) {
             if (event.tag != null) {
                val implicit = event.implicit
                if (implicit.bothFalse()) {
                    if (!data.contains("<${event.tag}>")) {
                        return false
                    }
                }
            }
            val end = event.scalarStyle.toString() + event.escapedValue()
            return data.endsWith(end)
        }
        return true
    }
}
