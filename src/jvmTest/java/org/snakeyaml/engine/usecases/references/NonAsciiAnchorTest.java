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
package org.snakeyaml.engine.usecases.references;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.api.Load;

@org.junit.jupiter.api.Tag("fast")
public class NonAsciiAnchorTest {

  private final String NON_ANCHORS = ",[]{}*&./";

  @Test
  @DisplayName("Non ASCII anchor name must be accepted")
  public void testNonAsciiAnchor() {
    Load load = new Load();
    String floatValue = (String) load.loadOne("&something_タスク タスク");
    assertEquals("タスク", floatValue);
  }

  @Test
  public void testUnderscore() {
    Load loader = new Load();
    Object value = loader.loadOne("&_ タスク");
    assertEquals("タスク", value);
  }

  @Test
  public void testSmile() {
    Load loader = new Load();
    Object value = loader.loadOne("&\uD83D\uDE01 v1");
    // System.out.println("&\uD83D\uDE01 v1");
    assertEquals("v1", value);
  }

  @Test
  public void testAlpha() {
    Load loader = new Load();
    Object value = loader.loadOne("&kääk v1");
    assertEquals("v1", value);
  }

  @Test
  @DisplayName("Reject invalid anchors which contain one of " + NON_ANCHORS)
  public void testNonAllowedAnchor() {
    for (int i = 0; i < NON_ANCHORS.length(); i++) {
      try {
        loadWith(NON_ANCHORS.charAt(i));
        fail("Special chars should not be allowed in anchor name");
      } catch (Exception e) {
        assertTrue(e.getMessage().contains("while scanning an anchor"), e.getMessage());
        assertTrue(e.getMessage().contains("unexpected character found"), e.getMessage());
      }
    }
  }

  private void loadWith(char c) {
    Load load = new Load();
    load.loadOne("&" + c + " value");
  }
}
