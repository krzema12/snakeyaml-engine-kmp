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
package org.snakeyaml.engine.issues.issue25;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;

@org.junit.jupiter.api.Tag("fast")
public class DumpToStringTest {

  @Test
  @DisplayName("If Dump instance is called more then once then the results are not predictable.")
  void dumpToStringTwice() {
    ConcurrentHashMap<String, Object> data = new ConcurrentHashMap<>();
    DumpSettings dumpSettings = DumpSettings.builder().setDefaultFlowStyle(FlowStyle.BLOCK).build();
    Dump dump = new Dump(dumpSettings);
    class Something {

      final int doesntmatter = 0;
    }
    Something something = new Something();
    data.put("before", "bla");
    data.put("nested", something);
    try {
      dump.dumpToString(data);
      fail("Something must not be accepted without Representer");
    } catch (YamlEngineException e) {
      assertEquals(
          "Representer is not defined for class org.snakeyaml.engine.issues.issue25.DumpToStringTest$1Something",
          e.getMessage());
    }
    String output = dump.dumpToString(data);
    assertEquals("before: bla\n", output);
  }
}
