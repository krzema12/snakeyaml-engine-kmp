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
package org.snakeyaml.engine.v2.nodes;

import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle;
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle;
import it.krzeminski.snakeyaml.engine.kmp.nodes.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static kotlin.text.StringsKt.substringBeforeLast;
import static org.junit.jupiter.api.Assertions.assertEquals;

@org.junit.jupiter.api.Tag("fast")
class MappingNodeTest {

  @Test
  void testToString() {
    NodeTuple tuple1 = new NodeTuple(
        new ScalarNode(Tag.STR, "a", ScalarStyle.PLAIN),
        new ScalarNode(Tag.INT, "1", ScalarStyle.PLAIN)
    );
    List<NodeTuple> list = new ArrayList<>();
    list.add(tuple1);
    Node mapping = new MappingNode(Tag.MAP, list, FlowStyle.FLOW);
    NodeTuple tuple2 = new NodeTuple(new ScalarNode(Tag.STR, "self", ScalarStyle.PLAIN), mapping);
    list.add(tuple2);
    String representation = mapping.toString();
    assertEquals(
        "<MappingNode (tag=tag:yaml.org,2002:map, values={ key=<ScalarNode (tag=tag:yaml.org,2002:str, value=a)>; value=<NodeTuple keyNode=<ScalarNode (tag=tag:yaml.org,2002:str, value=a)>; valueNode=<ScalarNode (tag=tag:yaml.org,2002:int, value=1)>> }{ key=<ScalarNode (tag=tag:yaml.org,2002:str, value=self)>;",
        substringBeforeLast(
            representation,
            " value=",
            representation
        )
    );
  }
}
