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
package org.snakeyaml.engine.usecases.external_test_suite;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings;
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings;
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose;
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Serialize;
import it.krzeminski.snakeyaml.engine.kmp.events.Event;
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException;
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@org.junit.jupiter.api.Tag("fast")
class ComposeSuiteTest {

  public static final List<String> emptyNodes = Lists.newArrayList("AVM7", "8G76", "98YD");

  private final List<SuiteData> allValid =
      SuiteUtils.getAll().stream().filter(data -> !data.hasError())
          .filter(data -> !SuiteUtils.deviationsWithSuccess.contains(data.getName()))
          .filter(data -> !SuiteUtils.deviationsWithError.contains(data.getName()))
          // TODO FIXME JEF9-02 is not according to the spec
          .filter(data -> !data.getName().equals("JEF9-02")).collect(Collectors.toList());

  private final List<SuiteData> allValidAndNonEmpty = allValid.stream()
      .filter(data -> !emptyNodes.contains(data.getName())).collect(Collectors.toList());

  private final List<SuiteData> allValidAndEmpty = allValid.stream()
      .filter(data -> emptyNodes.contains(data.getName())).collect(Collectors.toList());


  public static ComposeResult composeData(SuiteData data) {
    Exception error = null;
    List<Node> list = new ArrayList();
    try {
      LoadSettings settings = LoadSettings.builder().setLabel(data.getLabel()).build();
      Iterable<Node> iterable = new Compose(settings).composeAllFromString(data.getInput());
      iterable.forEach(event -> list.add(event));
    } catch (YamlEngineException e) {
      error = e;
    }
    return new ComposeResult(list, error);
  }

  @Test
  @DisplayName("Compose: run one test")
  void runOne() {
    SuiteData data = SuiteUtils.getOne("C4HZ");
    LoadSettings settings = LoadSettings.builder().setLabel(data.getLabel()).build();
    Node node = new Compose(settings).composeString(data.getInput());
    assertNotNull(node);
    // System.out.println(node);
  }

  @Test
  @DisplayName("Compose: Run comprehensive test suite for non empty Nodes")
  void runAllNonEmpty() {
    for (SuiteData data : allValidAndNonEmpty) {
      if ("4MUZ-01".equals(data.getName())) {
        continue; // TODO FIXME fix test
      }
      if ("UV7Q".equals(data.getName())) {
        continue; // TODO FIXME fix test
      }
      if ("HM87-00".equals(data.getName())) {
        continue; // TODO FIXME fix test
      }
      if ("M2N8-00".equals(data.getName())) {
        continue; // TODO FIXME fix test
      }
      if ("UKK6-00".equals(data.getName())) {
        continue; // TODO FIXME fix test
      }
      if ("MUS6-03".equals(data.getName())) {
        continue; // TODO FIXME fix test
      }
      if ("4Q9F".equals(data.getName())) {
        continue; // TODO FIXME fix test
      }
      ComposeResult result = composeData(data);
      List<Node> nodes = result.getNode();
      assertFalse(nodes.isEmpty(),
          data.getName() + " -> " + data.getLabel() + "\n" + data.getInput());
      DumpSettings settings =
          DumpSettings.builder().setExplicitStart(true).setExplicitEnd(true).build();
      Serialize serialize = new Serialize(settings);
      List<Event> events = serialize.serializeAll(nodes);
      assertEquals(data.getEvents().size(), events.size(),
          data.getName() + " -> " + data.getLabel() + "\n" + data.getInput());
      for (int i = 0; i < events.size(); i++) {
        Event event = events.get(i);
        EventRepresentation representation = new EventRepresentation(event);
        String expectation = data.getEvents().get(i);
        boolean theSame = representation.isSameAs(expectation);
        assertTrue(theSame, data.getName() + " -> " + data.getLabel() + "\n" + data.getInput()
            + "\n" + data.getEvents().get(i) + "\n" + events.get(i) + "\n");
      }
    }
  }

  @Test
  @DisplayName("Compose: Run comprehensive test suite for empty Nodes")
  void runAllEmpty() {
    for (SuiteData data : allValidAndEmpty) {
      ComposeResult result = composeData(data);
      List<Node> nodes = result.getNode();
      assertTrue(nodes.isEmpty(),
          data.getName() + " -> " + data.getLabel() + "\n" + data.getInput());
    }
  }
}


class ComposeResult {

  private final List<Node> node;
  private final Exception error;

  public ComposeResult(List<Node> node, Exception error) {
    this.node = node;
    this.error = error;
  }

  public List<Node> getNode() {
    return node;
  }

  public Exception getError() {
    return error;
  }
}
