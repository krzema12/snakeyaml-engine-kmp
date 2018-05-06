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
package org.snakeyaml.engine.api.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.api.Dump;
import org.snakeyaml.engine.api.DumpSettings;
import org.snakeyaml.engine.nodes.Node;
import org.snakeyaml.engine.representer.StandardRepresenter;

import com.google.common.collect.Lists;

@org.junit.jupiter.api.Tag("fast")
class OptionalTest {

    @Test
    @DisplayName("Represent Optional as value")
    void representOptional(TestInfo testInfo) {
        StandardRepresenter standardRepresenter = new StandardRepresenter();
        Node node = standardRepresenter.represent(Optional.of("a"));
        assertEquals("tag:yaml.org,2002:str", node.getTag().getValue());
    }

    @Test
    @DisplayName("Represent Optional.empty as null")
    void representEmptyOptional(TestInfo testInfo) {
        StandardRepresenter standardRepresenter = new StandardRepresenter();
        Node node = standardRepresenter.represent(Optional.empty());
        assertEquals("tag:yaml.org,2002:null", node.getTag().getValue());
    }

    @Test
    @DisplayName("Dump Optional as its value")
    void dumpOptional(TestInfo testInfo) {
        DumpSettings settings = new DumpSettings();
        Dump dump = new Dump(settings);
        String str = dump.dumpToString(Optional.of("a"));
        assertEquals("a\n", str);
    }

    @Test
    @DisplayName("Dump Optional as its value")
    void dumpEmptyOptional(TestInfo testInfo) {
        DumpSettings settings = new DumpSettings();
        Dump dump = new Dump(settings);
        String str = dump.dumpToString(Optional.empty());
        assertEquals("null\n", str);
    }

    @Test
    @DisplayName("Dump Optionals")
    void dumpListOfOptional(TestInfo testInfo) {
        DumpSettings settings = new DumpSettings();
        Dump dump = new Dump(settings);
        String str = dump.dumpToString(Lists.newArrayList(Optional.of(2), Optional.empty(), Optional.of("a")));
        assertEquals("[2, null, a]\n", str);
    }

}
