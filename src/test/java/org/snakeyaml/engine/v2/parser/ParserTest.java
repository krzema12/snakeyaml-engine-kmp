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
package org.snakeyaml.engine.v2.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.scanner.ScannerImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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

