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
package org.snakeyaml.engine.colon_in_flow_context;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.v1.api.Load;
import org.snakeyaml.engine.v1.api.LoadSettingsBuilder;

@org.junit.jupiter.api.Tag("fast")
class ColonInFlowContextInListTest {
    @Test
    void withSpacesAround(TestInfo testInfo) {
        Load loader = new Load(new LoadSettingsBuilder().build());
        List<String> list = (List<String>) loader.loadFromString("[ http://foo ]");
        assertTrue(list.contains("http://foo"));
    }

    @Test
    void withoutSpacesAround(TestInfo testInfo) {
        Load loader = new Load(new LoadSettingsBuilder().build());
        List<String> list = (List<String>) loader.loadFromString("[http://foo]");
        assertTrue(list.contains("http://foo"));
    }

    @Test
    void twoValues(TestInfo testInfo) {
        Load loader = new Load(new LoadSettingsBuilder().build());
        List<String> list = (List<String>) loader.loadFromString("[ http://foo,http://bar ]");
        assertTrue(list.contains("http://foo"));
        assertTrue(list.contains("http://bar"));
    }
}


