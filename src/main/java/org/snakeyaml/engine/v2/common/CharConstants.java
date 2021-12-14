/*
 * Copyright (c) 2018, SnakeYAML
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snakeyaml.engine.v2.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class CharConstants {
    private static final String ALPHA_S = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-_";

    private static final String LINEBR_S = "\n";
    private final static String FULL_LINEBR_S = "\r" + LINEBR_S;
    private static final String NULL_OR_LINEBR_S = "\0" + FULL_LINEBR_S;
    private static final String NULL_BL_LINEBR_S = " " + NULL_OR_LINEBR_S;
    private static final String NULL_BL_T_LINEBR_S = "\t" + NULL_BL_LINEBR_S;
    private static final String NULL_BL_T_S = "\0 \t";
    private static final String URI_CHARS_S = ALPHA_S + "-;/?:@&=+$,_.!~*'()[]%";

    public static final CharConstants LINEBR = new CharConstants(LINEBR_S);
    public static final CharConstants NULL_OR_LINEBR = new CharConstants(NULL_OR_LINEBR_S);
    public static final CharConstants NULL_BL_LINEBR = new CharConstants(NULL_BL_LINEBR_S);
    public static final CharConstants NULL_BL_T_LINEBR = new CharConstants(NULL_BL_T_LINEBR_S);
    public static final CharConstants NULL_BL_T = new CharConstants(NULL_BL_T_S);
    public static final CharConstants URI_CHARS = new CharConstants(URI_CHARS_S);

    public static final CharConstants ALPHA = new CharConstants(ALPHA_S);

    private static final int ASCII_SIZE = 128;
    boolean[] contains = new boolean[ASCII_SIZE];

    private CharConstants(String content) {
        Arrays.fill(contains, false);
        for (int i = 0; i < content.length(); i++) {
            int c = content.codePointAt(i);
            contains[c] = true;
        }
    }

    public boolean has(int c) {
        return (c < ASCII_SIZE) && contains[c];
    }

    public boolean hasNo(int c) {
        return !has(c);
    }

    public boolean has(int c, String additional) {
        return has(c) || additional.indexOf(c) != -1;
    }

    public boolean hasNo(int c, String additional) {
        return !has(c, additional);
    }

    /**
     * A mapping from an escaped character in the input stream to the character
     * that they should be replaced with.
     * <p>
     * YAML defines several common and a few uncommon escape sequences.
     */
    public static final Map<Integer, Character> ESCAPE_REPLACEMENTS;

    /**
     * A mapping from a character to be escaped to its code in the output stream. (used for emitting)
     * It contains the same as ESCAPE_REPLACEMENTS except ' ' and '/'
     * <p>
     * YAML defines several common and a few uncommon escape sequences.
     *
     * @see <a href="http://www.yaml.org/spec/1.2/spec.html#id2776092">5.7. Escaped Characters</a>
     */
    public static final Map<Character, Integer> ESCAPES;

    /**
     * A mapping from a character to a number of bytes to read-ahead for that
     * escape sequence. These escape sequences are used to handle unicode
     * escaping in the following formats, where H is a hexadecimal character:
     * <pre>
     * &#92;xHH         : escaped 8-bit Unicode character
     * &#92;uHHHH       : escaped 16-bit Unicode character
     * &#92;UHHHHHHHH   : escaped 32-bit Unicode character
     * </pre>
     */
    public static final Map<Character, Integer> ESCAPE_CODES;

    static {
        Map<Integer, Character> escapeReplacements = new HashMap();
        Map<Character, Integer> escapes = new HashMap();
        escapeReplacements.put(Integer.valueOf('0'), '\0');// ASCII null
        escapeReplacements.put(Integer.valueOf('a'), '\u0007');// ASCII bell
        escapeReplacements.put(Integer.valueOf('b'), '\u0008'); // ASCII backspace
        escapeReplacements.put(Integer.valueOf('t'), '\u0009'); // ASCII horizontal tab
        escapeReplacements.put(Integer.valueOf('n'), '\n');// ASCII newline (line feed; &#92;n maps to 0x0A)
        escapeReplacements.put(Integer.valueOf('v'), '\u000B');// ASCII vertical tab
        escapeReplacements.put(Integer.valueOf('f'), '\u000C');// ASCII form-feed
        escapeReplacements.put(Integer.valueOf('r'), '\r');// carriage-return (&#92;r maps to 0x0D)
        escapeReplacements.put(Integer.valueOf('e'), '\u001B');// ASCII escape character (Esc)
        escapeReplacements.put(Integer.valueOf(' '), '\u0020');// ASCII space
        escapeReplacements.put(Integer.valueOf('"'), '\"');// ASCII double-quote
        escapeReplacements.put(Integer.valueOf('/'), '/');// ASCII slash (#x2F), for JSON compatibility.
        escapeReplacements.put(Integer.valueOf('\\'), '\\');// ASCII backslash
        escapeReplacements.put(Integer.valueOf('N'), '\u0085');// Unicode next line
        escapeReplacements.put(Integer.valueOf('_'), '\u00A0');// Unicode non-breaking-space
        escapeReplacements.put(Integer.valueOf('L'), '\u2028');// Unicode line-separator
        escapeReplacements.put(Integer.valueOf('P'), '\u2029');// Unicode paragraph separator

        escapeReplacements.entrySet().stream()
                .filter(entry -> entry.getKey() != ' ' && entry.getKey() != '/')
                .forEach(entry -> escapes.put(entry.getValue(), entry.getKey()));
        ESCAPE_REPLACEMENTS = Collections.unmodifiableMap(escapeReplacements);
        ESCAPES = Collections.unmodifiableMap(escapes);

        Map<Character, Integer> escapeCodes = new HashMap();
        escapeCodes.put(Character.valueOf('x'), 2);// 8-bit Unicode
        escapeCodes.put(Character.valueOf('u'), 4);// 16-bit Unicode
        escapeCodes.put(Character.valueOf('U'), 8);// 32-bit Unicode (Supplementary characters are supported)
        ESCAPE_CODES = Collections.unmodifiableMap(escapeCodes);
    }
}
