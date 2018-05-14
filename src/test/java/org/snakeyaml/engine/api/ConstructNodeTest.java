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
package org.snakeyaml.engine.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.common.FlowStyle;
import org.snakeyaml.engine.common.ScalarStyle;
import org.snakeyaml.engine.exceptions.YamlEngineException;
import org.snakeyaml.engine.nodes.Node;
import org.snakeyaml.engine.nodes.ScalarNode;
import org.snakeyaml.engine.nodes.SequenceNode;
import org.snakeyaml.engine.nodes.Tag;

import com.google.common.collect.Lists;

@org.junit.jupiter.api.Tag("fast")
class ConstructNodeTest {

    @Test
    void failToConstructRecursive(TestInfo testInfo) {
        ConstructNode constructNode = new ConstructNode() {

            @Override
            public Object construct(Node node) {
                return null;
            }
        };
        Node node = new SequenceNode(Tag.SEQ, Lists.newArrayList(
                new ScalarNode(Tag.STR, "b", ScalarStyle.PLAIN)), FlowStyle.FLOW);
        node.setRecursive(true);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                constructNode.constructRecursive(node, new ArrayList<>()));
        assertEquals("Not implemented in org.snakeyaml.engine.api.ConstructNodeTest$1", exception.getMessage());
    }

    @Test
    void failToConstructNonRecursive(TestInfo testInfo) {
        ConstructNode constructNode = new ConstructNode() {

            @Override
            public Object construct(Node node) {
                return null;
            }
        };
        Node node = new SequenceNode(Tag.SEQ, Lists.newArrayList(
                new ScalarNode(Tag.STR, "b", ScalarStyle.PLAIN)), FlowStyle.FLOW);
        node.setRecursive(false);
        YamlEngineException exception = assertThrows(YamlEngineException.class, () ->
                constructNode.constructRecursive(node, new ArrayList<>()));
        assertTrue(exception.getMessage().startsWith("Unexpected recursive structure for Node"));
    }
}

