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
package org.snakeyaml.engine.issues.issue11;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

@org.junit.jupiter.api.Tag("fast")
public class TabInFlowContextTest {

  @Test
  @DisplayName("Do not fail to parse if TAB is used (issue 11)")
  void parseTabInFlowContext() {
    LoadSettings settings = LoadSettings.builder().build();
    try {
      String input = "{\n\t\"x\": \"y\"\n}";
      Object obj = new Load(settings).loadFromString(input);
      fail("TAB cannot start a token. Found: " + obj);
    } catch (Exception e) {
      assertEquals("while scanning for the next token\n"
          + "found character '\\t(TAB)' that cannot start any token. (Do not use \\t(TAB) for indentation)\n"
          + " in reader, line 2, column 1:\n" + "    \t\"x\": \"y\"\n" + "    ^\n", e.getMessage());
    }
  }

  @Test
  @DisplayName("TAB cannot start a token.")
  public void testWrongTab() {
    LoadSettings settings = LoadSettings.builder().build();
    try {
      Object obj = new Load(settings).loadFromString("\t  data: 1");
      fail("TAB cannot start a token. Found: " + obj);
    } catch (Exception e) {
      assertEquals("while scanning for the next token\n"
          + "found character '\\t(TAB)' that cannot start any token. (Do not use \\t(TAB) for indentation)\n"
          + " in reader, line 1, column 1:\n" + "    \t  data: 1\n" + "    ^\n", e.getMessage());
    }
  }
}
