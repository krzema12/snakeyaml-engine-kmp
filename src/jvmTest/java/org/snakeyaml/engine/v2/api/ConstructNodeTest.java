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
package org.snakeyaml.engine.v2.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Lists;
import java.util.ArrayList;

import it.krzeminski.snakeyaml.engine.kmp.api.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle;
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle;
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException;
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node;
import it.krzeminski.snakeyaml.engine.kmp.nodes.ScalarNode;
import it.krzeminski.snakeyaml.engine.kmp.nodes.SequenceNode;
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag;
import org.snakeyaml.engine.internal.TestConstructNode;

@org.junit.jupiter.api.Tag("fast")
class ConstructNodeTest {

  @Test
  void failToConstructRecursive() {
    ConstructNode constructNode = new TestConstructNode() {

      @Override
      public Object construct(Node node) {
        return null;
      }
    };
    Node node = new SequenceNode(Tag.SEQ,
        Lists.newArrayList(new ScalarNode(Tag.STR, "b", ScalarStyle.PLAIN)), FlowStyle.FLOW);
    node.setRecursive(true);
    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> constructNode.constructRecursive(node, new ArrayList<>()));
    assertEquals("Not implemented in class org.snakeyaml.engine.v2.api.ConstructNodeTest$1",
        exception.getMessage());
  }

  @Test
  void failToConstructNonRecursive() {
    ConstructNode constructNode = new TestConstructNode() {

      @Override
      public Object construct(Node node) {
        return null;
      }
    };
    Node node = new SequenceNode(Tag.SEQ,
        Lists.newArrayList(new ScalarNode(Tag.STR, "b", ScalarStyle.PLAIN)), FlowStyle.FLOW);
    node.setRecursive(false);
    YamlEngineException exception = assertThrows(YamlEngineException.class,
        () -> constructNode.constructRecursive(node, new ArrayList<>()));
    assertTrue(exception.getMessage().startsWith("Unexpected recursive structure for Node"));
  }
}
