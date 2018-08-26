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
package org.snakeyaml.engine.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.utils.TestUtils;

import com.google.common.collect.Lists;

@Tag("fast")
class LoadSequenceTest {

    @Test
    @DisplayName("Empty list [] is parsed")
    void parseEmptyList(TestInfo testInfo) {
        LoadSettings settings = new LoadSettingsBuilder().build();
        Load load = new Load(settings);
        List<Integer> list = (List<Integer>) load.loadFromString("[]");
        assertEquals(new ArrayList<>(), list);
    }

    @Test
    @DisplayName("list [2] is parsed")
    void parseList1(TestInfo testInfo) {
        LoadSettings settings = new LoadSettingsBuilder().build();
        Load load = new Load(settings);
        List<Integer> list = (List<Integer>) load.loadFromString("[2]");
        assertEquals(Collections.singletonList(new Integer(2)), list);
    }

    @Test
    @DisplayName("list [2,3] is parsed")
    void parseList2(TestInfo testInfo) {
        LoadSettings settings = new LoadSettingsBuilder().build();
        Load load = new Load(settings);
        List<Integer> list = (List<Integer>) load.loadFromString("[2,3]");
        assertEquals(Lists.newArrayList(2, 3), list);
    }

    @Test
    @DisplayName("list [2,a,true] is parsed")
    void parseList3(TestInfo testInfo) {
        LoadSettings settings = new LoadSettingsBuilder().build();
        Load load = new Load(settings);
        List<Object> list = (List<Object>) load.loadFromString("[2,a,true]");
        assertEquals(Lists.newArrayList(2, "a", Boolean.TRUE), list);
    }

    @Test
    @DisplayName("list is parsed")
    void parseList4(TestInfo testInfo) {
        LoadSettings settings = new LoadSettingsBuilder().build();
        Load load = new Load(settings);
        List<Object> list = (List<Object>) load.loadFromString(TestUtils.getResource("load/list1.yaml"));
        assertEquals(Lists.newArrayList("a", "bb", "ccc", "dddd"), list);
    }
}
