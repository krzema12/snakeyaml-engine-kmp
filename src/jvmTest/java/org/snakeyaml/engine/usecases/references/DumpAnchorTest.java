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


import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.api.Dump;
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings;
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings;
import it.krzeminski.snakeyaml.engine.kmp.api.StreamDataWriter;
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose;
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle;
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node;
import org.snakeyaml.engine.v2.utils.TestUtils;

import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

@org.junit.jupiter.api.Tag("fast")
public class DumpAnchorTest {

  @Test
  public void test_anchor_test() {
    String str = TestUtils.getResource("/anchor/issue481.yaml");
    Compose compose = new Compose(LoadSettings.builder().build());
    Node node = compose.compose(new StringReader(str));

    DumpSettings setting = DumpSettings.builder().setDefaultFlowStyle(FlowStyle.BLOCK)
        .setAnchorGenerator(node1 -> node1.getAnchor()).build();
    Dump yaml = new Dump(setting);

    StreamDataWriter writer = new MyDumperWriter();
    yaml.dumpNode(node, writer);
    assertEquals(str, writer.toString());
  }
}


class MyDumperWriter extends StringWriter implements StreamDataWriter {

}
