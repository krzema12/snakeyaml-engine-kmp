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
package org.snakeyaml.engine.usecases.recursive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.api.Dump;
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings;
import it.krzeminski.snakeyaml.engine.kmp.api.Load;
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings;
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException;

@Tag("fast")
class RecursiveMapTest {

  @Test
  @DisplayName("Load map with recursive values")
  void loadRecursiveMap() {
    LoadSettings settings = LoadSettings.builder().build();
    Load load = new Load(settings);
    Map<String, String> map = (Map<String, String>) load
        .loadFromString("First occurrence: &anchor Foo\n" + "Second occurrence: *anchor\n"
            + "Override anchor: &anchor Bar\n" + "Reuse anchor: *anchor\n");
    Map<String, String> expected = ImmutableMap.of("First occurrence", "Foo", "Second occurrence",
        "Foo", "Override anchor", "Bar", "Reuse anchor", "Bar");
    assertEquals(expected, map);
  }

  @Test
  @DisplayName("Dump and Load map with recursive values")
  void loadRecursiveMap2() {
    Map<String, Object> map1 = new LinkedHashMap<>();
    map1.put("name", "first");
    Map<String, Object> map2 = new LinkedHashMap<>();
    map2.put("name", "second");
    map1.put("next", map2);
    map2.put("next", map1);
    Dump dump = new Dump(DumpSettings.builder().build());
    String output1 = dump.dumpToString(map1);
    assertEquals("&id001\n" + "name: first\n" + "next:\n" + "  name: second\n" + "  next: *id001\n",
        output1);

    LoadSettings settings = LoadSettings.builder().build();
    Load load = new Load(settings);
    Map<String, Object> parsed1 = (Map<String, Object>) load.loadFromString(output1);
    assertEquals(2, parsed1.size());
    assertEquals("first", parsed1.get("name"));
    Map<String, Object> parsed2 = (Map<String, Object>) parsed1.get("next");
    assertEquals("second", parsed2.get("name"));
  }

  @Test
  @DisplayName("Fail to load map with recursive keys")
  void failToLoadRecursiveMapByDefault() {
    LoadSettings settings = LoadSettings.builder().build();
    Load load = new Load(settings);
    // fail to load map which has only one key - reference to itself
    YamlEngineException exception = assertThrows(YamlEngineException.class,
        () -> load.loadFromString("&id002\n" + "*id002 : foo"));
    assertEquals("Recursive key for mapping is detected but it is not configured to be allowed.",
        exception.getMessage());
  }

  @Test
  @DisplayName("Load map with recursive keys if it is explicitly allowed")
  void loadRecursiveMapIfAllowed() {
    LoadSettings settings = LoadSettings.builder().setAllowRecursiveKeys(true).build();
    Load load = new Load(settings);
    // load map which has only one key - reference to itself
    Map<Object, Object> recursive =
        (Map<Object, Object>) load.loadFromString("&id002\n" + "*id002 : foo");
    assertEquals(1, recursive.size());
  }
}
