/*
 * Copyright (c) 2018, SnakeYAML
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("fast")
class DumpTest {

    @Test
    @DisplayName("Dump string")
    void dumpString() {
        DumpSettings settings = DumpSettings.builder().build();
        Dump dump = new Dump(settings);
        String str = dump.dumpToString("a");
        assertEquals("a\n", str);
    }

    @Test
    @DisplayName("Dump int")
    void dumpInteger() {
        DumpSettings settings = DumpSettings.builder().build();
        Dump dump = new Dump(settings);
        String str = dump.dumpToString(Integer.valueOf(1));
        assertEquals("1\n", str);
    }

    @Test
    @DisplayName("Dump boolean")
    void dumpBoolean() {
        DumpSettings settings = DumpSettings.builder().build();
        Dump dump = new Dump(settings);
        String str = dump.dumpToString(Boolean.TRUE);
        assertEquals("true\n", str);
    }

    @Test
    @DisplayName("Dump seq")
    void dumpSequence() {
        DumpSettings settings = DumpSettings.builder().build();
        Dump dump = new Dump(settings);
        String str = dump.dumpToString(Lists.newArrayList(2, "a", Boolean.TRUE));
        assertEquals("[2, a, true]\n", str);
    }

    @Test
    @DisplayName("Dump map")
    void dumpMapping() {
        DumpSettings settings = DumpSettings.builder().build();
        Dump dump = new Dump(settings);
        String output = dump.dumpToString(ImmutableMap.of("x", 1, "y", 2, "z", 3));
        assertEquals("{x: 1, y: 2, z: 3}\n", output);
    }

    @Test
    @DisplayName("Dump all instances")
    void dumpAll() {
        DumpSettings settings = DumpSettings.builder().build();
        Dump dump = new Dump(settings);
        StreamToStringWriter streamToStringWriter = new StreamToStringWriter();
        List<Object> list = Lists.newArrayList("a", null, Boolean.TRUE);
        dump.dumpAll(list.iterator(), streamToStringWriter);
        assertEquals("a\n" +
                "--- null\n" +
                "--- true\n", streamToStringWriter.toString());
        //load back
        LoadSettings loadSettings = LoadSettings.builder().build();
        Load load = new Load(loadSettings);
        for (Object obj : load.loadAllFromString(streamToStringWriter.toString())) {
            assertEquals(list.remove(0), obj);
        }
    }

    @Test
    @DisplayName("Dump all instances")
    void dumpAllToString() {
        DumpSettings settings = DumpSettings.builder().build();
        Dump dump = new Dump(settings);
        List<Object> list = Lists.newArrayList("a", null, Boolean.TRUE);
        String output = dump.dumpAllToString(list.iterator());
        assertEquals("a\n" +
                "--- null\n" +
                "--- true\n", output);
        //load back
        LoadSettings loadSettings = LoadSettings.builder().build();
        Load load = new Load(loadSettings);
        for (Object obj : load.loadAllFromString(output)) {
            assertEquals(list.remove(0), obj);
        }
    }

    @Test
    @DisplayName("Dump to File")
    void dumpToFile() throws IOException {
        DumpSettings settings = DumpSettings.builder().build();
        Dump dump = new Dump(settings);
        File file = new File("target/temp.yaml");
        file.delete();
        assertFalse(file.exists());
        file.createNewFile();
        StreamDataWriter writer = new YamlOutputStreamWriter(new FileOutputStream(file),
                StandardCharsets.UTF_8) {
            @Override
            public void processIOException(IOException e) {
                throw new RuntimeException(e);
            }
        };
        dump.dump(ImmutableMap.of("x", 1, "y", 2, "z", 3), writer);
        assertTrue(file.exists());
        file.delete();//on Windows the file is not deleted
    }
}
