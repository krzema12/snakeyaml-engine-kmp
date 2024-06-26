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

import it.krzeminski.snakeyaml.engine.kmp.common.SpecVersion
import it.krzeminski.snakeyaml.engine.kmp.exceptions.Mark
import kotlin.jvm.JvmOverloads

/**
 * Marks the beginning of a document.
 *
 *
 * This event followed by the document's content and a [DocumentEndEvent].
 *
 */
class DocumentStartEvent @JvmOverloads constructor(
    val explicit: Boolean,
    /**
     * @return YAML version the document conforms to.
     */
    val specVersion: SpecVersion?,

    /**
     * Tag shorthands as defined by the `%TAG` directive.
     *
     * @return Mapping of 'handles' to 'prefixes' (the handles include the '!' characters).
     */
    val tags: Map<String, String>,
    startMark: Mark? = null,
    endMark: Mark? = null,
) : Event(startMark, endMark) {

    override val eventId: ID
        get() = ID.DocumentStart

    override fun toString(): String {
        return buildString {
            append("+DOC")
            if (explicit) {
                append(" ---")
            }
        }
    }
}
