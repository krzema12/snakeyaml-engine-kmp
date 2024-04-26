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
package it.krzeminski.snakeyaml.engine.kmp.nodes

import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.exceptions.Mark
import kotlin.jvm.JvmOverloads

/**
 * Base class for the two collection types [mapping][MappingNode] and [ collection][SequenceNode].
 *
 * @param[flowStyle] Serialization style of this collection
 */
sealed class CollectionNode<T> @JvmOverloads constructor(
  tag: it.krzeminski.snakeyaml.engine.kmp.nodes.Tag,
  var flowStyle: FlowStyle,
  startMark: Mark?,
  endMark: Mark?,
  resolved: Boolean = true,
) : it.krzeminski.snakeyaml.engine.kmp.nodes.Node(tag, startMark, endMark, resolved = resolved) {

    /**
     * Returns the elements in this sequence.
     *
     * @return Nodes in the specified order.
     */
    abstract val value: List<T>?

    fun setEndMark(value: Mark?) {
        super.endMark = value
    }
}
