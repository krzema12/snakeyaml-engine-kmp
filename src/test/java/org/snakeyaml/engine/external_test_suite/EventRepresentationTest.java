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
package org.snakeyaml.engine.external_test_suite;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.common.Anchor;
import org.snakeyaml.engine.common.FlowStyle;
import org.snakeyaml.engine.common.ScalarStyle;
import org.snakeyaml.engine.events.AliasEvent;
import org.snakeyaml.engine.events.DocumentEndEvent;
import org.snakeyaml.engine.events.DocumentStartEvent;
import org.snakeyaml.engine.events.Event;
import org.snakeyaml.engine.events.ImplicitTuple;
import org.snakeyaml.engine.events.MappingStartEvent;
import org.snakeyaml.engine.events.ScalarEvent;
import org.snakeyaml.engine.events.SequenceEndEvent;
import org.snakeyaml.engine.events.SequenceStartEvent;
import org.snakeyaml.engine.events.StreamEndEvent;
import org.snakeyaml.engine.events.StreamStartEvent;

@org.junit.jupiter.api.Tag("fast")
class EventRepresentationTest {
    @Test
    @DisplayName("Represent StreamStartEvent")
    void testStreamStartEvent(TestInfo testInfo) {
        StreamStartEvent event = new StreamStartEvent();
        EventRepresentation representation = new EventRepresentation(event);
        assertTrue(representation.isSameAs("+STR"));
        assertFalse(representation.isSameAs("-STR"));
        assertFalse(representation.isSameAs("=VAL"));
    }

    @Test
    @DisplayName("Represent StreamEndEvent")
    void testStreamEndEvent(TestInfo testInfo) {
        StreamEndEvent event = new StreamEndEvent();
        EventRepresentation representation = new EventRepresentation(event);
        assertTrue(representation.isSameAs("-STR"));
        assertFalse(representation.isSameAs("+STR"));
    }

    @Test
    @DisplayName("Represent AliasEvent")
    void testAliasEvent(TestInfo testInfo) {
        AliasEvent event = new AliasEvent(Optional.of(new Anchor("a")));
        EventRepresentation representation = new EventRepresentation(event);
        assertTrue(representation.isSameAs("=ALI *a"));
        assertTrue(representation.isSameAs("=ALI *b"));
        assertTrue(representation.isSameAs("=ALI *002"));
        assertFalse(representation.isSameAs("=ALI &002"));
        assertFalse(representation.isSameAs("+STR"));
    }

    @Test
    @DisplayName("Represent DocumentStartEvent")
    void testDocumentStartEvent(TestInfo testInfo) {
        valid(new DocumentStartEvent(true, Optional.empty(), null), "+DOC ---");
        valid(new DocumentStartEvent(true, Optional.empty(), null), "+DOC");

        valid(new DocumentStartEvent(false, Optional.empty(), null), "+DOC");
        valid(new DocumentStartEvent(false, Optional.empty(), null), "+DOC ---");
    }

    @Test
    @DisplayName("Represent DocumentEndEvent")
    void testDocumentEndEvent(TestInfo testInfo) {
        valid(new DocumentEndEvent(true), "-DOC ...");
        valid(new DocumentEndEvent(true), "-DOC");
        invalid(new DocumentEndEvent(true), "+DOC ---");
    }

    @Test
    @DisplayName("Represent SequenceStartEvent")
    void testSequenceStartEvent(TestInfo testInfo) {
        valid(new SequenceStartEvent(Optional.of(new Anchor("a")), Optional.of("ttt"), false, FlowStyle.FLOW),
                "+SEQ &a <ttt>");
        invalid(new SequenceStartEvent(Optional.of(new Anchor("a")), Optional.of("ttt"), false, FlowStyle.FLOW),
                "+SEQ *a <ttt>");
        invalid(new SequenceStartEvent(Optional.of(new Anchor("a")), Optional.of("ttt"), false, FlowStyle.FLOW),
                "+SEQ &a <t>");
        invalid(new SequenceStartEvent(Optional.of(new Anchor("a")), Optional.of("ttt"), false, FlowStyle.FLOW),
                "+SEQ <ttt>");
        invalid(new SequenceStartEvent(Optional.of(new Anchor("a")), Optional.of("ttt"), false, FlowStyle.FLOW),
                "+SEQ *a");
    }

    @Test
    @DisplayName("Represent SequenceEndEvent")
    void testSequenceEndEvent(TestInfo testInfo) {
        valid(new SequenceEndEvent(), "-SEQ");
        invalid(new SequenceEndEvent(), "-MAP");
    }

    @Test
    @DisplayName("Represent ScalarEvent")
    void testScalarEvent(TestInfo testInfo) {
        valid(new ScalarEvent(Optional.of(new Anchor("a")), Optional.of("ttt"), new ImplicitTuple(false, false), "v1", ScalarStyle.FOLDED),
                "=VAL &a <ttt> >v1");

        invalid(new ScalarEvent(Optional.of(new Anchor("a")), Optional.of("ttt"), new ImplicitTuple(false, false), "v1", ScalarStyle.PLAIN),
                "=VAL <ttt> >v1");
        invalid(new ScalarEvent(Optional.of(new Anchor("a")), Optional.of("ttt"), new ImplicitTuple(false, false), "v1", ScalarStyle.PLAIN),
                "=VAL &a >v1");
        invalid(new ScalarEvent(Optional.of(new Anchor("a")), Optional.of("ttt"), new ImplicitTuple(false, false), "v1", ScalarStyle.PLAIN),
                "=VAL &a <ttt>");
        invalid(new ScalarEvent(Optional.of(new Anchor("a")), Optional.of("ttt"), new ImplicitTuple(false, false), "v1", ScalarStyle.PLAIN),
                "=VAL &a <ttt> |v1");
    }

    @Test
    @DisplayName("Represent MappingStartEvent")
    void testMappingStartEvent(TestInfo testInfo) {
        invalid(new MappingStartEvent(Optional.of(new Anchor("a")), Optional.of("ttt"), false, FlowStyle.FLOW),
                "+MAP");
        valid(new MappingStartEvent(Optional.empty(), Optional.of(org.snakeyaml.engine.nodes.Tag.MAP.getValue()), false, FlowStyle.FLOW),
                "+MAP");
        valid(new MappingStartEvent(Optional.empty(), Optional.empty(), false, FlowStyle.FLOW),
                "+MAP");
    }

    private void valid(Event event, String expectation) {
        EventRepresentation representation = new EventRepresentation(event);
        assertTrue(representation.isSameAs(expectation));
    }

    private void invalid(Event event, String expectation) {
        EventRepresentation representation = new EventRepresentation(event);
        assertFalse(representation.isSameAs(expectation));
    }


}
