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
package org.snakeyaml.engine.representer;

import com.google.common.collect.TreeRangeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.exceptions.YamlEngineException;
import org.snakeyaml.engine.nodes.Node;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("fast")
class StandardRepresenterTest {
    private StandardRepresenter standardRepresenter = new StandardRepresenter();

    @Test
    @DisplayName("Represent UUID as node with global tag")
    void representUUID(TestInfo testInfo) {
        Node node = standardRepresenter.represent(UUID.fromString("37e6a9fa-52d3-11e8-9c2d-fa7ae01bbebc"));
        assertEquals("tag:yaml.org,2002:java.util.UUID", node.getTag().getValue());
    }

    @Test
    @DisplayName("Represent unknown class")
    void representUnknownClass(TestInfo testInfo) {
        YamlEngineException exception = assertThrows(YamlEngineException.class, () ->
                standardRepresenter.represent(TreeRangeSet.create()));
        assertEquals("Representer is not defined.", exception.getMessage());
    }

}
