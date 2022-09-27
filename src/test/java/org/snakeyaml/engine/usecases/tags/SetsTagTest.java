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
package org.snakeyaml.engine.usecases.tags;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

/**
 * Example of parsing a local tag
 */
@org.junit.jupiter.api.Tag("fast")
public class SetsTagTest {

  @Test
  @DisplayName("Test that !!set tag creates a Set")
  public void testSetsTag() {
    LoadSettings settings = LoadSettings.builder().build();
    Load loader = new Load(settings);
    final String YAML = "---\n" + "sets: !!set\n" + "    ? a\n" + "    ? b\n";
    Map<String, Set<String>> map = (Map<String, Set<String>>) loader.loadFromString(YAML);
    Set<String> set = map.get("sets");
    assertEquals(2, set.size());
    Iterator iter = set.iterator();
    assertEquals("a", iter.next());
    assertEquals("b", iter.next());
  }
}
