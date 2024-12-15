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
package org.snakeyaml.engine.v2.exceptions;

import it.krzeminski.snakeyaml.engine.kmp.exceptions.Mark;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@Tag("fast")
class MarkTest {

  @Test
  @DisplayName("Mark snippet")
  void testGet_snippet() {
    Mark mark = createMark(0, 0, 0, "*The first line.\nThe last line.", 0);
    assertEquals("    *The first line.\n    ^", mark.createSnippet());
    mark = createMark(0, 0, 0, "The first*line.\nThe last line.", 9);
    assertEquals("    The first*line.\n             ^", mark.createSnippet());
  }

  @Test
  @DisplayName("Mark toString()")
  void testToString() {
    Mark mark = createMark(0, 0, 0, "*The first line.\nThe last line.", 0);
    String[] lines = mark.toString().split("\n");
    assertEquals(" in test1, line 1, column 1:", lines[0]);
    assertEquals("*The first line.", lines[1].trim());
    assertEquals("^", lines[2].trim());
  }

  @Test
  @DisplayName("Mark position")
  void testPosition() {
    Mark mark = createMark(17, 29, 213, "*The first line.\nThe last line.", 0);
    assertEquals(17, mark.getIndex(), "index is used in JRuby");
    assertEquals(29, mark.getLine());
    assertEquals(213, mark.getColumn());
  }

  @Test
  @DisplayName("Mark buffer")
  void testGetBuffer() {
    Mark mark = createMark(0, 29, 213, "*The first line.\nThe last line.", 0);
    List<Integer> buffer = Arrays.asList(42, 84, 104, 101, 32, 102, 105, 114, 115, 116, 32, 108, 105, 110, 101,
      46, 10, 84, 104, 101, 32, 108, 97, 115, 116, 32, 108, 105, 110, 101, 46);
    assertEquals(buffer.size(), mark.getCodepoints().size());
    assertIterableEquals(buffer, mark.getCodepoints());
  }

  @Test
  @DisplayName("Mark pointer")
  void testGetPointer() {
    Mark mark = createMark(0, 29, 213, "*The first line.\nThe last line.", 5);
    assertEquals(5, mark.getPointer());
    assertEquals("test1", mark.getName());
  }

  @Test
  @DisplayName("Mark: createSnippet(): longer content must be reduced")
  void testGetReducedSnippet() {
    Mark mark = createMark(
      200,
      2,
      36,
      "*The first line,\nThe second line.\nThe third line, which aaaa bbbb ccccc dddddd * contains mor12345678901234\nThe last line.",
      78);
    assertEquals(
      "   ... aaaa bbbb ccccc dddddd * contains mor1234567 ... \n                             ^",
      mark.createSnippet(2, 55));
  }

  /*
   * "createSnippet(): longer content must be reduced" { val doc = """The first line. |The second
   * line, |The third line, which aaaa bbbb ccccc dddddd * contains moreeeeeeeeeeeeee |The last
   * line.""".trimMargin().toCharArray() Mark("test1", 2, 35, doc, 78).createSnippet(2, 55) shouldBe
   * """ ... aaa bbbb ccccc dddddd * contains moreeeeeeee ... | ^""".trimMargin() }
   */

  private static Mark createMark(
    int index,
    int line,
    int column,
    String str,
    int pointer
  ) {
    List<Integer> codepoints = str.codePoints().boxed().collect(Collectors.toList());
    return new Mark("test1", index, line, column, codepoints, pointer);
  }
}
