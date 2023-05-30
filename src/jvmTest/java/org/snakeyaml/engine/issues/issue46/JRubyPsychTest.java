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
package org.snakeyaml.engine.issues.issue46;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

/**
 * https://github.com/jruby/jruby/issues/7698
 */
public class JRubyPsychTest {

  @Test
  @DisplayName("Issue 46: parse different values")
  void parseDifferentValues() {
    parse(null, "\n \u2029");
    crash("while scanning an alias", "\n\u2029* "); // empty alias is not accepted
    crash("", "\n\u2029*"); // empty alias is not accepted
    crash("while scanning an alias", "\n\u2029* 1"); // empty alias is not accepted
    parse(null, "\n\u2029");
    parse(Integer.valueOf(1), "\n\u2029 1");
  }

  @Test
  @DisplayName("Issue 46: parse document")
  void parseValid() {
    LoadSettings loadSettings = LoadSettings.builder().build();
    Load load = new Load(loadSettings);
    Object obj = load.loadAllFromString("--- |2-\n\n\u2028  * C\n");
    assertNotNull(obj);
    Iterable iter = (Iterable) obj;
    Object doc = iter.iterator().next();
    assertEquals("\n\u2028 * C", doc);
  }

  // @Test
  @DisplayName("Issue 46: parse document")
  void parseInvalid2() {
    LoadSettings loadSettings = LoadSettings.builder().build();
    Load load = new Load(loadSettings);
    Object obj = load.loadAllFromString("--- |2-\n\n  \u2028* C\n");
    assertNotNull(obj);
    Iterable iter = (Iterable) obj;
    Object doc = iter.iterator().next();
    assertEquals("\n\u2028 * C", doc);
  }


  private void parse(Object expected, String data) {
    LoadSettings loadSettings = LoadSettings.builder().build();
    Load load = new Load(loadSettings);
    Object obj = load.loadFromString(data);
    assertEquals(expected, obj);
  }

  private void crash(String expectedError, String data) {
    LoadSettings loadSettings = LoadSettings.builder().build();
    Load load = new Load(loadSettings);
    try {
      load.loadFromString(data);
    } catch (Exception e) {
      assertTrue(e.getMessage().contains(expectedError), e.getMessage());
    }
  }

  @Test
  @DisplayName("Issue 46: fail to parse invalid")
  void failToParseInvalid() {
    LoadSettings loadSettings = LoadSettings.builder().build();
    Load load = new Load(loadSettings);
    Object obj = load.loadAllFromString("\n\u2028* C");
    Iterable iter = (Iterable) obj;
    try {
      for (Object o : iter) {
        System.out.println(o);
      }
      fail("Alias before anchor should not be accepted");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("unexpected character found  (32)"), e.getMessage());
    }
  }

  @Test
  @DisplayName("Issue 46: use anchor instead of alias")
  void parse2028_1() {
    LoadSettings loadSettings = LoadSettings.builder().build();
    Load load = new Load(loadSettings);
    Object obj = load.loadAllFromString("\n\u2028&C");
    Iterable iter = (Iterable) obj;
    for (Object o : iter) {
      assertNull(o);
    }
  }
}
