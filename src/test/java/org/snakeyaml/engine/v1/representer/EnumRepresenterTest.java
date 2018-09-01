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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.v1.api.Dump;
import org.snakeyaml.engine.v1.api.DumpSettings;
import org.snakeyaml.engine.v1.api.DumpSettingsBuilder;
import org.snakeyaml.engine.v1.common.ScalarStyle;
import org.snakeyaml.engine.v1.nodes.ScalarNode;

@Tag("fast")
class EnumRepresenterTest {

    @Test
    @DisplayName("Represent Enum as node with global tag")
    void represenEnum(TestInfo testInfo) {
        DumpSettings settings = new DumpSettingsBuilder().setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED).build();
        StandardRepresenter standardRepresenter = new StandardRepresenter(settings);
        ScalarNode node = (ScalarNode) standardRepresenter.represent(FormatEnum.JSON);
        assertEquals(ScalarStyle.DOUBLE_QUOTED, node.getScalarStyle());
        assertEquals("tag:yaml.org,2002:org.snakeyaml.engine.v1.representer.FormatEnum", node.getTag().getValue());
    }

    @Test
    @DisplayName("Dump Enum with ScalarStyle.DOUBLE_QUOTED")
    void dumpEnum(TestInfo testInfo) {
        DumpSettings settings = new DumpSettingsBuilder().setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED).build();
        Dump dumper = new Dump(settings);
        String node = dumper.dumpToString(FormatEnum.JSON);
        assertEquals("!!org.snakeyaml.engine.v1.representer.FormatEnum \"JSON\"\n", node);
    }
}
