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
package org.snakeyaml.engine.v2.common

import org.snakeyaml.engine.v2.exceptions.EmitterException
import java.util.*
import java.util.regex.Pattern

/**
 * Value inside Anchor and Alias
 * @param value anchor value
 */
class Anchor(val value: String) {

    init {
        require(value.isNotEmpty()) { "Empty anchor." }
        for (element in value) {
            if (INVALID_ANCHOR.contains(element)) {
                throw EmitterException("Invalid character '$element' in the anchor: $value")
            }
        }
        val matcher = SPACES_PATTERN.matcher(value)
        if (matcher.find()) {
            throw EmitterException("Anchor may not contain spaces: $value")
        }
    }

    override fun toString(): String = value

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val anchor1 = other as Anchor
        return value == anchor1.value
    }

    override fun hashCode(): Int {
        return Objects.hash(value)
    }

    companion object {
        private val INVALID_ANCHOR: Set<Char> = setOf(
            '[',
            ']',
            '{',
            '}',
            ',',
            '*',
            '&',
        )
        private val SPACES_PATTERN = Pattern.compile("\\s")
    }
}
