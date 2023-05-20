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
package org.snakeyaml.engine.v2.nodes

import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.exceptions.Mark
import java.util.*

/**
 * Represents a scalar node.
 *
 * Scalar nodes form the leaves in the node graph.
 *
 * @param[tag]
 * @param[resolved]
 * @param[value] Value of this scalar
 * @param[scalarStyle] style of this scalar node
 * Flow styles - https://yaml.org/spec/1.2/spec.html.id2786942
 * Block styles - https://yaml.org/spec/1.2/spec.html.id2793652
 * @see org.snakeyaml.engine.v2.events.ScalarEvent
 */
class ScalarNode @JvmOverloads constructor(
  tag: Tag,
  resolved: Boolean = true,
  val value: String,
  val scalarStyle: ScalarStyle,
  startMark: Optional<Mark> = Optional.empty<Mark>(),
  endMark: Optional<Mark> = Optional.empty<Mark>(),
) : Node(tag, startMark, endMark, resolved = resolved) {

  override val nodeType: NodeType
    get() = NodeType.SCALAR

  override fun toString(): String = "<${this.javaClass.name} (tag=$tag, value=$value)>"

  val isPlain: Boolean
    get() = scalarStyle == ScalarStyle.PLAIN
}
