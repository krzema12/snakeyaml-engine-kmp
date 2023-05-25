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

import org.snakeyaml.engine.v2.events.Event

/**
 * This interface represents an input stream of [Events][Event].
 *
 * The parser and the scanner form together the 'Parse' step in the loading process.
 *
 * @see [Figure 3.1. Processing Overview](https://yaml.org/spec/1.2.2/.31-processes)
 */
interface Parser : Iterator<Event> {

    /**
     * Check if the next event is one of the given type.
     *
     * @param choice Event ID to match
     * @returns `true` if the next event has the given ID. Returns `false` if no
     * more events are available.
     * @throws org.snakeyaml.engine.v2.exceptions.ParserException in case of malformed input.
     */
    fun checkEvent(choice: Event.ID): Boolean

    /**
     * Return the next event, but do not delete it from the stream.
     *
     * @returns the event that will be returned on the next call to [next],
     *          or `null` if the end has been reached
     * @throws org.snakeyaml.engine.v2.exceptions.ParserException in case of malformed input
     * or [NoSuchElementException] in case no event is available.
     */
    fun peekEvent(): Event?

    /**
     * Returns the next event.
     *
     * The event will be removed from the stream.
     *
     * @returns the next parsed event
     * @throws org.snakeyaml.engine.v2.exceptions.ParserException in case of malformed input.
     */
    override fun next(): Event
}
