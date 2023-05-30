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

/**
 * Base class for the start events of the collection nodes.
 *
 * @param[implicit] The implicit flag of a collection start event indicates if the tag may be omitted when the collection is emitted
 * @param[flowStyle] indicates if a collection is block or flow
 */
abstract class CollectionStartEvent(
    anchor: Anchor?,

    /**
     * Tag of this collection.
     *
     * @return The tag of this collection, or `empty` if no explicit tag is available.
     */
    val tag: String?,
    val implicit: Boolean,
    val flowStyle: FlowStyle,
    startMark: Mark?,
    endMark: Mark?,
) : NodeEvent(anchor, startMark, endMark) {

    /**
     * `true` if this collection is in flow style, `false` for block style.
     *
     * @return If this collection is in flow style.
     */
    fun isFlow(): Boolean = FlowStyle.FLOW == flowStyle

    /**
     * `true` if the tag can be omitted while this collection is emitted.
     *
     * @return True if the tag can be omitted while this collection is emitted.
     */
    fun isImplicit(): Boolean = implicit // temp-fix for Java interop, remove when everything is Kotlin

    override fun toString(): String {
        return buildString {
            anchor?.let { a -> append(" &$a") }
            if (!implicit) {
                tag?.let { theTag: String -> append(" <$theTag>") }
            }
        }
    }
}
