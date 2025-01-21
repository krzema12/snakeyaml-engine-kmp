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

import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.api.Dump;
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings;
import it.krzeminski.snakeyaml.engine.kmp.api.Load;
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings;
import it.krzeminski.snakeyaml.engine.kmp.schema.FailsafeSchema;

@org.junit.jupiter.api.Tag("fast")
public class FailsafeTest {

  Load loader = new Load(LoadSettings.builder().setSchema(new FailsafeSchema()).build());

  @Test
  void parseString() {
    assertEquals("true", loader.loadOne("true"));
    assertEquals("false", loader.loadOne("false"));
    assertEquals("null", loader.loadOne("null"));
    assertEquals("1", loader.loadOne("1"));
    assertEquals("0001", loader.loadOne("0001"));
    assertEquals("3.000", loader.loadOne("3.000"));
  }

  @Test
  void dumpString() {
    Dump dumper = new Dump(DumpSettings.builder().setSchema(new FailsafeSchema()).build());
    assertEquals("!!bool 'true'\n", dumper.dumpToString(Boolean.TRUE));
    assertEquals("!!bool 'false'\n", dumper.dumpToString(Boolean.FALSE));
    assertEquals("!!null 'null'\n", dumper.dumpToString(null));
    assertEquals("!!int '25'\n", dumper.dumpToString(25));
    assertEquals("!!int '17'\n", dumper.dumpToString(Integer.valueOf(17)));
    assertEquals("!!float '17.4'\n", dumper.dumpToString(Double.valueOf(17.4)));
  }
}
