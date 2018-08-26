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
package org.snakeyaml.engine.api.lowlevel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.api.DumpSettingsBuilder;
import org.snakeyaml.engine.common.ScalarStyle;
import org.snakeyaml.engine.events.DocumentEndEvent;
import org.snakeyaml.engine.events.DocumentStartEvent;
import org.snakeyaml.engine.events.Event;
import org.snakeyaml.engine.events.ImplicitTuple;
import org.snakeyaml.engine.events.ScalarEvent;
import org.snakeyaml.engine.events.StreamEndEvent;
import org.snakeyaml.engine.events.StreamStartEvent;
import org.snakeyaml.engine.nodes.ScalarNode;
import org.snakeyaml.engine.nodes.Tag;

import com.google.common.collect.Lists;

@org.junit.jupiter.api.Tag("fast")
class SerializeTest {

    @Test
    void serializeOneScalar(TestInfo testInfo) throws IOException {
        Serialize serialize = new Serialize(new DumpSettingsBuilder().build());
        Iterable<Event> events = serialize.serializeOne(new ScalarNode(Tag.STR, "a", ScalarStyle.PLAIN));
        List<Event> list = Lists.newArrayList(events);
        assertEquals(Lists.newArrayList(new StreamStartEvent(),
                new DocumentStartEvent(false, Optional.empty(), new HashMap<>()),
                new ScalarEvent(Optional.empty(), Optional.empty(), new ImplicitTuple(false, false), "a", ScalarStyle.PLAIN),
                new DocumentEndEvent(false),
                new StreamEndEvent()), list);
    }
}
