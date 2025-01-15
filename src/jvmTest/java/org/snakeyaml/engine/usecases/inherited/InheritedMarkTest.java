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
package org.snakeyaml.engine.usecases.inherited;

import it.krzeminski.snakeyaml.engine.kmp.exceptions.Mark;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static it.krzeminski.snakeyaml.engine.kmp.usecases.inherited.InheritedImportUtilsKt.getResource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@org.junit.jupiter.api.Tag("fast")
public class InheritedMarkTest {

  @Test
  @DisplayName("Marks")
  public void testMarks() {
    String content = getResource("test_mark.marks");
    String[] inputs = content.split("---\n");
    for (int i = 1; i < inputs.length; i++) {
      String input = inputs[i];
      int index = 0;
      int line = 0;
      int column = 0;
      while (input.charAt(index) != '*') {
        if (input.charAt(index) != '\n') {
          line += 1;
          column = 0;
        } else {
          column += 1;
        }
        index += 1;
      }
      List<Integer> inputCodepoints = input.codePoints().boxed().collect(Collectors.toList());
      Mark mark = new Mark("testMarks", index, line, column, inputCodepoints, index);
      String snippet = mark.createSnippet(2, 79);
      assertTrue(snippet.contains("\n"), "Must contain at least one '\n'.");
      assertEquals(snippet.indexOf("\n"), snippet.lastIndexOf("\n"), "Must only have only one '\n'.");
      String[] lines = snippet.split("\n");
      String data = lines[0];
      String pointer = lines[1];
      assertTrue(data.length() < 82, "Mark must be restricted: " + data);
      int dataPosition = data.indexOf("*");
      int pointerPosition = pointer.indexOf("^");
      assertEquals(dataPosition, pointerPosition, "Pointer should coincide with '*':\n " + snippet);
    }
  }
}
