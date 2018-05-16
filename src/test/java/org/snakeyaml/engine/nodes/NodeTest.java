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
package org.snakeyaml.engine.nodes;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.common.ScalarStyle;

@org.junit.jupiter.api.Tag("fast")
class NodeTest {

    @Test
    void notEqualToTheSameNode(TestInfo testInfo) {
        Node node1 = new ScalarNode(Tag.STR, "a", ScalarStyle.PLAIN);
        Node node2 = new ScalarNode(Tag.STR, "a", ScalarStyle.PLAIN);
        assertFalse(node1.equals(node2), "Nodes with the same contant are not equal");
        assertFalse(node2.equals(node1), "Nodes with the same contant are not equal");
    }

    @Test
    void equalsToItself(TestInfo testInfo) {
        Node node = new ScalarNode(org.snakeyaml.engine.nodes.Tag.STR, "a", ScalarStyle.PLAIN);
        assertTrue(node.equals(node));
    }
}
