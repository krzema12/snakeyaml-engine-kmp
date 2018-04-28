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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.api.Compose;
import org.snakeyaml.engine.api.DumpSettings;
import org.snakeyaml.engine.api.LoadSettings;
import org.snakeyaml.engine.api.Serialize;
import org.snakeyaml.engine.events.Event;
import org.snakeyaml.engine.exceptions.YamlEngineException;
import org.snakeyaml.engine.nodes.Node;

import com.google.common.collect.Lists;

@org.junit.jupiter.api.Tag("fast")
class ComposeSuiteTest {
    public static final List<String> emptyNodes = Lists.newArrayList("AVM7", "8G76", "98YD");

    private List<SuiteData> allValid = SuiteUtils.getAll().stream()
            .filter(data -> !data.getError())
            .filter(data -> !ParseSuiteTest.deviationsWithSuccess.contains(data.getName()))
            .filter(data -> !ParseSuiteTest.deviationsWithError.contains(data.getName()))
            .collect(Collectors.toList());

    private List<SuiteData> allValidAndNonEmpty = allValid.stream()
            .filter(data -> !emptyNodes.contains(data.getName()))
            .collect(Collectors.toList());

    private List<SuiteData> allValidAndEmpty = allValid.stream()
            .filter(data -> emptyNodes.contains(data.getName()))
            .collect(Collectors.toList());


    public static ComposeResult composeData(SuiteData data) {
        Optional<Exception> error = Optional.empty();
        List<Node> list = new ArrayList();

        try {
            LoadSettings settings = new LoadSettings();
            settings.setLabel(data.getLabel());
            Iterable<Node> iterable = new Compose(settings).composeAllFromString(data.getInput());
            iterable.forEach(event -> list.add(event));
        } catch (YamlEngineException e) {
            error = Optional.of(e);
        }
        return new ComposeResult(list, error);
    }

    @Test
    @DisplayName("Compose: run one test")
    void runOne(TestInfo testInfo) {
        SuiteData data = SuiteUtils.getOne("C4HZ");
        LoadSettings settings = new LoadSettings();
        settings.setLabel(data.getLabel());
        Optional<Node> node = new Compose(settings).composeString(data.getInput());
        assertTrue(node.isPresent());
//        System.out.println(node);
    }

    @Test
    @DisplayName("Compose: Run comprehensive test suite for non empty Nodes")
    void runAllNonEmpty(TestInfo testInfo) {
        for (SuiteData data : allValidAndNonEmpty) {
            ComposeResult result = composeData(data);
            List<Node> nodes = result.getNode();
            assertFalse(nodes.isEmpty(), data.getName() + " -> " + data.getLabel() + "\n" + data.getInput());
            //TODO
            final List<String> tofix = Lists.newArrayList(
                    "K4SU", "X38W", "6CK3", "8MK2", "J7VC", "7BMT", "735Y",
                    "96L6", "4Q9F", "Z9M4", "V55R", "4V8U", "229Q", "9U5K", "SYW4",
                    "6ZKB", "DHP8", "JS2J", "6WPF", "JHB9", "9DXL", "B3HG", "6LVF",
                    "5WE3", "S4T7", "5TYM", "CC74", "H3Z8", "2LFX", "KSS4", "Q9WF",
                    "RTP8", "6SLA", "M7NX", "77H8", "RZT7", "6BFJ", "35KP", "H2RW",
                    "SKE5", "CN3R", "MYW6", "F2C7", "93WF", "T4YY", "J9HZ", "SSW6",
                    "BU8L", "EXG3", "6JWB", "KMK3", "RZP5", "753E", "S4JQ", "C2DT",
                    "XLQ9", "9TFX", "U3XV", "HMQ5", "6JQW", "PUW8", "27NA", "DFF7",
                    "L94M", "9WXW", "3GZX", "LE5A", "RLU9", "EX5H", "PW8X", "ZF4X",
                    "8CWC", "74H7", "T26H", "U3C3", "T5N4", "RR7F", "P76L", "UGM3",
                    "FH7J", "26DV", "Q8AD", "J7PZ", "ZWK4", "ZH7C", "AZ63", "JTV5",
                    "XW4D", "BEC7", "6FWR", "36F6", "7W2P", "F3CP", "2AUY", "7FWL",
                    "CUP7", "U9NS", "YD5X", "57H4", "JDH8", "EHF6", "7TMG", "9KAX",
                    "6WLZ", "52DL", "2XXW", "GH63", "7BUB", "W42U", "M29M", "C4HZ", "E76Z");
            if (tofix.contains(data.getName())) continue;
            DumpSettings settings = new DumpSettings();
            settings.setExplicitStart(true);
            settings.setExplicitEnd(true);
            Serialize serialize = new Serialize(settings);
            List<Event> events = serialize.serializeAll(nodes);
            List<String> outEvents = events.stream().map(e -> e.toString()).collect(Collectors.toList());
            assertEquals(data.getEvents().size(), events.size(), data.getName() + " -> " + data.getLabel() + "\n" + data.getInput());
            //TODO assertEquals(data.getEvents(), outEvents, data.getName() + " -> " + data.getLabel() + "\n" + data.getInput());
        }
    }

    @Test
    @DisplayName("Compose: Run comprehensive test suite for empty Nodes")
    void runAllEmpty(TestInfo testInfo) {
        for (SuiteData data : allValidAndEmpty) {
            ComposeResult result = composeData(data);
            List<Node> nodes = result.getNode();
            assertTrue(nodes.isEmpty(), data.getName() + " -> " + data.getLabel() + "\n" + data.getInput());

        }
    }
}

class ComposeResult {

    private final List<Node> node;
    private final Optional<Exception> error;

    public ComposeResult(List<Node> node, Optional<Exception> error) {
        this.node = node;
        this.error = error;
    }

    public List<Node> getNode() {
        return node;
    }

    public Optional<Exception> getError() {
        return error;
    }
}

