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
package org.snakeyaml.engine.issues.issue39;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.StreamDataWriter;
import org.snakeyaml.engine.v2.emitter.Emitter;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.events.ScalarEvent;
import org.snakeyaml.engine.v2.parser.Parser;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;
import org.snakeyaml.engine.v2.utils.TestUtils;

@org.junit.jupiter.api.Tag("fast")
public class EmitCommentAndSpacesTest {

  @Test
  @DisplayName("Issue 39: extra space added")
  void emitCommentWithEvent() {
    LoadSettings loadSettings = LoadSettings.builder().setParseComments(true).build();
    String input = TestUtils.getResource("/issues/issue39-input.yaml");
    Parser parser = new ParserImpl(loadSettings, new StreamReader(loadSettings, input));
    DumpSettings settings = DumpSettings.builder().setDumpComments(true).build();
    StreamDataWriter writer = new StreamToStringWriter();
    Emitter emitter = new Emitter(settings, writer);
    while (parser.hasNext()) {
      Event event = parser.next();
      emitter.emit(event);
    }
    assertNotEquals(input, writer.toString());
  }

  @Test
  @DisplayName("Issue 39: extra space added - small example")
  void emitCommentWithEventSmall() {
    LoadSettings loadSettings = LoadSettings.builder().setParseComments(true).build();
    String input = "first:\n  second: abc\n  \n  \n\n";
    Parser parser = new ParserImpl(loadSettings, new StreamReader(loadSettings, input));
    DumpSettings settings = DumpSettings.builder().setDumpComments(true).build();
    StreamDataWriter writer = new StreamToStringWriter();
    Emitter emitter = new Emitter(settings, writer);
    List<Event> events = new ArrayList<Event>();
    while (parser.hasNext()) {
      Event event = parser.next();
      events.add(event);
      emitter.emit(event);
    }
    assertEquals(14, events.size());
    assertEquals("abc", ((ScalarEvent) events.get(6)).getValue());
    // assertEquals(input, writer.toString());
  }
}


class StreamToStringWriter extends StringWriter implements StreamDataWriter {

}
