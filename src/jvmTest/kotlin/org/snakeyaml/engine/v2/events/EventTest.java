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
package org.snakeyaml.engine.v2.events;

import it.krzeminski.snakeyaml.engine.kmp.events.AliasEvent;
import it.krzeminski.snakeyaml.engine.kmp.events.Event;
import it.krzeminski.snakeyaml.engine.kmp.events.StreamStartEvent;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.common.Anchor;
import it.krzeminski.snakeyaml.engine.kmp.exceptions.Mark;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("fast")
class EventTest {

  @Test
  void testToString() {
    Event alias = new AliasEvent(new Anchor("111"));
    assertNotEquals(alias, alias.toString());
  }

  @Test
  void bothMarks() {
    Mark fake = new Mark("a", 0, 0, 0, Collections.emptyList(), 0);
    NullPointerException exception = assertThrows(NullPointerException.class,
        () -> new StreamStartEvent(null, fake));
    assertEquals("Both marks must be either present or absent.", exception.getMessage());
  }
}
