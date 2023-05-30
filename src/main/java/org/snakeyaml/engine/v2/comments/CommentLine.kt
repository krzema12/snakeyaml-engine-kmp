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

/**
 * A comment line. Maybe a block comment, blank line, or inline comment.
 * @param startMark - start position
 * @param endMark - end position
 * @param value - Value of this comment
 * @param commentType - the type
 */
class CommentLine(
    /**  */
    @JvmField
    val startMark: Mark?,
    @JvmField
    val endMark: Mark?,
    @JvmField
    val value: String,
    @JvmField
    val commentType: CommentType,
) {

    /**
     * Create
     *
     * @param event - the source
     */
    constructor(event: CommentEvent) : this(event.startMark, event.endMark, event.value, event.commentType)

    override fun toString(): String = "<${this.javaClass.name} (type=$commentType, value=$value)>"
}
