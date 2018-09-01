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
package org.snakeyaml.engine.v1.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.v1.nodes.Tag;

@org.junit.jupiter.api.Tag("fast")
class JsonScalarResolverTest {
    private ScalarResolver scalarResolver = new JsonScalarResolver();

    @Test
    @DisplayName("Resolve explicit scalar")
    void resolveExplicitScalar(TestInfo testInfo) {
        assertEquals(Tag.STR, scalarResolver.resolve("1", false));
    }

    @Test
    @DisplayName("Resolve implicit integer")
    void resolveImplicitInteger(TestInfo testInfo) {
        assertEquals(Tag.INT, scalarResolver.resolve("1", true));
        assertEquals(Tag.INT, scalarResolver.resolve("-1", true));
        assertEquals(Tag.INT, scalarResolver.resolve("-01", true));
        assertEquals(Tag.INT, scalarResolver.resolve("013", true));
        assertEquals(Tag.INT, scalarResolver.resolve("0", true));
    }

    @Test
    @DisplayName("Resolve implicit float")
    void resolveImplicitFloat(TestInfo testInfo) {
        assertEquals(Tag.FLOAT, scalarResolver.resolve("1.0", true));
        assertEquals(Tag.FLOAT, scalarResolver.resolve("-1.3", true));
        assertEquals(Tag.FLOAT, scalarResolver.resolve("+01.445", true));
        assertEquals(Tag.FLOAT, scalarResolver.resolve("-1.455e45", true));
        assertEquals(Tag.FLOAT, scalarResolver.resolve("1.455E-045", true));
        assertEquals(Tag.FLOAT, scalarResolver.resolve("0.0", true));
        assertEquals(Tag.FLOAT, scalarResolver.resolve("+1", true));
    }

    @Test
    @DisplayName("Resolve implicit boolean")
    void resolveImplicitBoolean(TestInfo testInfo) {
        assertEquals(Tag.BOOL, scalarResolver.resolve("true", true));
        assertEquals(Tag.BOOL, scalarResolver.resolve("false", true));
        assertEquals(Tag.STR, scalarResolver.resolve("False", true));
        assertEquals(Tag.STR, scalarResolver.resolve("FALSE", true));
        assertEquals(Tag.STR, scalarResolver.resolve("off", true));
        assertEquals(Tag.STR, scalarResolver.resolve("no", true));
    }

    @Test
    @DisplayName("Resolve implicit null")
    void resolveImplicitNull(TestInfo testInfo) {
        assertEquals(Tag.NULL, scalarResolver.resolve("null", true));
        assertEquals(Tag.NULL, scalarResolver.resolve("", true));
    }

    @Test
    @DisplayName("Resolve implicit strings")
    void resolveImplicitStrings(TestInfo testInfo) {
        assertEquals(Tag.STR, scalarResolver.resolve(".inf", true));
        assertEquals(Tag.STR, scalarResolver.resolve("0xFF", true));
        assertEquals(Tag.STR, scalarResolver.resolve("True", true));
        assertEquals(Tag.STR, scalarResolver.resolve("TRUE", true));
        assertEquals(Tag.STR, scalarResolver.resolve("NULL", true));
        assertEquals(Tag.STR, scalarResolver.resolve("~", true));
    }
}
