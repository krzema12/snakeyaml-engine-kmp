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

import org.snakeyaml.engine.v2.comments.CommentType
import org.snakeyaml.engine.v2.exceptions.Mark
import java.util.Objects
import java.util.Optional

/**
 * Marks a comment block value.
 */
class CommentEvent(
    type: CommentType, value: String, startMark: Optional<Mark>,
    endMark: Optional<Mark>,
) :
    Event(startMark, endMark) {
    /**
     * The comment type.
     *
     * @return the commentType.
     */
    val commentType: CommentType

    /**
     * String representation of the value.
     *
     *
     * Without quotes and escaping.
     *
     *
     * @return Value a comment line string without the leading '#' or a blank line.
     */
    val value: String

    init {
        Objects.requireNonNull(type)
        commentType = type
        Objects.requireNonNull(value)
        this.value = value
    }

    override val eventId: ID
        get() = ID.Comment

    override fun toString(): String {
        return "=COM $commentType $value"
    }
}