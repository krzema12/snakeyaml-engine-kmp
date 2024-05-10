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

import it.krzeminski.snakeyaml.engine.kmp.api.Load;
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@org.junit.jupiter.api.Tag("fast")
public class TabInFlowContextTest {

  @Test
  @DisplayName("Do not fail to parse if TAB is used (issue 11)")
  void parseTabInFlowContext() {
    LoadSettings settings = LoadSettings.builder().build();
    String input = "{\n\t\"x\": \"y\"\n}";
    Object obj = new Load(settings).loadFromString(input);
    assertEquals(Map.of("x", "y"), obj);
  }

  @Test
  @DisplayName("TAB can start a token.")
  public void testWrongTab() {
    LoadSettings settings = LoadSettings.builder().build();
    Object obj = new Load(settings).loadFromString("\t  data: 1");
    assertEquals(Map.of("data", 1), obj);
  }
}
