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
package org.snakeyaml.engine.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.nodes.NodeType;
import org.snakeyaml.engine.nodes.Tag;

@org.junit.jupiter.api.Tag("fast")
class JsonResolverTest {
    private Resolver resolver = new JsonResolver();

    @Test
    @DisplayName("Resolve MAP does not depend on value or boolean")
    void resolveMap(TestInfo testInfo) {
        assertEquals(Tag.MAP, resolver.resolve(NodeType.MAPPING, "Foo", true));
        assertEquals(Tag.MAP, resolver.resolve(NodeType.MAPPING, "Foo", false));
    }

    @Test
    @DisplayName("Resolve SEQ does not depend on value or boolean")
    void resolveSequence(TestInfo testInfo) {
        assertEquals(Tag.SEQ, resolver.resolve(NodeType.SEQUENCE, "Foo", true));
        assertEquals(Tag.SEQ, resolver.resolve(NodeType.SEQUENCE, "Foo", false));
    }

    @Test
    @DisplayName("Resolve explicit scalar")
    void resolveExplicitScalar(TestInfo testInfo) {
        assertEquals(Tag.STR, resolver.resolve(NodeType.SCALAR, "1", false));
    }

    @Test
    @DisplayName("Resolve implicit integer")
    void resolveImplicitInteger(TestInfo testInfo) {
        assertEquals(Tag.INT, resolver.resolve(NodeType.SCALAR, "1", true));
        assertEquals(Tag.INT, resolver.resolve(NodeType.SCALAR, "-1", true));
        assertEquals(Tag.INT, resolver.resolve(NodeType.SCALAR, "-01", true));
        assertEquals(Tag.INT, resolver.resolve(NodeType.SCALAR, "013", true));
        assertEquals(Tag.INT, resolver.resolve(NodeType.SCALAR, "0", true));
    }

    @Test
    @DisplayName("Resolve implicit float")
    void resolveImplicitFloat(TestInfo testInfo) {
        assertEquals(Tag.FLOAT, resolver.resolve(NodeType.SCALAR, "1.0", true));
        assertEquals(Tag.FLOAT, resolver.resolve(NodeType.SCALAR, "-1.3", true));
        assertEquals(Tag.FLOAT, resolver.resolve(NodeType.SCALAR, "+01.445", true));
        assertEquals(Tag.FLOAT, resolver.resolve(NodeType.SCALAR, "-1.455e45", true));
        assertEquals(Tag.FLOAT, resolver.resolve(NodeType.SCALAR, "1.455E-045", true));
        assertEquals(Tag.FLOAT, resolver.resolve(NodeType.SCALAR, "0.0", true));
        assertEquals(Tag.FLOAT, resolver.resolve(NodeType.SCALAR, "+1", true));
    }

    @Test
    @DisplayName("Resolve implicit boolean")
    void resolveImplicitBoolean(TestInfo testInfo) {
        assertEquals(Tag.BOOL, resolver.resolve(NodeType.SCALAR, "true", true));
        assertEquals(Tag.BOOL, resolver.resolve(NodeType.SCALAR, "false", true));
        assertEquals(Tag.STR, resolver.resolve(NodeType.SCALAR, "False", true));
        assertEquals(Tag.STR, resolver.resolve(NodeType.SCALAR, "FALSE", true));
        assertEquals(Tag.STR, resolver.resolve(NodeType.SCALAR, "off", true));
        assertEquals(Tag.STR, resolver.resolve(NodeType.SCALAR, "no", true));
    }

    @Test
    @DisplayName("Resolve implicit null")
    void resolveImplicitNull(TestInfo testInfo) {
        assertEquals(Tag.NULL, resolver.resolve(NodeType.SCALAR, "null", true));
        assertEquals(Tag.NULL, resolver.resolve(NodeType.SCALAR, "", true));
    }

    @Test
    @DisplayName("Resolve implicit strings")
    void resolveImplicitStrings(TestInfo testInfo) {
        assertEquals(Tag.STR, resolver.resolve(NodeType.SCALAR, ".inf", true));
        assertEquals(Tag.STR, resolver.resolve(NodeType.SCALAR, "0xFF", true));
        assertEquals(Tag.STR, resolver.resolve(NodeType.SCALAR, "True", true));
        assertEquals(Tag.STR, resolver.resolve(NodeType.SCALAR, "TRUE", true));
        assertEquals(Tag.STR, resolver.resolve(NodeType.SCALAR, "NULL", true));
        assertEquals(Tag.STR, resolver.resolve(NodeType.SCALAR, "~", true));
    }
}
