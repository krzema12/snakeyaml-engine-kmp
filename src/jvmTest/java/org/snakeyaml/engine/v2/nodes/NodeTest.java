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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import it.krzeminski.snakeyaml.engine.kmp.nodes.Node;
import it.krzeminski.snakeyaml.engine.kmp.nodes.ScalarNode;
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag;
import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle;

@org.junit.jupiter.api.Tag("fast")
class NodeTest {

  @Test
  void notEqualToTheSameNode() {
    Node node1 = new ScalarNode(Tag.STR, "a", ScalarStyle.PLAIN);
    Node node2 = new ScalarNode(Tag.STR, "a", ScalarStyle.PLAIN);
    assertNotEquals(node1, node2, "Nodes with the same content are not equal");
    assertNotEquals(node2, node1, "Nodes with the same content are not equal");
  }

  @Test
  void equalsToItself() {
    Node node = new ScalarNode(Tag.STR, "a", ScalarStyle.PLAIN);
    assertEquals(node, node);
  }

  @Test
  void properties() {
    Node node = new ScalarNode(Tag.STR, "a", ScalarStyle.PLAIN);
    assertNull(node.getProperty("p"));
    assertNull(node.setProperty("p", "value"));
    assertEquals("value", node.getProperty("p"));
  }
}
