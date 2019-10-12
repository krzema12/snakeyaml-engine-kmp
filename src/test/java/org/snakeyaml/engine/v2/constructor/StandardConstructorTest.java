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
package org.snakeyaml.engine.v2.constructor;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.lowlevel.Compose;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.utils.TestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("fast")
class StandardConstructorTest {

    @Test
    void constructMergeExample() {
        Compose compose = new Compose(LoadSettings.builder().build());
        Optional<Node> node = compose.composeString(TestUtils.getResource("merge/example.yaml"));
        StandardConstructor constructor = new StandardConstructor(LoadSettings.builder().build());
        Object object = constructor.construct(node.get());
        assertNotNull(object);
    }
}
