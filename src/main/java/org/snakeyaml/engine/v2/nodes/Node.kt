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

import org.snakeyaml.engine.v2.comments.CommentLine
import org.snakeyaml.engine.v2.common.Anchor
import org.snakeyaml.engine.v2.exceptions.Mark
import java.util.*

/**
 * Base class for all nodes.
 *
 * The nodes form the node-graph described in the [YAML Specification](https://yaml.org/spec/1.2/spec.html).
 *
 * While loading, the node graph is usually created by the [org.snakeyaml.engine.v2.composer.Composer].
 *
 * @param tag - the tag
 * @param startMark - start mark when available
 * @param endMark - end mark when available
 */
abstract class Node @JvmOverloads constructor(

  /**
   * Tag of this node.
   *
   *
   * Every node has a tag assigned. The tag is either local or global.
   *
   * @return Tag of this node.
   */
  var tag: Tag,

  val startMark: Optional<Mark>,

  @JvmField
  protected var endMark: Optional<Mark>,

  /**
   * `true` when the tag is assigned by the resolver
   */
  @JvmField
  protected var resolved: Boolean = true,
) {


  /**
   * Indicates if this node must be constructed in two steps.
   *
   *
   * Two-step construction is required whenever a node is a child (direct or indirect) of it self.
   * That is, if a recursive structure is build using anchors and aliases.
   *
   *
   *
   * Set by [org.snakeyaml.engine.v2.composer.Composer], used during the construction process.
   *
   *
   *
   * Only relevant during loading.
   *
   *
   * @return `true` if the node is self referenced.
   */
  var isRecursive: Boolean = false

  /**
   * Get the anchor if it was defined for this Node
   *
   * @return the Anchor if available
   * @see [3.2.2.2. Anchors and Aliases](https://yaml.org/spec/1.2/spec.html.id2765878)
   */

  /**
   * The anchor for this Node
   *
   * @see [3.2.2.2. Anchors and Aliases](https://yaml.org/spec/1.2/spec.html.id2765878)
   */
  var anchor: Optional<Anchor> = Optional.empty()

  /**
   * The ordered list of in-line comments. The first of which appears at the end of the line
   * respresent by this node. The rest are in the following lines, indented per the Spec to indicate
   * they are continuation of the inline comment.
   *
   * @return the comment line list.
   */
  var inLineComments: List<CommentLine>? = null

  /**
   * The ordered list of blank lines and block comments (full line) that appear before this node.
   *
   * @return the comment line list.
   */
  var blockComments: List<CommentLine>? = null

  /**
   * The ordered list of blank lines and block comments (full line) that appear AFTER this node.
   *
   *
   * NOTE: these comment should occur only in the last node in a document, when walking the node
   * tree "in order"
   *
   * @return the comment line list.
   */
  // End Comments are only on the last node in a document
  var endComments: List<CommentLine>? = null

  private var properties: MutableMap<String, Any>? = null

  /**
   * @return scalar, sequence, mapping
   */
  abstract val nodeType: NodeType

  /**
   * Define a custom runtime property. It is not used by Engine but may be used by other tools.
   *
   * @param key - the key for the custom property
   * @param value - the value for the custom property
   * @return the previous value for the provided key if it was defined
   */
  fun setProperty(key: String, value: Any): Any? {
    if (properties == null) {
      properties = HashMap()
    }
    return properties!!.put(key, value)
  }

  /**
   * Get the custom runtime property.
   *
   * @param key - the key of the runtime property
   * @return the value if it was specified
   */
  fun getProperty(key: String): Any? = properties?.get(key)

  fun getEndMark(): Optional<Mark> = endMark // Java interop function, to be removed when everything is Kotlin
}
