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
package org.snakeyaml.engine.v1.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.v1.common.SpecVersion;

@Tag("fast")
class DumpSettingsTest {

    @Test
    @DisplayName("Dump explicit version")
    void dumpVersion(TestInfo testInfo) {
        DumpSettings settings = new DumpSettingsBuilder().setYamlDirective(Optional.of(new SpecVersion(1, 2))).build();
        Dump dump = new Dump(settings);
        String str = dump.dumpToString("a");
        assertEquals("%YAML 1.2\n" +
                "--- a\n", str);
    }


}
