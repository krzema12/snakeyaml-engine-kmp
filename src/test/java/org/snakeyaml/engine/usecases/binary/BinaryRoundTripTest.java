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
package org.snakeyaml.engine.usecases.binary;

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v1.api.Dump;
import org.snakeyaml.engine.v1.api.DumpSettingsBuilder;
import org.snakeyaml.engine.v1.api.Load;
import org.snakeyaml.engine.v1.api.LoadSettingsBuilder;
import org.snakeyaml.engine.v1.api.lowlevel.Serialize;
import org.snakeyaml.engine.v1.common.ScalarStyle;
import org.snakeyaml.engine.v1.events.Event;
import org.snakeyaml.engine.v1.events.ImplicitTuple;
import org.snakeyaml.engine.v1.events.ScalarEvent;
import org.snakeyaml.engine.v1.nodes.Node;
import org.snakeyaml.engine.v1.nodes.NodeType;
import org.snakeyaml.engine.v1.nodes.ScalarNode;
import org.snakeyaml.engine.v1.nodes.Tag;
import org.snakeyaml.engine.v1.representer.StandardRepresenter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@org.junit.jupiter.api.Tag("fast")
public class BinaryRoundTripTest {

    @Test
    public void testBinary() {
        Dump dumper = new Dump(new DumpSettingsBuilder().setConvertNonPrintableToBinary(true).build());
        String source = "\u0096";
        String serialized = dumper.dumpToString(source);
        assertEquals("!!binary |-\n" +
                "  wpY=\n", serialized);
        //parse back to bytes
        Load loader = new Load(new LoadSettingsBuilder().build());
        byte[] deserialized = (byte[]) loader.loadFromString(serialized);
        assertEquals(source, new String(deserialized));
    }

    @Test
    public void testBinaryNode() {
        String source = "\u0096";
        StandardRepresenter standardRepresenter = new StandardRepresenter(new DumpSettingsBuilder().setConvertNonPrintableToBinary(true).build());
        ScalarNode scalar = (ScalarNode) standardRepresenter.represent(source);
        //check Node
        assertEquals(org.snakeyaml.engine.v1.nodes.Tag.BINARY, scalar.getTag());
        assertEquals(NodeType.SCALAR, scalar.getNodeType());
        assertEquals("wpY=", scalar.getValue());
        //check Event
        Serialize serialize = new Serialize(new DumpSettingsBuilder().build());
        Iterable<Event> eventsIter = serialize.serializeOne(scalar);
        List<Event> events = ((List<Event>) eventsIter).subList(0, ((List<Event>) eventsIter).size());
        assertEquals(5, events.size());
        ScalarEvent data = (ScalarEvent) events.get(2);
        assertEquals(Tag.BINARY.toString(), data.getTag().get());
        assertEquals(ScalarStyle.LITERAL, data.getScalarStyle());
        assertEquals("wpY=", data.getValue());
        ImplicitTuple implicit = data.getImplicit();
        assertFalse(implicit.canOmitTagInPlainScalar());
        assertFalse(implicit.canOmitTagInNonPlainScalar());
    }

    @Test
    public void testStrNode() {
        StandardRepresenter standardRepresenter = new StandardRepresenter(new DumpSettingsBuilder().build());
        String source = "\u0096";
        ScalarNode scalar = (ScalarNode) standardRepresenter.represent(source);
        Node node = standardRepresenter.represent(source);
        assertEquals(Tag.STR, node.getTag());
        assertEquals(NodeType.SCALAR, node.getNodeType());
        assertEquals("\u0096", scalar.getValue());
    }

    @Test
    public void testRoundTripBinary() {
        Dump dumper = new Dump(new DumpSettingsBuilder().setConvertNonPrintableToBinary(false).build());
        Map<String, String> toSerialized = new HashMap<>();
        toSerialized.put("key", "a\u0096b");
        String output = dumper.dumpToString(toSerialized);
        assertEquals("{key: \"a\\x96b\"}\n", output);
        Load loader = new Load(new LoadSettingsBuilder().build());
        Map<String, String> parsed = (Map<String, String>) loader.loadFromString(output);
        assertEquals(toSerialized.get("key"), parsed.get("key"));
        assertEquals(toSerialized, parsed);
    }
}
