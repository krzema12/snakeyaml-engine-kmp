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
package it.krzeminski.snakeyaml.engine.kmp.events

import it.krzeminski.snakeyaml.engine.kmp.common.Anchor
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.exceptions.Mark
import kotlin.jvm.JvmOverloads

/**
 * Marks the beginning of a mapping node.
 *
 *
 * This event is followed by a number of key value pairs. <br></br>
 * The pairs are not in any particular order. However, the value always directly follows the
 * corresponding key. <br></br>
 * After the key value pairs follows a [MappingEndEvent].
 *
 *
 *
 * There must be an even number of node events between the start and end event.
 *
 *
 * @see MappingEndEvent
 */
class MappingStartEvent @JvmOverloads constructor(
    anchor: Anchor?,
    tag: String?,
    implicit: Boolean,
    flowStyle: FlowStyle,
    startMark: Mark? = null,
    endMark: Mark? = null,
) : CollectionStartEvent(anchor, tag, implicit, flowStyle, startMark, endMark) {

    override val eventId: ID
        get() = ID.MappingStart

    override fun toString(): String {
        return buildString {
            append("+MAP")
            if (flowStyle == FlowStyle.FLOW) {
                append(" {}")
            }
            append(super.toString())
        }
    }
}
