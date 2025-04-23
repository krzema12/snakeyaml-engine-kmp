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
package it.krzeminski.snakeyaml.engine.kmp.parser

import it.krzeminski.snakeyaml.engine.kmp.events.Event

/**
 * This interface represents an input stream of [Events][Event].
 *
 * The parser and the scanner form together the 'Parse' step in the loading process.
 *
 * See [Figure 3.1. Processing Overview](https://yaml.org/spec/1.2.2/#31-processes).
 */
interface Parser : Iterator<Event> {

    /**
     * Check if the next event is one of the given type.
     *
     * @param choice Event ID to match.
     * @returns `true` if the next event has the given ID. Returns `false` otherwise.
     * @throws it.krzeminski.snakeyaml.engine.kmp.exceptions.ParserException in case of malformed input.
     * @throws NoSuchElementException in case no next event is available.
     */
    fun checkEvent(choice: Event.ID): Boolean

    /**
     * Return the next event, but do not delete it from the stream.
     *
     * @returns the event that will be returned on the next call to [next].
     * @throws it.krzeminski.snakeyaml.engine.kmp.exceptions.ParserException in case of malformed input.
     * @throws NoSuchElementException in case no next event is available.
     */
    fun peekEvent(): Event

    /**
     * Returns the next event.
     *
     * The event will be removed from the stream.
     *
     * @returns the next parsed event
     * @throws it.krzeminski.snakeyaml.engine.kmp.exceptions.ParserException in case of malformed input.
     * @throws NoSuchElementException in case no next event is available.
     */
    override fun next(): Event
}
