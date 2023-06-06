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
import org.snakeyaml.engine.v2.exceptions.Mark
import kotlin.jvm.JvmOverloads

/**
 * Marks the inclusion of a previously anchored node.
 */
class AliasEvent @JvmOverloads constructor(
    anchor: Anchor?,
    startMark: Mark? = null,
    endMark: Mark? = null,
) : NodeEvent(anchor, startMark, endMark) {
    val alias: Anchor

    init {
        alias = anchor ?: throw NullPointerException("Anchor is required in AliasEvent")
    }

    override val eventId: ID
        get() = ID.Alias

    override fun toString(): String = "=ALI *$alias"
}
