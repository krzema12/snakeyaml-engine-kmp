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
package org.snakeyaml.engine.v1.issues.issue1;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.v1.api.ConstructNode;
import org.snakeyaml.engine.v1.api.Load;
import org.snakeyaml.engine.v1.api.LoadSettings;
import org.snakeyaml.engine.v1.api.LoadSettingsBuilder;
import org.snakeyaml.engine.v1.nodes.Node;
import org.snakeyaml.engine.v1.nodes.Tag;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@org.junit.jupiter.api.Tag("fast")
class NullConstructorTest {

    @Test
    void customConstructorMustBeCalled(TestInfo testInfo) {
        Map<Tag, ConstructNode> tagConstructors = new HashMap<>();
        tagConstructors.put(Tag.NULL, new MyConstructNull());
        LoadSettings settings = new LoadSettingsBuilder().setTagConstructors(tagConstructors).build();
        Load loader = new Load(settings);
        assertNotNull(loader.loadFromString(""), "Expected MyConstructNull to be called.");
    }

    private class MyConstructNull implements ConstructNode {
        @Override
        public Object construct(Node node) {
            return 1;
        }
    }
}
