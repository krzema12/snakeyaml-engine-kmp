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
package org.snakeyaml.engine.v2.events

import org.snakeyaml.engine.v2.exceptions.Mark

/**
 * Marks the end of a mapping node.
 *
 * @see MappingStartEvent
 */
class MappingEndEvent : CollectionEndEvent {
    constructor(startMark: Mark?, endMark: Mark?) : super(startMark, endMark)
    constructor() : super()

    override val eventId: ID
        get() = ID.MappingEnd

    override fun toString(): String = "-MAP"
}
