/*
 * Copyright (c) 2018, SnakeYAML
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.snakeyaml.engine.v2.api.lowlevel;

import com.google.common.collect.Lists;
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Serialize;
import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings;
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle;
import it.krzeminski.snakeyaml.engine.kmp.events.*;
import it.krzeminski.snakeyaml.engine.kmp.nodes.ScalarNode;
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag;
import org.snakeyaml.engine.v2.utils.TestUtils;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@org.junit.jupiter.api.Tag("fast")
class SerializeTest {

  @Test
  void serializeOneScalar() {
    Serialize serialize = new Serialize(DumpSettings.builder().build());
    Iterable<Event> events =
        serialize.serializeOne(new ScalarNode(Tag.STR, "a", ScalarStyle.PLAIN));
    List<Event> list = Lists.newArrayList(events);
    assertEquals(5, list.size());
    TestUtils
        .compareEvents(
            Lists.newArrayList(new StreamStartEvent(),
                new DocumentStartEvent(false, null, new HashMap<>()),
                new ScalarEvent(null, null, new ImplicitTuple(false, false),
                    "a", ScalarStyle.PLAIN),
                new DocumentEndEvent(false), new StreamEndEvent()),
            list);
  }
}
