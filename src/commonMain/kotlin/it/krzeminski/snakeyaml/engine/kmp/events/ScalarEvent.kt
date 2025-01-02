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

import it.krzeminski.snakeyaml.engine.kmp.internal.utils.toCodePoints
import it.krzeminski.snakeyaml.engine.kmp.common.Anchor
import it.krzeminski.snakeyaml.engine.kmp.common.CharConstants
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.exceptions.Mark
import kotlin.jvm.JvmOverloads

/**
 * Marks a scalar value.
 */
class ScalarEvent @JvmOverloads constructor(
    anchor: Anchor?,

    /**
     * Tag of this scalar.
     *
     * @returns The tag of this scalar, or `null` if no explicit tag is available.
     */
    val tag: String?,
    // The implicit flag of a scalar event is a pair of boolean values that
    // indicate if the tag may be omitted when the scalar is emitted in a plain
    // and non-plain style correspondingly.
    val implicit: ImplicitTuple,
    /**
     * String representation of the value, without quotes and escaping.
     *
     * @return Value as Unicode string.
     */
    val value: String,
    /**
     * Indicates the style of the scalar
     *
     * @return Style of the scalar.
     */
    val scalarStyle: ScalarStyle,
    startMark: Mark? = null,
    endMark: Mark? = null,
) : NodeEvent(anchor, startMark, endMark) {

    override val eventId: ID
        get() = ID.Scalar

    val plain: Boolean
        get() = scalarStyle == ScalarStyle.PLAIN

    val literal: Boolean
        get() = scalarStyle == ScalarStyle.LITERAL

    val sQuoted: Boolean
        get() = scalarStyle == ScalarStyle.SINGLE_QUOTED

    val dQuoted: Boolean
        get() = scalarStyle == ScalarStyle.DOUBLE_QUOTED

    val folded: Boolean
        get() = scalarStyle == ScalarStyle.FOLDED

    val json: Boolean
        get() = scalarStyle == ScalarStyle.JSON_SCALAR_STYLE

    override fun toString(): String {
        return buildString {
            append("=VAL")
            if (anchor != null) append(" &$anchor")
            if (implicit.bothFalse()) {
                if (tag != null) append(" <$tag>")
            }
            append(" ")
            append(scalarStyle.toString())
            append(escapedValue())
        }
    }

    fun escapedValue(): String {
        return value
            .toCodePoints()
            .filter { it < Char.MAX_VALUE.code }
            .joinToString("") { ch: Int ->
                CharConstants.escapeChar(ch.toChar())
            }
    }
}
