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
package org.snakeyaml.engine.recursive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.api.Load;
import org.snakeyaml.engine.api.LoadSettings;

import com.google.common.collect.ImmutableMap;

@Tag("fast")
class RecursiveMapTest {

    @Test
    @DisplayName("Load map with recursive values")
    void loadRecursiveMap(TestInfo testInfo) {
        LoadSettings settings = new LoadSettings();
        Load load = new Load(settings);
        Map<String, String> map = (Map<String, String>) load.loadFromString("First occurrence: &anchor Foo\n" +
                "Second occurrence: *anchor\n" +
                "Override anchor: &anchor Bar\n" +
                "Reuse anchor: *anchor\n");
        Map<String, String> expected = ImmutableMap.of("First occurrence", "Foo",
                "Second occurrence", "Foo",
                "Override anchor", "Bar",
                "Reuse anchor", "Bar");
        assertEquals(expected, map);
    }

}
