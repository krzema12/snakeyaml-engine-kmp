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

import java.util.LinkedHashMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

@Tag("fast")
public class ReferencesTest {

  /**
   * Create data which is difficult to parse.
   *
   * @param size - size of the map, defines the complexity
   * @return YAML to parse
   */
  private String createDump(int size) {
    LinkedHashMap root = new LinkedHashMap();
    LinkedHashMap s1, s2, t1, t2;
    s1 = root;
    s2 = new LinkedHashMap();
    /*
     * the time to parse grows very quickly SIZE -> time to parse in seconds 25 -> 1 26 -> 2 27 -> 3
     * 28 -> 8 29 -> 13 30 -> 28 31 -> 52 32 -> 113 33 -> 245 34 -> 500
     */
    for (int i = 0; i < size; i++) {

      t1 = new LinkedHashMap();
      t2 = new LinkedHashMap();
      t1.put("foo", "1");
      t2.put("bar", "2");

      s1.put("a", t1);
      s1.put("b", t2);
      s2.put("a", t1);
      s2.put("b", t2);

      s1 = t1;
      s2 = t2;
    }

    // this is VERY BAD code
    // the map has itself as a key (no idea why it may be used except of a DoS attack)
    LinkedHashMap f = new LinkedHashMap();
    f.put(f, "a");
    f.put("g", root);

    Dump dump = new Dump(DumpSettings.builder().build());
    String output = dump.dumpToString(f);
    // TODO no replace should be needed
    return output.replace("001: ", "001 : ");
  }

  @Test
  public void referencesWithRecursiveKeysNotAllowedByDefault() {
    String output = createDump(30);
    // System.out.println(output);
    long time1 = System.currentTimeMillis();
    // Load
    LoadSettings settings = LoadSettings.builder().setMaxAliasesForCollections(150).build();
    Load load = new Load(settings);
    try {
      load.loadFromString(output);
      fail();
    } catch (Exception e) {
      assertEquals("Recursive key for mapping is detected but it is not configured to be allowed.",
          e.getMessage());
    }
    long time2 = System.currentTimeMillis();
    float duration = (time2 - time1) / 1000;
    assertTrue(duration < 1.0, "It should fail quickly. Time was " + duration + " seconds.");
  }

  @Test
  @DisplayName("Parsing with aliases may take a lot of time, CPU and memory")
  public void parseManyAliasesForCollections() {
    String output = createDump(25);
    // Load
    long time1 = System.currentTimeMillis();
    LoadSettings settings =
        LoadSettings.builder().setAllowRecursiveKeys(true).setMaxAliasesForCollections(50).build();
    Load load = new Load(settings);
    load.loadFromString(output);
    long time2 = System.currentTimeMillis();
    double duration = (time2 - time1) / 1000.0;
    int cores = Runtime.getRuntime().availableProcessors();
    double minDuration = 0.7;
    if (cores > 4) {
      minDuration = 0.4;
    }
    assertTrue(duration > minDuration, "It should take time. Time was " + duration + " seconds.");
    assertTrue(duration < 9.0, "Time was " + duration + " seconds.");
  }

  @Test
  @DisplayName("Prevent DoS attack by failing early")
  public void referencesWithRestrictedAliases() {
    // without alias restriction this size should occupy tons of CPU, memory and time to parse
    String bigYAML = createDump(35);
    // Load
    long time1 = System.currentTimeMillis();
    LoadSettings settings =
        LoadSettings.builder().setAllowRecursiveKeys(true).setMaxAliasesForCollections(40).build();
    Load load = new Load(settings);
    try {
      load.loadFromString(bigYAML);
      fail();
    } catch (Exception e) {
      assertEquals("Number of aliases for non-scalar nodes exceeds the specified max=40",
          e.getMessage());
    }
    long time2 = System.currentTimeMillis();
    float duration = (time2 - time1) / 1000;
    assertTrue(duration < 1.0, "It should fail quickly. Time was " + duration + " seconds.");
  }
}
