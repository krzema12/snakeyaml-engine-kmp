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

/**
 * Used by the Composer and Emitter to collect comment events so that they can be used at a later
 * point in the process.
 */
class CommentEventsCollector private constructor(
    private val eventSource: EventQueue,
    private val expectedCommentTypes: Array<out CommentType>,
) {
    private val commentLineList: MutableList<CommentLine> = mutableListOf()

    /**
     * Constructor used to collect comment events emitted by a Parser.
     *
     * @param parser               the event source.
     * @param expectedCommentTypes the comment types expected.
     *                             Any comment types not included are not collected.
     */
    constructor(
        parser: Parser,
        vararg expectedCommentTypes: CommentType,
    ) : this(
        EventQueue(parser),
        expectedCommentTypes,
    )

    /**
     * Constructor used to collect events emitted by the Serializer.
     *
     * @param eventSource          the event source.
     * @param expectedCommentTypes the comment types expected.
     *                             Any comment types not included are not collected.
     */
    constructor(
        eventSource: ArrayDeque<Event>,
        vararg expectedCommentTypes: CommentType,
    ) : this(
        EventQueue(eventSource),
        expectedCommentTypes,
    )

    /**
     * Determine if the event is a comment of one of the expected types set during construction.
     *
     * @param event the event to test.
     * @return `true` if the events is a comment of the expected type; Otherwise, `false`.
     */
    private fun isEventExpected(event: Event?): Boolean {
        return event != null
                && event.eventId == Event.ID.Comment
                && event is CommentEvent
                && event.commentType in expectedCommentTypes
    }

    /**
     * Collect all events of the expected type (set during construction) starting with the top event
     * on the event source. Collection stops as soon as a non comment or comment of the unexpected
     * type is encountered.
     *
     * @returns this instance.
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
            val e = eventSource.poll()
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
            commentLineList.toList()
        } finally {
            commentLineList.clear()
        }
    }

    /**
     * Test if the collector contains any collected events.
     *
     * @return `true` if it does; Otherwise, `false`
     */
    fun isEmpty(): Boolean = commentLineList.isEmpty()
}


private class EventQueue(
    private val pollFn: () -> Event,
    private val peekFn: () -> Event?,
) {
    constructor(parser: Parser) : this(
        pollFn = parser::next,
        peekFn = parser::peekEvent,
    )

    constructor(eventQueue: ArrayDeque<Event>) : this(
        pollFn = eventQueue::removeFirst,
        peekFn = eventQueue::firstOrNull,
    )

    fun poll(): Event = pollFn()
    fun peek(): Event? = peekFn()
}
