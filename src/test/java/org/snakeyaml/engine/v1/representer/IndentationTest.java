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
package org.snakeyaml.engine.v1.representer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.v1.api.Dump;
import org.snakeyaml.engine.v1.api.DumpSettings;
import org.snakeyaml.engine.v1.api.DumpSettingsBuilder;
import org.snakeyaml.engine.v1.common.FlowStyle;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for issue https://bitbucket.org/asomov/snakeyaml-engine/issues/9/indentation-before-sequence
 */
@Tag("fast")
class IndentationTest {

    private ArrayList createList(String v1, String v2) {
        ArrayList<Object> sequence = new ArrayList();
        sequence.add(v1);
        sequence.add(v2);
        return sequence;
    }

    private LinkedHashMap<Object, Object> createMap() {
        LinkedHashMap<Object, Object> mapping = new LinkedHashMap();
        mapping.put("key1", createList("value1", "value2"));
        mapping.put("key2", createList("value3", "value4"));
        return mapping;
    }

    private ArrayList<Object> createSequence() {
        ArrayList<Object> sequence = new ArrayList();
        LinkedHashMap<Object, Object> mapping1 = new LinkedHashMap();
        mapping1.put("key1", "value1");
        mapping1.put("key2", "value2");
        sequence.add(mapping1);
        LinkedHashMap<Object, Object> mapping2 = new LinkedHashMap();
        mapping2.put("key3", "value3");
        mapping2.put("key4", "value4");
        sequence.add(mapping2);
        return sequence;
    }

    private Dump createDump(int indicatorIndent) {
        DumpSettingsBuilder builder = new DumpSettingsBuilder();
        builder.setDefaultFlowStyle(FlowStyle.BLOCK);
        builder.setIndicatorIndent(indicatorIndent);
        builder.setIndent(indicatorIndent + 2);
        DumpSettings settings = builder.build();
        Dump dump = new Dump(settings);
        return dump;
    }

    @Test
    @DisplayName("Dump block map seq with default indent settings")
    void dumpBlockMappingSequenceWithDefaultSettings(TestInfo testInfo) {
        Dump dump = createDump(0);
        String output = dump.dumpToString(createMap());
        assertEquals("key1:\n" +
                "- value1\n" +
                "- value2\n" +
                "key2:\n" +
                "- value3\n" +
                "- value4\n", output);
    }

    @Test
    @DisplayName("Dump block seq map with default indent settings")
    void dumpBlockSequenceMappingWithDefaultSettings(TestInfo testInfo) {
        Dump dump = createDump(0);
        String output = dump.dumpToString(createSequence());
        assertEquals("- key1: value1\n" +
                "  key2: value2\n" +
                "- key3: value3\n" +
                "  key4: value4\n", output);
    }

    @Test
    @DisplayName("Dump block seq map with specified indicator indent")
    void dumpBlockMappingSequence(TestInfo testInfo) {
        Dump dump = createDump(2);
        String output = dump.dumpToString(createMap());
        assertEquals("key1:\n" +
                "  - value1\n" +
                "  - value2\n" +
                "key2:\n" +
                "  - value3\n" +
                "  - value4\n", output);
    }

    @Test
    @DisplayName("Dump block seq map with indicatorIndent=2")
    void dumpBlockSequenceMapping(TestInfo testInfo) {
        Dump dump = createDump(2);
        String output = dump.dumpToString(createSequence());
        assertEquals("  - key1: value1\n" +
                "    key2: value2\n" +
                "  - key3: value3\n" +
                "    key4: value4\n", output);
    }
}
