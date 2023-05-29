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
import org.snakeyaml.engine.v2.exceptions.Mark
import java.util.*

/**
 * A comment line. Maybe a block comment, blank line, or inline comment.
 */
class CommentLine(
    startMark: Optional<Mark>,
    endMark: Optional<Mark>,
    value: String,
    commentType: CommentType,
) {
    /**
     * getter
     *
     * @return start position
     */
    @JvmField
    val startMark: Optional<Mark>

    /**
     * getter
     *
     * @return end position
     */
    @JvmField
    val endMark: Optional<Mark>

    /**
     * Value of this comment.
     *
     * @return comment's value.
     */
    @JvmField
    val value: String

    /**
     * getter
     *
     * @return type of it
     */
    @JvmField
    val commentType: CommentType

    /**
     * Create
     *
     * @param event - the source
     */
    constructor(event: CommentEvent) : this(event.startMark, event.endMark, event.value, event.commentType)

    /**
     * Create
     *
     * @param startMark - start
     * @param endMark - end
     * @param value - the comment
     * @param commentType - the type
     */
    init {
        this.startMark = startMark
        this.endMark = endMark
        this.value = value
        this.commentType = commentType
    }

    override fun toString(): String {
        return "<" + this.javaClass.name + " (type=" + commentType + ", value=" + value + ")>"
    }
}
