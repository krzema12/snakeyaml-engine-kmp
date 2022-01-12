/*
 * Copyright (c) 2018, SnakeYAML
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snakeyaml.engine.v2.representer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.StreamDataWriter;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.comments.CommentType;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.emitter.Emitter;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.serializer.Serializer;

@Tag("fast")
public class RepresentEntryTest {

  private final DumpSettings settings = DumpSettings.builder()
      .setDefaultScalarStyle(ScalarStyle.PLAIN)
      .setDefaultFlowStyle(FlowStyle.BLOCK)
      .setDumpComments(true)
      .build();
  private final CommentedEntryRepresenter commentedEntryRepresenter = new CommentedEntryRepresenter(
      settings);

  private Map<String, String> createMap() {
    Map<String, String> map = new LinkedHashMap<>();
    map.put("a", "val1");
    return map;
  }

  @Test
  @DisplayName("Represent and dump mapping nodes using the new method")
  void representMapping() {
    StringOutputStream stringOutputStream = new StringOutputStream();

    Serializer serializer = new Serializer(settings, new Emitter(settings, stringOutputStream));
    serializer.emitStreamStart();
    serializer.serializeDocument(commentedEntryRepresenter.represent(createMap()));
    serializer.emitStreamEnd();

    assertEquals("#Key node block comment\n" +
        "a: val1 #Value node inline comment\n", stringOutputStream.toString());
  }

  private class CommentedEntryRepresenter extends StandardRepresenter {

    public CommentedEntryRepresenter(DumpSettings settings) {
      super(settings);
    }

    @Override
    protected NodeTuple representMappingEntry(Map.Entry<?, ?> entry) {
      NodeTuple tuple = super.representMappingEntry(entry);
      List<CommentLine> keyBlockComments = new ArrayList<>();
      keyBlockComments.add(
          new CommentLine(Optional.empty(), Optional.empty(), "Key node block comment",
              CommentType.BLOCK));
      tuple.getKeyNode().setBlockComments(keyBlockComments);

      List<CommentLine> valueEndComments = new ArrayList<>();
      valueEndComments.add(
          new CommentLine(Optional.empty(), Optional.empty(), "Value node inline comment",
              CommentType.IN_LINE));
      tuple.getValueNode().setEndComments(valueEndComments);

      return tuple;
    }
  }

  private class StringOutputStream extends StringWriter implements StreamDataWriter {

  }
}