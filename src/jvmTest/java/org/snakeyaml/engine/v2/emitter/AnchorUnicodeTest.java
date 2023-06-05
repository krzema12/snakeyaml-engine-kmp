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
package org.snakeyaml.engine.v2.emitter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.Anchor;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.serializer.AnchorGenerator;

@Tag("fast")
public class AnchorUnicodeTest {

  private static final Set<Character> INVALID_ANCHOR = new HashSet<>();

  static {
    INVALID_ANCHOR.add('[');
    INVALID_ANCHOR.add(']');
    INVALID_ANCHOR.add('{');
    INVALID_ANCHOR.add('}');
    INVALID_ANCHOR.add(',');
    INVALID_ANCHOR.add('*');
    INVALID_ANCHOR.add('&');
  }

  @Test
  public void testUnicodeAnchor() {
    DumpSettings settings = DumpSettings.builder().setAnchorGenerator(new AnchorGenerator() {
      int id = 0;

      @NotNull
      @Override
      public Anchor nextAnchor(@NotNull Node node) {
        return new Anchor("タスク" + id++);
      }
    }).build();
    Dump dump = new Dump(settings);
    List<String> list = new ArrayList<>();
    list.add("abc");

    List<List<String>> toExport = new ArrayList<>();
    toExport.add(list);
    toExport.add(list);

    String output = dump.dumpToString(toExport);
    assertEquals("- &タスク0 [abc]\n- *タスク0\n", output);
  }

  @Test
  public void testInvalidAnchor() {
    for (Character ch : INVALID_ANCHOR) {
      Dump dump = new Dump(createSettings(ch));
      List<String> list = new ArrayList<>();
      list.add("abc");

      List<List<String>> toExport = new ArrayList<>();
      toExport.add(list);
      toExport.add(list);
      try {
        dump.dumpToString(toExport);
        fail("expected dumpToString(toExport) would fail, but it succeeded");
      } catch (Exception e) {
        String message = "Invalid character '" + ch + "' in the anchor: anchor" + ch;
        assertEquals(message, e.getMessage());
      }
    }
  }


  private DumpSettings createSettings(final Character invalid) {
    return DumpSettings.builder().setAnchorGenerator(node -> new Anchor("anchor" + invalid)).build();
  }

  @Test
  public void testAnchors() {
    assertEquals("a", new Anchor("a").toString());
    assertEquals("Anchor may not contain spaces: a ", checkAnchor("a "));
    assertEquals("Anchor may not contain spaces: a \t", checkAnchor("a \t"));
    assertEquals("Invalid character '[' in the anchor: a[", checkAnchor("a["));
    assertEquals("Invalid character ']' in the anchor: a]", checkAnchor("a]"));
    assertEquals("Invalid character '{' in the anchor: {a", checkAnchor("{a"));
    assertEquals("Invalid character '}' in the anchor: }a", checkAnchor("}a"));
    assertEquals("Invalid character ',' in the anchor: a,b", checkAnchor("a,b"));
    assertEquals("Invalid character '*' in the anchor: a*b", checkAnchor("a*b"));
    assertEquals("Invalid character '&' in the anchor: a&b", checkAnchor("a&b"));
  }

  private String checkAnchor(String a) {
    try {
      new Anchor(a).toString();
      throw new IllegalStateException("Invalid must not be accepted: " + a);
    } catch (Exception e) {
      return e.getMessage();
    }
  }
}
