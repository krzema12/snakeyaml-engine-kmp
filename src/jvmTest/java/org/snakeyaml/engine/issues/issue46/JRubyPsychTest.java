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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.exceptions.ScannerException;

/**
 * https://github.com/jruby/jruby/issues/7698
 */
public class JRubyPsychTest {

  @Test
  @DisplayName("Issue 46: parse different values")
  void parseDifferentValues() {
    parse("\u2029", "\n \u2029");
    parse("\u2029", "\n\u2029");
    parse("\u2028", "\n \u2028");
    parse("\u2028", "\n\u2028");
    parse("\u2029 1", "\n\u2029 1");

    crash("while scanning an alias", "\n\u2029* "); // empty alias is not accepted
    crash("while scanning an alias", "\n\u2029*"); // empty alias is not accepted
    crash("while scanning an alias", "\n\u2029* 1"); // empty alias is not accepted
  }

  @Test
  @DisplayName("Issue 46: parse document where 2028 is used as leading space (3rd)")
  void parseValid() {
    LoadSettings loadSettings = LoadSettings.builder().build();
    Load load = new Load(loadSettings);
    Object obj = load.loadAllFromString("--- |2-\n\n\u2028  * C\n");
    assertNotNull(obj);
    Iterable iter = (Iterable) obj;
    try {
      iter.iterator().next();
    } catch (ScannerException e) {
      assertTrue(e.getMessage().contains(
          " the leading empty lines contain more spaces (2) than the first non-empty line."));
    }
  }

  @Test
  @DisplayName("Issue 46: parse document")
  void parseInvalid2() {
    LoadSettings loadSettings = LoadSettings.builder().build();
    Load load = new Load(loadSettings);
    Object obj = load.loadAllFromString("--- |2-\n\n  \u2028* C\n");
    assertNotNull(obj);
    Iterable iter = (Iterable) obj;
    Object doc = iter.iterator().next();
    assertEquals("\n\u2028* C", doc);
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
  @DisplayName("Issue 46: * is not alias after 2028")
  void failToParseInvalid() {
    LoadSettings loadSettings = LoadSettings.builder().build();
    Load load = new Load(loadSettings);
    Object obj = load.loadAllFromString("\n\u2028* C");
    Iterable iter = (Iterable) obj;
    for (Object o : iter) {
      System.out.println(o);
      assertEquals("\u2028* C", o);
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
      assertEquals("\u2028&C", o);
    }
  }
}
