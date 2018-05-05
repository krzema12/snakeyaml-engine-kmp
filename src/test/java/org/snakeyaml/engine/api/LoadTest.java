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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

@Tag("fast")
class LoadTest {

    @Test
    @DisplayName("String 'a' is parsed")
    void parseString(TestInfo testInfo) {
        LoadSettings settings = new LoadSettings();
        Load load = new Load(settings);
        String str = (String) load.loadFromString("a");
        assertEquals("a", str);
    }

    @Test
    @DisplayName("Integer 1 is parsed")
    void parseInteger(TestInfo testInfo) {
        LoadSettings settings = new LoadSettings();
        Load load = new Load(settings);
        Integer integer = (Integer) load.loadFromString("1");
        assertEquals(new Integer(1), integer);
    }

    @Test
    @DisplayName("Boolean true is parsed")
    void parseBoolean(TestInfo testInfo) {
        LoadSettings settings = new LoadSettings();
        Load load = new Load(settings);
        assertTrue((Boolean) load.loadFromString("true"));
    }

    @Test
    @DisplayName("null is parsed")
    void parseNull(TestInfo testInfo) {
        LoadSettings settings = new LoadSettings();
        Load load = new Load(settings);
        assertNull(load.loadFromString(""));
    }

    @Test
    @DisplayName("null tag is parsed")
    void parseNullTag(TestInfo testInfo) {
        LoadSettings settings = new LoadSettings();
        Load load = new Load(settings);
        assertNull(load.loadFromString("!!null"));
    }

    @Test
    @DisplayName("Float is parsed")
    void parseFloat(TestInfo testInfo) {
        LoadSettings settings = new LoadSettings();
        Load load = new Load(settings);
        Double doubleValue = (Double) load.loadFromString("1.01");
        assertEquals(new Double(1.01), doubleValue);
    }

}
