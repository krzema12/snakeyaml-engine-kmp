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
package org.snakeyaml.engine.issues.issue36;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.StreamDataWriter;
import org.snakeyaml.engine.v2.comments.CommentType;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.emitter.Emitter;
import org.snakeyaml.engine.v2.events.*;

import java.io.StringWriter;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@org.junit.jupiter.api.Tag("fast")
public class EmitCommentTest {

  @Test
  @DisplayName("Issue 36: comment with scalar should not be ignored")
  void emitCommentWithEvent() {
    DumpSettings settings = DumpSettings.builder().setDumpComments(true).build();
    StreamDataWriter writer = new StreamToStringWriter();
    Emitter emitter = new Emitter(settings, writer);
    emitter.emit(new StreamStartEvent());
    emitter.emit(new DocumentStartEvent(false, null, new HashMap<>()));
    emitter.emit(
        new CommentEvent(CommentType.BLOCK, "Hello world!", null, null));
    emitter.emit(new ScalarEvent(null, null, new ImplicitTuple(true, true),
        "This is the scalar", ScalarStyle.DOUBLE_QUOTED));
    emitter.emit(new DocumentEndEvent(false));
    emitter.emit(new StreamEndEvent());

    assertEquals("#Hello world!\n" + "\"This is the scalar\"\n", writer.toString());
  }

  @Test
  @DisplayName("Issue 36: only comment should not be ignored")
  void emitComment() {
    DumpSettings settings = DumpSettings.builder().setDumpComments(true).build();
    StreamDataWriter writer = new StreamToStringWriter();
    Emitter emitter = new Emitter(settings, writer);
    emitter.emit(new StreamStartEvent());
    emitter.emit(new DocumentStartEvent(false, null, new HashMap<>()));
    emitter.emit(
        new CommentEvent(CommentType.BLOCK, "Hello world!", null, null));
    emitter.emit(new DocumentEndEvent(false));
    emitter.emit(new StreamEndEvent());

    assertEquals("#Hello world!\n", writer.toString());
  }
}


class StreamToStringWriter extends StringWriter implements StreamDataWriter {

}
