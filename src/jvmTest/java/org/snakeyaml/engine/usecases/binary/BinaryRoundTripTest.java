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
package org.snakeyaml.engine.usecases.binary;

import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.api.Dump;
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings;
import it.krzeminski.snakeyaml.engine.kmp.api.Load;
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Serialize;
import it.krzeminski.snakeyaml.engine.kmp.common.NonPrintableStyle;
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle;
import it.krzeminski.snakeyaml.engine.kmp.events.Event;
import it.krzeminski.snakeyaml.engine.kmp.events.ImplicitTuple;
import it.krzeminski.snakeyaml.engine.kmp.events.ScalarEvent;
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node;
import it.krzeminski.snakeyaml.engine.kmp.nodes.NodeType;
import it.krzeminski.snakeyaml.engine.kmp.nodes.ScalarNode;
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag;
import it.krzeminski.snakeyaml.engine.kmp.representer.StandardRepresenter;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@org.junit.jupiter.api.Tag("fast")
public class BinaryRoundTripTest {

  @Test
  public void testBinary() {
    Dump dumper =
        new Dump(DumpSettings.builder().setNonPrintableStyle(NonPrintableStyle.BINARY).build());
    String source = "\u0096";
    String serialized = dumper.dumpToString(source);
    assertEquals("!!binary |-\n" + "  wpY=\n", serialized);
    // parse back to bytes
    Load loader = new Load();
    byte[] deserialized = (byte[]) loader.loadOne(serialized);
    assertEquals(source, new String(deserialized, StandardCharsets.UTF_8));
  }

  @Test
  public void testBinaryNode() {
    String source = "\u0096";
    StandardRepresenter standardRepresenter = new StandardRepresenter(
        DumpSettings.builder().setNonPrintableStyle(NonPrintableStyle.BINARY).build());
    ScalarNode scalar = (ScalarNode) standardRepresenter.represent(source);
    // check Node
    assertEquals(Tag.BINARY, scalar.getTag());
    assertEquals(NodeType.SCALAR, scalar.getNodeType());
    assertEquals("wpY=", scalar.getValue());
    // check Event
    Serialize serialize = new Serialize(DumpSettings.builder().build());
    Iterable<Event> eventsIter = serialize.serializeOne(scalar);
    List<Event> events = ((List<Event>) eventsIter).subList(0, ((List<Event>) eventsIter).size());
    assertEquals(5, events.size());
    ScalarEvent data = (ScalarEvent) events.get(2);
    assertEquals(Tag.BINARY.toString(), data.getTag());
    assertEquals(ScalarStyle.LITERAL, data.getScalarStyle());
    assertEquals("wpY=", data.getValue());
    ImplicitTuple implicit = data.getImplicit();
    assertFalse(implicit.canOmitTagInPlainScalar());
    assertFalse(implicit.canOmitTagInNonPlainScalar());
  }

  @Test
  public void testStrNode() {
    StandardRepresenter standardRepresenter =
        new StandardRepresenter(DumpSettings.builder().build());
    String source = "\u0096";
    ScalarNode scalar = (ScalarNode) standardRepresenter.represent(source);
    Node node = standardRepresenter.represent(source);
    assertEquals(Tag.STR, node.getTag());
    assertEquals(NodeType.SCALAR, node.getNodeType());
    assertEquals("\u0096", scalar.getValue());
  }

  @Test
  public void testRoundTripBinary() {
    Dump dumper =
        new Dump(DumpSettings.builder().setNonPrintableStyle(NonPrintableStyle.ESCAPE).build());
    Map<String, String> toSerialized = new HashMap<>();
    toSerialized.put("key", "a\u0096b");
    String output = dumper.dumpToString(toSerialized);
    assertEquals("{key: \"a\\x96b\"}\n", output);
    Load loader = new Load();
    Map<String, String> parsed = (Map<String, String>) loader.loadOne(output);
    assertEquals(toSerialized.get("key"), parsed.get("key"));
    assertEquals(toSerialized, parsed);
  }
}
