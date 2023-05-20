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

import org.snakeyaml.engine.v2.common.Anchor
import org.snakeyaml.engine.v2.common.FlowStyle
import org.snakeyaml.engine.v2.exceptions.Mark
import java.util.Optional

/**
 * Marks the beginning of a sequence node.
 *
 * This event is followed by the elements contained in the sequence, and a [SequenceEndEvent].
 *
 * @see SequenceEndEvent
 */
class SequenceStartEvent @JvmOverloads constructor(
    anchor: Optional<Anchor>,
    tag: Optional<String>,
    implicit: Boolean,
    flowStyle: FlowStyle,
    startMark: Optional<Mark> = Optional.empty(),
    endMark: Optional<Mark> = Optional.empty(),
) : CollectionStartEvent(anchor, tag, implicit, flowStyle, startMark, endMark) {

    override val eventId: ID
        get() = ID.SequenceStart

    override fun toString(): String {
        return buildString {
            append("+SEQ")
            if (flowStyle == FlowStyle.FLOW) {
                append(" []")
            }
            append(super.toString())
        }
    }
}
