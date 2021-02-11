/*
 * Copyright (c) 2018, http://www.snakeyaml.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snakeyaml.engine.v2.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.common.SpecVersion;
import org.snakeyaml.engine.v2.exceptions.EmitterException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("fast")
class DumpSettingsTest {

    @Test
    @DisplayName("Check default values")
    void defaults() {
        DumpSettings settings = DumpSettings.builder().build();

        assertEquals("\n", settings.getBestLineBreak());
        assertEquals(2, settings.getIndent());
        assertEquals(FlowStyle.AUTO, settings.getDefaultFlowStyle());
        assertEquals(ScalarStyle.PLAIN, settings.getDefaultScalarStyle());
        assertEquals(Optional.empty(), settings.getExplicitRootTag());
        assertFalse(settings.getIndentWithIndicator());
        assertEquals(0, settings.getIndicatorIndent());
        assertEquals(128, settings.getMaxSimpleKeyLength());
        assertNull(settings.getNonPrintableStyle()); //TODO should have Optional ?
        assertEquals(80, settings.getWidth());
        assertEquals(Optional.empty(), settings.getYamlDirective());
        assertEquals(new HashMap<>(), settings.getTagDirective());
        assertNotNull(settings.getAnchorGenerator());
        assertNotNull(settings.getScalarResolver());
    }


    @Test
    @DisplayName("testSplitLinesDoubleQuoted")
    void testSplitLinesDoubleQuoted() {
        String data = "1111111111 2222222222 3333333333 4444444444 5555555555 6666666666 7777777777 8888888888 9999999999 0000000000";
        DumpSettings settings = DumpSettings.builder()
                .setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED)
                .setSplitLines(true)
                .build();
        Dump dump = new Dump(settings);
        // Split lines enabled (default)
        assertTrue(settings.isSplitLines());
        assertEquals(80, settings.getWidth());
        String output = dump.dumpToString(data);
        assertEquals("\"1111111111 2222222222 3333333333 4444444444 5555555555 6666666666 7777777777 8888888888\\\n  \\ 9999999999 0000000000\"\n", output);

        // Lines with double spaces can be split too as whitespace can be preserved
        output = dump.dumpToString("1111111111  2222222222  3333333333  4444444444  5555555555  6666666666  7777777777  8888888888  9999999999  0000000000");
        assertEquals("\"1111111111  2222222222  3333333333  4444444444  5555555555  6666666666  7777777777\\\n  \\  8888888888  9999999999  0000000000\"\n", output);

        // Split lines disabled
        DumpSettings settings2 = DumpSettings.builder()
                .setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED)
                .setSplitLines(false)
                .build();
        assertFalse(settings2.isSplitLines());
        Dump dump2 = new Dump(settings2);

        output = dump2.dumpToString(data);
        assertEquals("\"1111111111 2222222222 3333333333 4444444444 5555555555 6666666666 7777777777 8888888888 9999999999 0000000000\"\n", output);
    }

    @Test
    @DisplayName("Canonical output")
    void setCanonical() {
        DumpSettings settings = DumpSettings.builder().setCanonical(true).build();
        Dump dump = new Dump(settings);
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            data.add(i);
        }
        String str = dump.dumpToString(data);
        assertEquals("---\n" +
                "!!seq [\n" +
                "  !!int \"0\",\n" +
                "  !!int \"1\",\n" +
                "]\n", str);
    }

    @Test
    @DisplayName("Show tag directives")
    void setTagDirective() {
        Map<String, String> tagDirectives = new TreeMap<>();
        tagDirectives.put("!yaml!", "tag:yaml.org,2002:");
        tagDirectives.put("!python!", "!python");
        DumpSettings settings = DumpSettings.builder().setTagDirective(tagDirectives).build();
        Dump dump = new Dump(settings);
        String str = dump.dumpToString("data");
        assertEquals("%TAG !python! !python\n" +
                "%TAG !yaml! tag:yaml.org,2002:\n" +
                "--- data\n", str);
    }

    @Test
    @DisplayName("Check corner cases for indent")
    void setIndent() {
        Exception exception1 = assertThrows(EmitterException.class, () -> DumpSettings.builder().setIndent(0));
        assertEquals("Indent must be at least 1", exception1.getMessage());

        Exception exception2 = assertThrows(EmitterException.class, () -> DumpSettings.builder().setIndent(12));
        assertEquals("Indent must be at most 10", exception2.getMessage());
    }

    @Test
    @DisplayName("Check corner cases for Indicator Indent")
    void setIndicatorIndent() {
        Exception exception1 = assertThrows(EmitterException.class, () -> DumpSettings.builder().setIndicatorIndent(-1));
        assertEquals("Indicator indent must be non-negative", exception1.getMessage());

        Exception exception2 = assertThrows(EmitterException.class, () -> DumpSettings.builder().setIndicatorIndent(10));
        assertEquals("Indicator indent must be at most Emitter.MAX_INDENT-1: 9", exception2.getMessage());
    }

    @Test
    @DisplayName("Dump explicit version")
    void dumpVersion() {
        DumpSettings settings = DumpSettings.builder().setYamlDirective(Optional.of(new SpecVersion(1, 2))).build();
        Dump dump = new Dump(settings);
        String str = dump.dumpToString("a");
        assertEquals("%YAML 1.2\n" +
                "--- a\n", str);
    }

    @Test
    void dumpCustomProperty() {
        DumpSettings settings = DumpSettings.builder().setCustomProperty(new KeyName("key"), "value").build();
        assertEquals("value", settings.getCustomProperty(new KeyName("key")));
        assertNull(settings.getCustomProperty(new KeyName("None")));
    }

    static class KeyName implements SettingKey {
        private final String keyName;

        public KeyName(String name) {
            keyName = name;
        }

        public String getKeyName() {
            return keyName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            KeyName keyName1 = (KeyName) o;
            return Objects.equals(keyName, keyName1.keyName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(keyName);
        }
    }
}
