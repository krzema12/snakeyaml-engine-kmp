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

import it.krzeminski.snakeyaml.engine.kmp.comments.CommentType
import it.krzeminski.snakeyaml.engine.kmp.exceptions.Mark

/**
 * Marks a comment block value.
 */
class CommentEvent(
    val commentType: CommentType,
    /**
     * String representation of the value, without quotes and escaping.
     *
     * @return Value a comment line string without the leading '#' or a blank line.
     */
    val value: String,
    startMark: Mark?,
    endMark: Mark?,
) : Event(startMark, endMark) {

    override val eventId: ID
        get() = ID.Comment

    override fun toString(): String = "=COM $commentType $value"
}
