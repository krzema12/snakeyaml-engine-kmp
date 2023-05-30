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
package org.snakeyaml.engine.issues.issue544;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.lowlevel.Present;
import org.snakeyaml.engine.v2.api.lowlevel.Serialize;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.Tag;

public class DoubleQuoteTest {

  private MappingNode create() {
    String content = "🔐This process is simple and secure.";

    ScalarNode doubleQuotedKey = new ScalarNode(Tag.STR, "double_quoted", ScalarStyle.PLAIN);
    ScalarNode doubleQuotedValue = new ScalarNode(Tag.STR, content, ScalarStyle.DOUBLE_QUOTED);
    NodeTuple doubleQuotedTuple = new NodeTuple(doubleQuotedKey, doubleQuotedValue);

    ScalarNode singleQuotedKey = new ScalarNode(Tag.STR, "single_quoted", ScalarStyle.PLAIN);
    ScalarNode singleQuotedValue = new ScalarNode(Tag.STR, content, ScalarStyle.SINGLE_QUOTED);
    NodeTuple singleQuotedTuple = new NodeTuple(singleQuotedKey, singleQuotedValue);

    List<NodeTuple> nodeTuples = new ArrayList<>();
    nodeTuples.add(doubleQuotedTuple);
    nodeTuples.add(singleQuotedTuple);

    MappingNode mappingNode = new MappingNode(Tag.MAP, nodeTuples, FlowStyle.BLOCK);
    return mappingNode;
  }

  private String emit(DumpSettings settings) {
    Serialize serialize = new Serialize(settings);
    Iterable<Event> eventsIter = serialize.serializeOne(create());
    Present emit = new Present(settings);
    return emit.emitToString(eventsIter.iterator());
  }

  @Test
  public void testSubstitution() {
    DumpSettings settings = DumpSettings.builder().setUseUnicodeEncoding(false).build();
    String expectedOutput = "double_quoted: \"\\U0001f510This process is simple and secure.\"\n"
        + "single_quoted: \"\\U0001f510This process is simple and secure.\"\n";
    assertEquals(expectedOutput, emit(settings));
  }

  @Test
  public void testUnicode() {
    DumpSettings settings = DumpSettings.builder().setUseUnicodeEncoding(true).build();
    String expectedOutput = "double_quoted: \"🔐This process is simple and secure.\"\n"
        + "single_quoted: '🔐This process is simple and secure.'\n";
    assertEquals(expectedOutput, emit(settings));
  }

  @Test
  public void testDefault() {
    DumpSettings settings = DumpSettings.builder().build();
    String expectedOutput = "double_quoted: \"🔐This process is simple and secure.\"\n"
        + "single_quoted: '🔐This process is simple and secure.'\n";
    assertEquals(expectedOutput, emit(settings));
  }
}
