/*
 * Copyright (c) 2018, http://www.snakeyaml.org
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
package org.snakeyaml.engine.usecases.references;


import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.StreamDataWriter;
import org.snakeyaml.engine.v2.api.lowlevel.Compose;
import org.snakeyaml.engine.v2.common.Anchor;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.serializer.AnchorGenerator;
import org.snakeyaml.engine.v2.utils.TestUtils;

import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

@org.junit.jupiter.api.Tag("fast")
public class DumpAnchorTest {

    @Test
    public void test_anchor_test() {
        String str = TestUtils.getResource("anchor/issue481.yaml");
        Compose compose = new Compose(LoadSettings.builder().build());
        Node node = compose.composeReader(new StringReader(str)).get();


        DumpSettings setting = DumpSettings.builder()
                .setDefaultFlowStyle(FlowStyle.BLOCK)
                .setAnchorGenerator(new AnchorGenerator() {
                    @Override
                    public Anchor nextAnchor(Node node) {
                        return node.getAnchor().get();
                    }
                })
                .build();
        Dump yaml = new Dump(setting);

        StreamDataWriter writer = new MyDumperWriter();
        yaml.dumpNode(node, writer);
        assertEquals(str, writer.toString());
    }
}

class MyDumperWriter extends StringWriter implements StreamDataWriter {
}
