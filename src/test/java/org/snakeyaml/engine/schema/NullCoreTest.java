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
package org.snakeyaml.engine.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.schema.CoreSchema;

@org.junit.jupiter.api.Tag("fast")
public class NullCoreTest {

  Load loader = new Load(LoadSettings.builder().setSchema(new CoreSchema()).build());

  @Test
  void parseNull() {
    // null | Null | NULL | ~
    assertNull(loader.loadFromString("null"));
    assertNull(loader.loadFromString("Null"));
    assertNull(loader.loadFromString("NULL"));
    assertNull(loader.loadFromString("~"));
    assertEquals("null", loader.loadFromString("! null"));
  }

  @Test
  void dumpNull() {
    Dump dumper = new Dump(DumpSettings.builder().setSchema(new CoreSchema()).build());
    assertEquals("null\n", dumper.dumpToString(null));
  }
}
