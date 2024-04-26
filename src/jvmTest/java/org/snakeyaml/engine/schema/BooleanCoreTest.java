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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.api.Dump;
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings;
import it.krzeminski.snakeyaml.engine.kmp.api.Load;
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings;
import it.krzeminski.snakeyaml.engine.kmp.schema.CoreSchema;

@org.junit.jupiter.api.Tag("fast")
public class BooleanCoreTest {

  Load loader = new Load(LoadSettings.builder().setSchema(new CoreSchema()).build());

  @Test
  void parseBoolean() {
    // true | True | TRUE | false | False | FALSE
    assertEquals(Boolean.TRUE, loader.loadFromString("true"));
    assertEquals(Boolean.TRUE, loader.loadFromString("True"));
    assertEquals(Boolean.TRUE, loader.loadFromString("TRUE"));
    assertEquals(Boolean.FALSE, loader.loadFromString("false"));
    assertEquals(Boolean.FALSE, loader.loadFromString("False"));
    assertEquals(Boolean.FALSE, loader.loadFromString("FALSE"));

    // the ! non-specific tag
    assertEquals("true", loader.loadFromString("! true"));
  }

  @Test
  @DisplayName("Dump special booleans in 1.1 but strings in 1.2")
  void parseString() {
    assertEquals("on", loader.loadFromString("on"));
    assertEquals("yes", loader.loadFromString("yes"));
  }

  @Test
  void dumpBoolean() {
    Dump dumper = new Dump(DumpSettings.builder().setSchema(new CoreSchema()).build());
    assertEquals("true\n", dumper.dumpToString(Boolean.TRUE));
    assertEquals("false\n", dumper.dumpToString(Boolean.FALSE));
  }
}
