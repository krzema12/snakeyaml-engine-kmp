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
package org.snakeyaml.engine.issues.issue3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;

@org.junit.jupiter.api.Tag("fast")
class ExceptionTest {

  @Test
  void sequenceException() {
    Load load = new Load(LoadSettings.builder().build());
    YamlEngineException exception =
        assertThrows(YamlEngineException.class, () -> load.loadFromString("!!seq abc"));
    System.err.println(exception.getMessage());
    assertTrue(exception.getMessage().contains("java.lang.ClassCastException"));
    assertTrue(exception.getMessage().contains("org.snakeyaml.engine.v2.nodes.ScalarNode"));
    assertTrue(exception.getMessage().contains("cannot be cast to"));
    assertTrue(exception.getMessage().contains("org.snakeyaml.engine.v2.nodes.SequenceNode"));
  }

  @Test
  void intException() {
    Load load = new Load(LoadSettings.builder().build());
    YamlEngineException exception =
        assertThrows(YamlEngineException.class, () -> load.loadFromString("!!int abc"));
    assertEquals("java.lang.NumberFormatException: For input string: \"abc\"",
        exception.getMessage());
  }
}
