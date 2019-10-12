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
package org.snakeyaml.engine.v2.api.lowlevel;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.events.*;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.utils.TestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@org.junit.jupiter.api.Tag("fast")
class SerializeTest {

    @Test
    void serializeOneScalar(TestInfo testInfo) {
        Serialize serialize = new Serialize(DumpSettings.builder().build());
        Iterable<Event> events = serialize.serializeOne(new ScalarNode(Tag.STR, "a", ScalarStyle.PLAIN));
        List<Event> list = Lists.newArrayList(events);
        assertEquals(5, list.size());
        TestUtils.compareEvents(Lists.newArrayList(new StreamStartEvent(),
                new DocumentStartEvent(false, Optional.empty(), new HashMap<>()),
                new ScalarEvent(Optional.empty(), Optional.empty(), new ImplicitTuple(false, false), "a", ScalarStyle.PLAIN),
                new DocumentEndEvent(false),
                new StreamEndEvent()), list);
    }
}
