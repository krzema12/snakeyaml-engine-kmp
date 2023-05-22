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
package org.snakeyaml.engine.v2.comments

import org.snakeyaml.engine.v2.events.CommentEvent
import org.snakeyaml.engine.v2.events.Event
import org.snakeyaml.engine.v2.parser.Parser
import java.util.*

/**
 * Used by the Composer and Emitter to collect comment events so that they can be used at a later
 * point in the process.
 */
class CommentEventsCollector {
    private val eventSource: Queue<Event>
    private val expectedCommentTypes: Array<CommentType>
    private var commentLineList: MutableList<CommentLine>

    /**
     * Constructor used to collect comment events emitted by a Parser.
     *
     * @param parser               the event source.
     * @param expectedCommentTypes the comment types expected. Any comment types not included are not
     * collected.
     */
    constructor(parser: Parser, vararg expectedCommentTypes: CommentType) {
        eventSource = object : AbstractQueue<Event>() {
            override fun offer(e: Event): Boolean {
                throw UnsupportedOperationException()
            }

            override fun poll(): Event {
                return parser.next()
            }

            override fun peek(): Event {
                return parser.peekEvent()
            }

            override fun iterator(): MutableIterator<Event> {
                throw UnsupportedOperationException()
            }

            override val size: Int
                get() = throw UnsupportedOperationException()
        }
        this.expectedCommentTypes = expectedCommentTypes as Array<CommentType>
        commentLineList = ArrayList()
    }

    /**
     * Constructor used to collect events emitted by the Serializer.
     *
     * @param eventSource          the event source.
     * @param expectedCommentTypes the comment types expected. Any comment types not included are not
     * collected.
     */
    constructor(eventSource: Queue<Event>, vararg expectedCommentTypes: CommentType) {
        this.eventSource = eventSource
        this.expectedCommentTypes = expectedCommentTypes as Array<CommentType>
        commentLineList = ArrayList()
    }

    /**
     * Determine if the event is a comment of one of the expected types set during construction.
     *
     * @param event the event to test.
     * @return `true` if the events is a comment of the expected type; Otherwise, false.
     */
    private fun isEventExpected(event: Event?): Boolean {
        if (event == null || event.eventId !== Event.ID.Comment) {
            return false
        }
        val commentEvent = event as CommentEvent
        for (type in expectedCommentTypes) {
            if (commentEvent.commentType == type) {
                return true
            }
        }
        return false
    }

    /**
     * Collect all events of the expected type (set during construction) starting with the top event
     * on the event source. Collection stops as soon as a non comment or comment of the unexpected
     * type is encountered.
     *
     * @return this object.
     */
    fun collectEvents(): CommentEventsCollector {
        collectEvents(null)
        return this
    }

    /**
     * Collect all events of the expected type (set during construction) starting with event provided
     * as an argument and continuing with the top event on the event source. Collection stops as soon
     * as a non comment or comment of the unexpected type is encountered.
     *
     * @param event the first event to attempt to collect.
     * @return the event provided as an argument, if it is not collected; Otherwise, `null`
     */
    fun collectEvents(event: Event?): Event? {
        if (event != null) {
            if (isEventExpected(event)) {
                commentLineList.add(CommentLine(event as CommentEvent))
            } else {
                return event
            }
        }
        while (isEventExpected(eventSource.peek())) {
            val e = Objects.requireNonNull(eventSource.poll())
            commentLineList.add(CommentLine(e as CommentEvent))
        }
        return null
    }

    /**
     * Collect all events of the expected type (set during construction) starting with event provided
     * as an argument and continuing with the top event on the event source. Collection stops as soon
     * as a non comment or comment of the unexpected type is encountered.
     *
     * @param event the first event to attempt to collect.
     * @return the event provided as an argument, if it is not collected; Otherwise, the first event
     * that is not collected.
     */
    fun collectEventsAndPoll(event: Event?): Event {
        val nextEvent = collectEvents(event)
        return nextEvent ?: eventSource.poll()
    }

    /**
     * Return the events collected and reset the collector.
     *
     * @return the events collected.
     */
    fun consume(): List<CommentLine> {
        return try {
            commentLineList
        } finally {
            commentLineList = ArrayList()
        }
    }

    val isEmpty: Boolean
        /**
         * Test if the collector contains any collected events.
         *
         * @return `true` if it does; Otherwise, `false`
         */
        get() = commentLineList.isEmpty()
}
