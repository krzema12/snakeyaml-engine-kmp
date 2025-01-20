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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import com.google.common.io.CharSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Parse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings;
import it.krzeminski.snakeyaml.engine.kmp.events.Event;
import it.krzeminski.snakeyaml.engine.kmp.events.StreamEndEvent;
import it.krzeminski.snakeyaml.engine.kmp.events.StreamStartEvent;
import org.snakeyaml.engine.v2.utils.TestUtils;

@Tag("fast")
class ParseTest {

  @Test
  void parseEmptyReader() throws IOException {
    Parse parse = new Parse(LoadSettings.builder().build());
    Iterable<Event> events = parse.parse(CharSource.wrap("").openStream());
    List<Event> list = Lists.newArrayList(events);
    assertEquals(2, list.size());
    TestUtils.compareEvents(Lists.newArrayList(new StreamStartEvent(), new StreamEndEvent()), list);
  }

  @Test
  void parseEmptyInputStream() {
    Parse parse = new Parse(LoadSettings.builder().build());
    Iterable<Event> events = parse.parse(new ByteArrayInputStream("".getBytes()));
    List<Event> list = Lists.newArrayList(events);
    assertEquals(2, list.size());
    TestUtils.compareEvents(Lists.newArrayList(new StreamStartEvent(), new StreamEndEvent()), list);
  }
}
