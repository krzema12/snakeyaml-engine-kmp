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
package org.snakeyaml.engine.common;

import org.snakeyaml.engine.exceptions.YAMLException;

/**
 * YAML provides a rich set of scalar styles. Block scalar styles include
 * the literal style and the folded style; flow scalar styles include the
 * plain style and two quoted styles, the single-quoted style and the
 * double-quoted style. These styles offer a range of trade-offs between
 * expressive power and readability.
 */
public enum ScalarStyle {
    DOUBLE_QUOTED('"'), SINGLE_QUOTED('\''), LITERAL('|'), FOLDED('>'), PLAIN(null);
    private Character styleChar; //TODO should styleChar be optional ?

    private ScalarStyle(Character style) {
        this.styleChar = style;
    }

    public Character getChar() {
        return styleChar;
    }

    @Override
    public String toString() {
        return "Scalar style: '" + styleChar + "'";
    }

    public static ScalarStyle createStyle(Character style) {
        if (style == null) {
            return PLAIN;
        } else {
            switch (style) {
                case '"':
                    return DOUBLE_QUOTED;
                case '\'':
                    return SINGLE_QUOTED;
                case '|':
                    return LITERAL;
                case '>':
                    return FOLDED;
                default:
                    throw new YAMLException("Unknown scalar style character: " + style);
            }
        }
    }
}