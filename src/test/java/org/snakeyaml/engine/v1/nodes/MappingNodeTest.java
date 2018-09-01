/**
 * Copyright (c) 2018, http://www.snakeyaml.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snakeyaml.engine.v1.nodes;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.v1.common.FlowStyle;
import org.snakeyaml.engine.v1.common.ScalarStyle;

@org.junit.jupiter.api.Tag("fast")
class MappingNodeTest {

    @Test
    void toString(TestInfo testInfo) {
        NodeTuple tuple1 = new NodeTuple(new ScalarNode(Tag.STR, "a", ScalarStyle.PLAIN), new ScalarNode(Tag.INT, "1", ScalarStyle.PLAIN));
        List list = new ArrayList();
        list.add(tuple1);
        Node mapping = new MappingNode(Tag.MAP, list, FlowStyle.FLOW);
        NodeTuple tuple2 = new NodeTuple(new ScalarNode(Tag.STR, "self", ScalarStyle.PLAIN), mapping);
        list.add(tuple2);
        String representation = mapping.toString();
        assertTrue(representation.startsWith("<org.snakeyaml.engine.v1.nodes.MappingNode (tag=tag:yaml.org,2002:map, values={ key=<org.snakeyaml.engine.v1.nodes.ScalarNode (tag=tag:yaml.org,2002:str, value=a)>; value=<NodeTuple keyNode=<org.snakeyaml.engine.v1.nodes.ScalarNode (tag=tag:yaml.org,2002:str, value=a)>; valueNode=<org.snakeyaml.engine.v1.nodes.ScalarNode (tag=tag:yaml.org,2002:int, value=1)>> }{ key=<org.snakeyaml.engine.v1.nodes.ScalarNode (tag=tag:yaml.org,2002:str, value=self)>;"));

    }
}
