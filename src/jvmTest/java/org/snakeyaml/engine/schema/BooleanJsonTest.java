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

import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings;
import it.krzeminski.snakeyaml.engine.kmp.schema.JsonSchema;
import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.api.Dump;
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings;
import it.krzeminski.snakeyaml.engine.kmp.api.Load;

@org.junit.jupiter.api.Tag("fast")
public class BooleanJsonTest {

  private final Load loader = new Load(LoadSettings.builder().setSchema(new JsonSchema()).build());

  @Test
  void parseBoolean() {
    assertEquals(Boolean.TRUE, loader.loadOne("true"));
    assertEquals(Boolean.FALSE, loader.loadOne("false"));
    assertEquals("False", loader.loadOne("False"));
    assertEquals("True", loader.loadOne("True"));
    // the ! non-specific tag
    assertEquals("true", loader.loadOne("! true"));
  }

  @Test
  void dumpBoolean() {
    Dump dumper = new Dump(DumpSettings.builder().setSchema(new JsonSchema()).build());
    assertEquals("true\n", dumper.dumpToString(Boolean.TRUE));
    assertEquals("false\n", dumper.dumpToString(Boolean.FALSE));
  }
}
