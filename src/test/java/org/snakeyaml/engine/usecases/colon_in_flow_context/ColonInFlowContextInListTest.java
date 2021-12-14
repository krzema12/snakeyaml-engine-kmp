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
package org.snakeyaml.engine.usecases.colon_in_flow_context;

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@org.junit.jupiter.api.Tag("fast")
class ColonInFlowContextInListTest {
    @Test
    void withSpacesAround() {
        Load loader = new Load(LoadSettings.builder().build());
        List<String> list = (List<String>) loader.loadFromString("[ http://foo ]");
        assertTrue(list.contains("http://foo"));
    }

    @Test
    void withoutSpacesAround() {
        Load loader = new Load(LoadSettings.builder().build());
        List<String> list = (List<String>) loader.loadFromString("[http://foo]");
        assertTrue(list.contains("http://foo"));
    }

    @Test
    void twoValues() {
        Load loader = new Load(LoadSettings.builder().build());
        List<String> list = (List<String>) loader.loadFromString("[ http://foo,http://bar ]");
        assertTrue(list.contains("http://foo"));
        assertTrue(list.contains("http://bar"));
    }
}


