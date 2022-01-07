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
package org.snakeyaml.engine.v2.api.dump;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.DumpSettingsBuilder;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.ScalarStyle;

@Tag("fast")
class DumpWidthTest {

  private final DumpSettingsBuilder split = DumpSettings.builder().setSplitLines(true);
  private final DumpSettingsBuilder noSplit = DumpSettings.builder().setSplitLines(false);

  private final String data1 = "1111111111 2222222222 3333333333 4444444444 5555555555 6666666666 7777777777 8888888888 9999999999 0000000000";
  private final String data2 = "1111111111  2222222222  3333333333  4444444444  5555555555  6666666666  7777777777  8888888888  9999999999  0000000000";

  @Test
  void testSplitLinesDoubleQuoted() {
    Dump dump = new Dump(split.setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED).build());
    // Split lines enabled (default)
    String output = dump.dumpToString(data1);
    assertEquals(
        "\"1111111111 2222222222 3333333333 4444444444 5555555555 6666666666 7777777777 8888888888\\\n  \\ 9999999999 0000000000\"\n",
        output);

    // Lines with double spaces can be split too as whitespace can be preserved
    output = dump.dumpToString(data2);
    assertEquals(
        "\"1111111111  2222222222  3333333333  4444444444  5555555555  6666666666  7777777777\\\n  \\  8888888888  9999999999  0000000000\"\n",
        output);

    // Split lines disabled
    Dump dump2 = new Dump(noSplit.setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED).build());

    output = dump2.dumpToString(data1);
    assertEquals(
        "\"1111111111 2222222222 3333333333 4444444444 5555555555 6666666666 7777777777 8888888888 9999999999 0000000000\"\n",
        output);

    // setWidth
    Dump dump3 = new Dump(split.setWidth(15).build());
    output = dump3.dumpToString(data1);
    assertEquals("\"1111111111 2222222222\\\n" +
        "  \\ 3333333333 4444444444\\\n" +
        "  \\ 5555555555 6666666666\\\n" +
        "  \\ 7777777777 8888888888\\\n" +
        "  \\ 9999999999 0000000000\"\n", output);
  }

  @Test
  void testSplitLinesSingleQuoted() {
    Dump dump = new Dump(split.setDefaultScalarStyle(ScalarStyle.SINGLE_QUOTED).build());
    // Split lines enabled (default)
    String output = dump.dumpToString(data1);
    assertEquals(
        "'1111111111 2222222222 3333333333 4444444444 5555555555 6666666666 7777777777 8888888888\n  9999999999 0000000000'\n",
        output);

    // Do not split on double space as whitespace cannot be preserved in single quoted style
    output = dump.dumpToString(data2);
    assertEquals(
        "'1111111111  2222222222  3333333333  4444444444  5555555555  6666666666  7777777777  8888888888  9999999999  0000000000'\n",
        output);

    // Split lines disabled
    Dump dump2 = new Dump(noSplit.setDefaultScalarStyle(ScalarStyle.SINGLE_QUOTED).build());

    output = dump2.dumpToString(data1);
    assertEquals(
        "'1111111111 2222222222 3333333333 4444444444 5555555555 6666666666 7777777777 8888888888 9999999999 0000000000'\n",
        output);
  }

  @Test
  void testSplitLinesFolded() {
    Dump dump = new Dump(split.setDefaultScalarStyle(ScalarStyle.FOLDED).build());
    // Split lines enabled (default)
    String output = dump.dumpToString(data1);
    assertEquals(
        ">-\n  1111111111 2222222222 3333333333 4444444444 5555555555 6666666666 7777777777 8888888888\n  9999999999 0000000000\n",
        output);
    String str = (String) new Load(LoadSettings.builder().build()).loadFromString(
        ">-\n\n  1111111111 2222222222 3333333333 4444444444 5555555555 6666666666 7777777777 8888888888\n  9999999999 0000000000\n");
    assertEquals("\n" + data1, str, "No LF must be added");

    // Do not split on double space as whitespace cannot be preserved in folded style
    output = dump.dumpToString(data2);
    assertEquals(
        ">-\n  1111111111  2222222222  3333333333  4444444444  5555555555  6666666666  7777777777  8888888888  9999999999  0000000000\n",
        output);

    // Split lines disabled
    Dump dump2 = new Dump(noSplit.setDefaultScalarStyle(ScalarStyle.FOLDED).build());

    output = dump2.dumpToString(data1);
    assertEquals(
        ">-\n  1111111111 2222222222 3333333333 4444444444 5555555555 6666666666 7777777777 8888888888 9999999999 0000000000\n",
        output);
  }

  @Test
  void testSplitLinesLiteral() {
    Dump dump = new Dump(split.setDefaultScalarStyle(ScalarStyle.LITERAL).build());
    String output = dump.dumpToString(data1);
    // Split lines enabled (default) -- split lines does not apply to literal style
    assertEquals(
        "|-\n  1111111111 2222222222 3333333333 4444444444 5555555555 6666666666 7777777777 8888888888 9999999999 0000000000\n",
        output);
  }

  @Test
  void testSplitLinesPlain() {
    Dump dump = new Dump(split.setDefaultScalarStyle(ScalarStyle.PLAIN).build());
    // Split lines enabled (default)
    String output = dump.dumpToString(data1);
    assertEquals(
        "1111111111 2222222222 3333333333 4444444444 5555555555 6666666666 7777777777 8888888888\n  9999999999 0000000000\n",
        output);

    // Do not split on double space as whitespace cannot be preserved in plain style
    output = dump.dumpToString(data2);
    assertEquals(data2 + "\n", output);

    // Split lines disabled
    Dump dump2 = new Dump(noSplit.setDefaultScalarStyle(ScalarStyle.PLAIN).build());

    output = dump2.dumpToString(data1);
    assertEquals(data1 + "\n", output);
  }


}
