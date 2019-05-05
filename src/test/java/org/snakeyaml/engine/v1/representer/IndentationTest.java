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

    @Test
    @DisplayName("Dump block map seq with default indent settings")
    void dumpBlockMappingSequenceWithDefaultSettings(TestInfo testInfo) {
        DumpSettingsBuilder builder = new DumpSettingsBuilder();
        builder.setDefaultFlowStyle(FlowStyle.BLOCK);
        DumpSettings settings = builder.build();
        Dump dump = new Dump(settings);
        LinkedHashMap<Object, Object> mapping = new LinkedHashMap();
        ArrayList<Object> sequence1 = new ArrayList();
        sequence1.add("value1");
        sequence1.add("value2");
        mapping.put("key1", sequence1);
        ArrayList<Object> sequence2 = new ArrayList();
        sequence2.add("value3");
        sequence2.add("value4");
        mapping.put("key2", sequence2);
        String output = dump.dumpToString(mapping);
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
        DumpSettingsBuilder builder = new DumpSettingsBuilder();
        builder.setDefaultFlowStyle(FlowStyle.BLOCK);
        DumpSettings settings = builder.build();
        Dump dump = new Dump(settings);
        ArrayList<Object> sequence = new ArrayList();
        LinkedHashMap<Object, Object> mapping1 = new LinkedHashMap();
        mapping1.put("key1", "value1");
        mapping1.put("key2", "value2");
        sequence.add(mapping1);
        LinkedHashMap<Object, Object> mapping2 = new LinkedHashMap();
        mapping2.put("key3", "value3");
        mapping2.put("key4", "value4");
        sequence.add(mapping2);
        String output = dump.dumpToString(sequence);
        assertEquals("- key1: value1\n" +
                "  key2: value2\n" +
                "- key3: value3\n" +
                "  key4: value4\n", output);
    }

    @Test
    @DisplayName("Dump block map seq with specified indicator indent")
    void dumpBlockMappingSequence(TestInfo testInfo) {
        DumpSettingsBuilder builder = new DumpSettingsBuilder();
        builder.setDefaultFlowStyle(FlowStyle.BLOCK);
        builder.setIndicatorIndent(2);
        DumpSettings settings = builder.build();
        Dump dump = new Dump(settings);
        LinkedHashMap<Object, Object> mapping = new LinkedHashMap();
        ArrayList<Object> sequence1 = new ArrayList();
        sequence1.add("value1");
        sequence1.add("value2");
        mapping.put("key1", sequence1);
        ArrayList<Object> sequence2 = new ArrayList();
        sequence2.add("value3");
        sequence2.add("value4");
        mapping.put("key2", sequence2);
        String output = dump.dumpToString(mapping);
        assertEquals("key1:\n" +
                "  - value1\n" +
                "  - value2\n" +
                "key2:\n" +
                "  - value3\n" +
                "  - value4\n", output);
    }
}
