/*
 * Copyright (c) 2018, SnakeYAML
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snakeyaml.engine.v2.representer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.ScalarNode;

@Tag("fast")
class EnumRepresenterTest {

  @Test
  @DisplayName("Represent Enum as node with global tag")
  void represenEnum() {
    DumpSettings settings = DumpSettings.builder().setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED)
        .build();
    StandardRepresenter standardRepresenter = new StandardRepresenter(settings);
    ScalarNode node = (ScalarNode) standardRepresenter.represent(FormatEnum.JSON);
    assertEquals(ScalarStyle.DOUBLE_QUOTED, node.getScalarStyle());
    assertEquals("tag:yaml.org,2002:org.snakeyaml.engine.v2.representer.FormatEnum",
        node.getTag().getValue());
  }

  @Test
  @DisplayName("Dump Enum with ScalarStyle.DOUBLE_QUOTED")
  void dumpEnum() {
    DumpSettings settings = DumpSettings.builder().setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED)
        .build();
    Dump dumper = new Dump(settings);
    String node = dumper.dumpToString(FormatEnum.JSON);
    assertEquals("!!org.snakeyaml.engine.v2.representer.FormatEnum \"JSON\"\n", node);
  }
}
