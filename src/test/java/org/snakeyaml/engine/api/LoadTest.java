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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

@Tag("fast")
class LoadTest {

    @Test
    @DisplayName("String 'a' is parsed")
    void parseString(TestInfo testInfo) {
        LoadSettings settings = new LoadSettingsBuilder().build();
        Load load = new Load(settings);
        String str = (String) load.loadFromString("a");
        assertEquals("a", str);
    }

    @Test
    @DisplayName("Integer 1 is parsed")
    void parseInteger(TestInfo testInfo) {
        LoadSettings settings = new LoadSettingsBuilder().build();
        Load load = new Load(settings);
        Integer integer = (Integer) load.loadFromString("1");
        assertEquals(new Integer(1), integer);
    }

    @Test
    @DisplayName("Boolean true is parsed")
    void parseBoolean(TestInfo testInfo) {
        LoadSettings settings = new LoadSettingsBuilder().build();
        Load load = new Load(settings);
        assertTrue((Boolean) load.loadFromString("true"));
    }

    @Test
    @DisplayName("null is parsed")
    void parseNull(TestInfo testInfo) {
        LoadSettings settings = new LoadSettingsBuilder().build();
        Load load = new Load(settings);
        assertNull(load.loadFromString(""));
    }

    @Test
    @DisplayName("null tag is parsed")
    void parseNullTag(TestInfo testInfo) {
        LoadSettings settings = new LoadSettingsBuilder().build();
        Load load = new Load(settings);
        assertNull(load.loadFromString("!!null"));
    }

    @Test
    @DisplayName("Float is parsed")
    void parseFloat(TestInfo testInfo) {
        LoadSettings settings = new LoadSettingsBuilder().build();
        Load load = new Load(settings);
        Double doubleValue = (Double) load.loadFromString("1.01");
        assertEquals(new Double(1.01), doubleValue);
    }

    @Test
    @DisplayName("Load from InputStream")
    void loadFromInputStream(TestInfo testInfo) {
        LoadSettings settings = new LoadSettingsBuilder().build();
        Load load = new Load(settings);
        String v = (String) load.loadFromInputStream(new ByteArrayInputStream("aaa".getBytes()));
        assertEquals("aaa", v);
    }

    @Test
    @DisplayName("Load from Reader")
    void loadFromReader(TestInfo testInfo) {
        LoadSettings settings = new LoadSettingsBuilder().build();
        Load load = new Load(settings);
        String v = (String) load.loadFromReader(new StringReader("bbb"));
        assertEquals("bbb", v);
    }

    @Test
    @DisplayName("Load all from String")
    void loadAllFromString(TestInfo testInfo) {
        LoadSettings settings = new LoadSettingsBuilder().build();
        Load load = new Load(settings);
        InputStream input = new ByteArrayInputStream("bbb\n---\nccc\n---\nddd".getBytes());
        Iterable<Object> v = load.loadAllFromInputStream(input);
        Iterator<Object> iter = v.iterator();
        assertTrue(iter.hasNext());
        Object o1 = iter.next();
        assertEquals("bbb", o1);

        assertTrue(iter.hasNext());
        Object o2 = iter.next();
        assertEquals("ccc", o2);

        assertTrue(iter.hasNext());
        Object o3 = iter.next();
        assertEquals("ddd", o3);

        assertFalse(iter.hasNext());
    }

    @Test
    @DisplayName("Load all from String")
    void loadIterableFromString(TestInfo testInfo) {
        LoadSettings settings = new LoadSettingsBuilder().build();
        Load load = new Load(settings);
        Iterable<Object> v = load.loadAllFromString("1\n---\n2\n---\n3");
        int counter = 1;
        for (Object o : v) {
            assertEquals(counter++, o);
        }
    }

    @Test
    @DisplayName("Load all from Reader")
    void loadAllFromReader(TestInfo testInfo) {
        LoadSettings settings = new LoadSettingsBuilder().build();
        Load load = new Load(settings);
        Iterable<Object> v = load.loadAllFromReader(new StringReader("bbb"));
        Iterator<Object> iter = v.iterator();
        assertTrue(iter.hasNext());
        Object o1 = iter.next();
        assertEquals("bbb", o1);
        assertFalse(iter.hasNext());
    }

    @Test
    @DisplayName("Throw UnsupportedOperationException if try to remove from iterator")
    void loadAllFromStringWithUnsupportedOperationException(TestInfo testInfo) {
        LoadSettings settings = new LoadSettingsBuilder().build();
        Load load = new Load(settings);
        Iterable<Object> v = load.loadAllFromString("bbb");
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () ->
                v.iterator().remove());
        assertEquals("Removing is not supported.", exception.getMessage());
    }

}
