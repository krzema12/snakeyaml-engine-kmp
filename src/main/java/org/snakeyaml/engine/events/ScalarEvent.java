/**
 * Copyright (c) 2018, http://www.snakeyaml.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snakeyaml.engine.events;

import static org.snakeyaml.engine.common.CharConstants.ESCAPES;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.snakeyaml.engine.common.ScalarStyle;
import org.snakeyaml.engine.exceptions.Mark;

/**
 * Marks a scalar value.
 */
public final class ScalarEvent extends NodeEvent {

    //this is only for Scalar representation (error messages and test suite)
    private static final Map<Character, Integer> ESCAPES_TO_PRINT = ESCAPES.entrySet().stream()
            .filter(entry -> entry.getKey() != '"').collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

    private final String tag;
    // style flag of a scalar event indicates the style of the scalar. Possible
    // values are None, '', '\'', '"', '|', '>'
    private final ScalarStyle style;
    private final String value;
    // The implicit flag of a scalar event is a pair of boolean values that
    // indicate if the tag may be omitted when the scalar is emitted in a plain
    // and non-plain style correspondingly.
    private final ImplicitTuple implicit;

    //TODO anchor is optional ?
    //TODO tag is optional ?
    public ScalarEvent(String anchor, String tag, ImplicitTuple implicit, String value, ScalarStyle style,
                       Optional<Mark> startMark, Optional<Mark> endMark) {
        super(anchor, startMark, endMark);
        this.tag = tag;
        this.implicit = implicit;
        if (value == null) throw new NullPointerException("Value must be provided.");
        this.value = value;
        if (style == null) throw new NullPointerException("Style must be provided.");
        this.style = style;
    }

    /**
     * Tag of this scalar.
     *
     * @return The tag of this scalar, or <code>null</code> if no explicit tag
     * is available.
     */
    public String getTag() {
        return this.tag;
    }

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
     * </dl>
     *
     * @return Style of the scalar.
     */
    public ScalarStyle getStyle() {
        return this.style;
    }

    /**
     * String representation of the value.
     * <p>
     * Without quotes and escaping.
     * </p>
     *
     * @return Value as Unicode string.
     */
    public String getValue() {
        return this.value;
    }

    public ImplicitTuple getImplicit() {
        return this.implicit;
    }

    @Override
    public boolean is(Event.ID id) {
        return ID.Scalar == id;
    }

    public boolean isPlain() {
        return style == ScalarStyle.PLAIN;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("=VAL");
        if (getAnchor() != null) {
            builder.append(" &");
            builder.append(getAnchor());
        }
        if (getTag() != null) {
            builder.append(" <");
            builder.append(getTag());
            builder.append(">");
        }
        builder.append(" ");
        if (getStyle() == ScalarStyle.PLAIN) {
            builder.append(":");
        } else {
            builder.append(getStyle().getChar());
        }
        //escape and drop surrogates
        String str = value.codePoints().filter(i -> i < Character.MAX_VALUE).mapToObj(c -> (char) c)
                .map(c -> escape(c)).collect(Collectors.joining(""));
        builder.append(str);
        return builder.toString();
    }

    /*
     * TODO why the hel converting int -> string is so complex ?
     * ch - the character to escape. Surrogates are not supported (because of int -> char conversion)
     */
    private String escape(Character ch) {
        if (ESCAPES_TO_PRINT.containsKey(ch)) {
            Integer i = ESCAPES_TO_PRINT.get(ch);
            Character c = Character.valueOf((char) i.intValue());
            return "\\" + c.toString();
        } else {
            return ch.toString();
        }
    }
}
