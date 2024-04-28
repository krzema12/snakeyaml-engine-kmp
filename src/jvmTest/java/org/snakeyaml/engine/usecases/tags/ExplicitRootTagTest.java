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

import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.api.Dump;
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings;
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Example of serializing a root tag
 */
@org.junit.jupiter.api.Tag("fast")
public class ExplicitRootTagTest {

  @Test
  public void testLocalTag() {
    DumpSettings settings =
        DumpSettings.builder().setExplicitRootTag(new Tag("!my-data")).build();
    Map<String, String> map = new HashMap();
    map.put("foo", "bar");
    Dump dump = new Dump(settings);
    String output = dump.dumpToString(map);
    assertEquals("!my-data {foo: bar}\n", output);
  }
}
