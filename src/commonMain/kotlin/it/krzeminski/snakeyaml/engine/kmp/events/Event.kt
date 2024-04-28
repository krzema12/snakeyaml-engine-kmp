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

import it.krzeminski.snakeyaml.engine.kmp.exceptions.Mark
import kotlin.jvm.JvmOverloads

/**
 * Basic unit of output from a [it.krzeminski.snakeyaml.engine.kmp.parser.Parser] or input of a
 * [it.krzeminski.snakeyaml.engine.kmp.emitter.Emitter].
 */
abstract class Event @JvmOverloads constructor(
    val startMark: Mark? = null,
    val endMark: Mark? = null,
) {

    init {
        if (startMark != null && endMark == null || startMark == null && endMark != null) {
            throw NullPointerException("Both marks must be either present or absent.")
        }
    }

    /**
     * Get the type (kind) of this Event
     *
     * @return the [ID] of this Event
     */
    abstract val eventId: ID

    /**
     * ID of a non-abstract Event
     */
    enum class ID {
        Alias,
        Comment,
        DocumentEnd,
        DocumentStart,
        MappingEnd,
        MappingStart,
        Scalar,
        SequenceEnd,
        SequenceStart,
        StreamEnd,
        StreamStart,
    }
}
