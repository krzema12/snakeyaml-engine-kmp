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
import org.snakeyaml.engine.api.DumpSettings;
import org.snakeyaml.engine.api.DumpSettingsBuilder;
import org.snakeyaml.engine.api.LoadSettings;
import org.snakeyaml.engine.api.lowlevel.Compose;
import org.snakeyaml.engine.api.lowlevel.Serialize;
import org.snakeyaml.engine.events.Event;
import org.snakeyaml.engine.exceptions.YamlEngineException;
import org.snakeyaml.engine.nodes.Node;

import com.google.common.collect.Lists;

@org.junit.jupiter.api.Tag("fast")
class ComposeSuiteTest {
    public static final List<String> emptyNodes = Lists.newArrayList("AVM7", "8G76", "98YD");

    private List<SuiteData> allValid = SuiteUtils.getAll().stream()
            .filter(data -> !data.getError())
            .filter(data -> !SuiteUtils.deviationsWithSuccess.contains(data.getName()))
            .filter(data -> !SuiteUtils.deviationsWithError.contains(data.getName()))
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
//            if (!"C4HZ".equals(data.getName())) continue;
            ComposeResult result = composeData(data);
            List<Node> nodes = result.getNode();
            assertFalse(nodes.isEmpty(), data.getName() + " -> " + data.getLabel() + "\n" + data.getInput());
            DumpSettings settings = new DumpSettingsBuilder().setExplicitStart(true).setExplicitEnd(true).build();
            Serialize serialize = new Serialize(settings);
            List<Event> events = serialize.serializeAll(nodes);
            assertEquals(data.getEvents().size(), events.size(), data.getName() + " -> " + data.getLabel() + "\n" + data.getInput());
            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                EventRepresentation representation = new EventRepresentation(event);
                String etalon = data.getEvents().get(i);
                assertTrue(representation.isSameAs(etalon),
                        data.getName() + " -> " + data.getLabel() + "\n" +
                                data.getInput() + "\n" + data.getEvents().get(i) + "\n" + events.get(i) + "\n");
            }
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

