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

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.api.Dump;
import org.snakeyaml.engine.api.DumpSettings;
import org.snakeyaml.engine.api.Load;
import org.snakeyaml.engine.api.LoadSettings;
import org.snakeyaml.engine.nodes.Node;
import org.snakeyaml.engine.representer.StandardRepresenter;

@org.junit.jupiter.api.Tag("fast")
class UuidTest {

    private static final UUID THE_UUID = UUID.fromString("37e6a9fa-52d3-11e8-9c2d-fa7ae01bbebc");

    @Test
    @DisplayName("Represent UUID as node with global tag")
    void representUUID(TestInfo testInfo) {
        StandardRepresenter standardRepresenter = new StandardRepresenter(new DumpSettings());
        Node node = standardRepresenter.represent(THE_UUID);
        assertEquals("tag:yaml.org,2002:java.util.UUID", node.getTag().getValue());
    }

    @Test
    @DisplayName("Dump UUID as string")
    void dumpUuid(TestInfo testInfo) {
        DumpSettings settings = new DumpSettings();
        Dump dump = new Dump(settings);
        String output = dump.dumpToString(THE_UUID);
        assertEquals("!!java.util.UUID '37e6a9fa-52d3-11e8-9c2d-fa7ae01bbebc'\n", output);
    }

    @Test
    @DisplayName("Parse UUID")
    void parseUuid(TestInfo testInfo) {
        LoadSettings settings = new LoadSettings();
        Load load = new Load(settings);
        UUID uuid = (UUID) load.loadFromString("!!java.util.UUID '37e6a9fa-52d3-11e8-9c2d-fa7ae01bbebc'\n");
        assertEquals(THE_UUID, uuid);
    }

    @Test
    @DisplayName("Parse UUID as root")
    void parseUuidAsRoot(TestInfo testInfo) {
        LoadSettings settings = new LoadSettings();
        Load load = new Load(settings);
        UUID uuid = (UUID) load.loadFromString("!!java.util.UUID '37e6a9fa-52d3-11e8-9c2d-fa7ae01bbebc'\n");
        assertEquals(THE_UUID, uuid);
    }
}
