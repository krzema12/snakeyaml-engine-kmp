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
package org.snakeyaml.engine.v2.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.NoSuchElementException;

import it.krzeminski.snakeyaml.engine.kmp.parser.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings;
import it.krzeminski.snakeyaml.engine.kmp.events.Event;
import it.krzeminski.snakeyaml.engine.kmp.scanner.ScannerImpl;
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader;

@org.junit.jupiter.api.Tag("fast")
class ParserTest {

  @Test
  @DisplayName("Expected NoSuchElementException after all the events are finished.")
  void testToString() {
    LoadSettings settings = LoadSettings.builder().build();
    StreamReader reader = new StreamReader(settings, "444333");
    ScannerImpl scanner = new ScannerImpl(settings, reader);
    Parser parser = new ParserImpl(settings, scanner);
    assertTrue(parser.hasNext());
    assertEquals(Event.ID.StreamStart, parser.next().getEventId());
    assertTrue(parser.hasNext());
    assertEquals(Event.ID.DocumentStart, parser.next().getEventId());
    assertTrue(parser.hasNext());
    assertEquals(Event.ID.Scalar, parser.next().getEventId());
    assertTrue(parser.hasNext());
    assertEquals(Event.ID.DocumentEnd, parser.next().getEventId());
    assertTrue(parser.hasNext());
    assertEquals(Event.ID.StreamEnd, parser.next().getEventId());
    assertFalse(parser.hasNext());
    try {
      parser.next();
      fail("Expected NoSuchElementException");
    } catch (NoSuchElementException e) {
      assertEquals("No more Events found.", e.getMessage());
    }
  }
}
