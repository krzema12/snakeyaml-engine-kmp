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
import org.snakeyaml.engine.v2.common.CharConstants
import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.exceptions.Mark
import java.util.Objects
import java.util.Optional
import java.util.stream.Collectors

/**
 * Marks a scalar value.
 */
class ScalarEvent @JvmOverloads constructor(
    anchor: Optional<Anchor>,
    tag: Optional<String>,
    implicit: ImplicitTuple,
    value: String,
    style: ScalarStyle,
    startMark: Optional<Mark> = Optional.empty(),
    endMark: Optional<Mark> = Optional.empty(),
) :
    NodeEvent(anchor, startMark, endMark) {
    /**
     * Tag of this scalar.
     *
     * @return The tag of this scalar, or `null` if no explicit tag is available.
     */
    val tag: Optional<String>

    /**
     * Style of the scalar.
     * <dl>
     * <dt>null</dt>
     * <dd>Flow Style - Plain</dd>
     * <dt>'\''</dt>
     * <dd>Flow Style - Single-Quoted</dd>
     * <dt>'"'</dt>
     * <dd>Flow Style - Double-Quoted</dd>
     * <dt>'|'</dt>
     * <dd>Block Style - Literal</dd>
     * <dt>'&gt;'</dt>
     * <dd>Block Style - Folded</dd>
    </dl> *
     *
     * @return Style of the scalar.
     */
    // style flag of a scalar event indicates the style of the scalar. Possible
    // values are None, '', '\'', '"', '|', '>'
    val scalarStyle: ScalarStyle

    /**
     * String representation of the value.
     *
     *
     * Without quotes and escaping.
     *
     *
     * @return Value as Unicode string.
     */
    val value: String

    // The implicit flag of a scalar event is a pair of boolean values that
    // indicate if the tag may be omitted when the scalar is emitted in a plain
    // and non-plain style correspondingly.
    val implicit: ImplicitTuple

    init {
        Objects.requireNonNull(tag)
        this.tag = tag
        this.implicit = implicit
        Objects.requireNonNull(value)
        this.value = value
        Objects.requireNonNull(style)
        scalarStyle = style
    }

    override val eventId: ID
        get() = ID.Scalar

    val isPlain: Boolean
        get() = scalarStyle == ScalarStyle.PLAIN

    override fun toString(): String {
        val builder = StringBuilder("=VAL")
        anchor.ifPresent { a -> builder.append(" &$a") }
        if (implicit.bothFalse()) {
            tag.ifPresent { theTag: String -> builder.append(" <$theTag>") }
        }
        builder.append(" ")
        builder.append(scalarStyle.toString())
        builder.append(escapedValue())
        return builder.toString()
    }

    // escape
    fun escapedValue(): String {
        return value.codePoints().filter { i: Int -> i < Character.MAX_VALUE.code }
            .mapToObj { ch: Int ->
                CharConstants.escapeChar(
                    String(
                        Character.toChars(
                            ch,
                        ),
                    ),
                )
            }
            .collect(Collectors.joining(""))
    }
}
