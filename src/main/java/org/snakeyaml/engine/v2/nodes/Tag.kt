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

import org.snakeyaml.engine.v2.common.UriEncoder
import kotlin.reflect.KClass

class Tag {
    val value: String

    constructor(tag: String) {
        require(tag.isNotEmpty()) { "Tag must not be empty." }
        require(tag.trim { it <= ' ' }.length == tag.length) { "Tag must not contain leading or trailing spaces." }
        value = UriEncoder.encode(tag)
    }

    /**
     * Create a global tag to dump the fully qualified class name
     *
     * @param clazz - the class to use the name
     */
    constructor(clazz: KClass<*>) {
        value = PREFIX + UriEncoder.encode(clazz.qualifiedName!!)
    }

    override fun toString(): String = value

    override fun equals(other: Any?): Boolean =
        when (other) {
            is Tag -> value == other.value
            else   -> false
        }

    override fun hashCode(): Int = value.hashCode()

    companion object {
        const val PREFIX = "tag:yaml.org,2002:"

        @JvmField
        val SET = Tag(PREFIX + "set")

        @JvmField
        val BINARY = Tag(PREFIX + "binary")

        @JvmField
        val INT = Tag(PREFIX + "int")

        @JvmField
        val FLOAT = Tag(PREFIX + "float")

        @JvmField
        val BOOL = Tag(PREFIX + "bool")

        @JvmField
        val NULL = Tag(PREFIX + "null")

        @JvmField
        val STR = Tag(PREFIX + "str")

        @JvmField
        val SEQ = Tag(PREFIX + "seq")

        @JvmField
        val MAP = Tag(PREFIX + "map")

        /** Used to indicate a DUMMY node that contains comments, when there is no other (empty document) */
        @JvmField
        val COMMENT = Tag(PREFIX + "comment")

        @JvmField
        val ENV_TAG = Tag("!ENV_VARIABLE")
    }
}
