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
package org.snakeyaml.engine.usecases.custom_collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.v1.api.Load;
import org.snakeyaml.engine.v1.api.LoadSettings;

import java.util.LinkedList;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@org.junit.jupiter.api.Tag("fast")
class CustomDefaultCollectionsTest {

    @Test
    @DisplayName("Create LinkedList by default")
    void createLinkedListByDefault(TestInfo testInfo) {
        //init size is not used in LinkedList
        LoadSettings settings = LoadSettings.builder().setDefaultList((initSize) -> new LinkedList()).build();
        Load load = new Load(settings);
        LinkedList<String> list = (LinkedList<String>) load.loadFromString("- a\n- b");
        assertEquals(2, list.size());
    }

    @Test
    @DisplayName("Create TreeMap by default")
    void createTreeMapByDefault(TestInfo testInfo) {
        //init size is not used in TreeMap
        LoadSettings settings = LoadSettings.builder().setDefaultMap((initSize) -> new TreeMap()).build();
        Load load = new Load(settings);
        TreeMap<String, String> list = (TreeMap<String, String>) load.loadFromString("{k1: v1, k2: v2}");
        assertEquals(2, list.size());
    }
}

