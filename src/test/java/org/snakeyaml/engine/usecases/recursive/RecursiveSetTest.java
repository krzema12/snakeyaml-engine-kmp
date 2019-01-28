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
package org.snakeyaml.engine.usecases.recursive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.v1.api.Load;
import org.snakeyaml.engine.v1.api.LoadSettings;
import org.snakeyaml.engine.v1.api.LoadSettingsBuilder;
import org.snakeyaml.engine.v1.exceptions.YamlEngineException;
import org.snakeyaml.engine.v1.utils.TestUtils;

@Tag("fast")
class RecursiveSetTest {

    @Test
    @DisplayName("Fail to load map with recursive keys")
    void failToLoadRecursiveSetByDefault(TestInfo testInfo) {
        String recursiveInput = TestUtils.getResource("recursive/recursive-set-1.yaml");
        LoadSettings settings = new LoadSettingsBuilder().build();
        Load load = new Load(settings);
        //fail to load map which has only one key - reference to itself
        YamlEngineException exception = assertThrows(YamlEngineException.class, () -> load.loadFromString(recursiveInput));
        assertEquals("Recursive key for mapping is detected but it is not configured to be allowed.", exception.getMessage());
    }

    @Test
    @DisplayName("Load map with recursive keys if it is explicitly allowed")
    void loadRecursiveSetIfAllowed(TestInfo testInfo) {
        String recursiveInput = TestUtils.getResource("recursive/recursive-set-1.yaml");
        LoadSettings settings = new LoadSettingsBuilder().setAllowRecursiveKeys(true).build();
        Load load = new Load(settings);
        //load map which has only one key - reference to itself
        Set recursive = (Set<Object>) load.loadFromString(recursiveInput);
        assertEquals(3, recursive.size());
    }
}
