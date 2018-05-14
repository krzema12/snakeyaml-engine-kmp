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
package org.snakeyaml.engine.constructor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.api.LoadSettings;
import org.snakeyaml.engine.api.lowlevel.Compose;
import org.snakeyaml.engine.nodes.Node;
import org.snakeyaml.engine.utils.TestUtils;

@Tag("fast")
class StandardConstructorTest {

    @Test
    void constructMergeExample(TestInfo testInfo) {
        Compose compose = new Compose(new LoadSettings());
        Optional<Node> node = compose.composeString(TestUtils.getResource("merge/example.yaml"));
        StandardConstructor constructor = new StandardConstructor(new LoadSettings());
        Object object = constructor.construct(node.get());
        assertNotNull(object);
    }
}
