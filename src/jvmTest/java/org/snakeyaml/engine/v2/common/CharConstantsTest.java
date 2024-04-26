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
package org.snakeyaml.engine.v2.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static it.krzeminski.snakeyaml.engine.kmp.common.CharConstants.ESCAPE_REPLACEMENTS;

import it.krzeminski.snakeyaml.engine.kmp.common.CharConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("fast")
class CharConstantsTest {

  @Test
  @DisplayName("LINEBR contains only LF and CR: http://www.yaml.org/spec/1.2/spec.html#id2774608")
  void lineBreaks() {
    assertTrue(CharConstants.LINEBR.has('\n'), "LF must be included");
    assertFalse(CharConstants.LINEBR.has('\r'), "CR must be excluded");
    assertTrue(CharConstants.LINEBR.hasNo('\u0085'), "85 (next line) must not be included in 1.2");
    assertTrue(CharConstants.LINEBR.hasNo('\u2028'),
        "2028 (line separator) must not be included in 1.2");
    assertTrue(CharConstants.LINEBR.hasNo('\u2029'),
        "2029 (paragraph separator) must not be included in 1.2");
    assertTrue(CharConstants.LINEBR.hasNo('a'), "normal char should not be included");
  }

  @Test
  @DisplayName("NULL_OR_LINEBR contains 3 chars")
  void lineBreaksAndNulls() {
    assertTrue(CharConstants.NULL_OR_LINEBR.has('\n'));
    assertTrue(CharConstants.NULL_OR_LINEBR.has('\r'));
    assertTrue(CharConstants.NULL_OR_LINEBR.has('\u0000'));
    assertFalse(CharConstants.NULL_OR_LINEBR.has('\u0085'),
        "85 (next line) must not be included in 1.2");
    assertFalse(CharConstants.NULL_OR_LINEBR.has('\u2028'),
        "2028 (line separator) must not be included in 1.2");
    assertFalse(CharConstants.NULL_OR_LINEBR.has('\u2029'),
        "2029 (paragraph separator) must not be included in 1.2");
    assertFalse(CharConstants.NULL_OR_LINEBR.has('b'), "normal char should not be included");
  }

  @Test
  @DisplayName("additional chars")
  void lineBreaksAndNullsAndSpace() {
    assertTrue(CharConstants.NULL_BL_LINEBR.hasNo('1'));
    assertTrue(CharConstants.NULL_BL_LINEBR.has('1', "123"));
    assertTrue(CharConstants.NULL_BL_LINEBR.hasNo('4', "123"));
  }


  @Test
  @DisplayName("ESCAPE_REPLACEMENTS")
  void ESCAPE_REPLACEMENTS() {
    assertEquals(Integer.valueOf(97), Integer.valueOf('a'));
    assertEquals(15, ESCAPE_REPLACEMENTS.size());
    assertEquals("\r", ESCAPE_REPLACEMENTS.get('r'));
  }

  @Test
  void escapeChar() {
    assertEquals(" ", CharConstants.escapeChar(' '));
    assertEquals("/", CharConstants.escapeChar('/'));
    assertEquals("\\t", CharConstants.escapeChar('\t'));
  }
}
