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
package org.snakeyaml.engine.v1.api.lowlevel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.v1.api.LoadSettingsBuilder;
import org.snakeyaml.engine.v1.events.Event;
import org.snakeyaml.engine.v1.events.StreamEndEvent;
import org.snakeyaml.engine.v1.events.StreamStartEvent;

import com.google.common.collect.Lists;
import com.google.common.io.CharSource;

@Tag("fast")
class ParseTest {

    @Test
    void parseEmptyReader(TestInfo testInfo) throws IOException {
        Parse parse = new Parse(new LoadSettingsBuilder().build());
        Iterable<Event> events = parse.parseReader(CharSource.wrap("").openStream());
        List<Event> list = Lists.newArrayList(events);
        assertEquals(Lists.newArrayList(new StreamStartEvent(), new StreamEndEvent()), list);
    }

    @Test
    void parseEmptyInputStream(TestInfo testInfo) {
        Parse parse = new Parse(new LoadSettingsBuilder().build());
        Iterable<Event> events = parse.parseInputStream(new ByteArrayInputStream("".getBytes()));
        List<Event> list = Lists.newArrayList(events);
        assertEquals(Lists.newArrayList(new StreamStartEvent(), new StreamEndEvent()), list);
    }
}
