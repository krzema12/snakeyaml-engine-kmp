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
package org.snakeyaml.engine.usecases.external_test_suite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.lowlevel.Parse;
import org.snakeyaml.engine.v2.events.Event;

@org.junit.jupiter.api.Tag("fast")
class ParseSuiteTest {

  private final List<String> ignore = Lists.newArrayList(
      "652Z", "Y2GN");

  private final List<SuiteData> all = SuiteUtils.getAll().stream()
      .filter(data -> !ignore.contains(data.getName()))
      .collect(Collectors.toList());

  @Test
  @DisplayName("Parse: Run one test")
  /**
   * This test is used to debug one test (which is given explicitly)
   */
  void runOne() {
    SuiteData data = SuiteUtils.getOne("652Z");
    LoadSettings settings = LoadSettings.builder().setLabel(data.getLabel()).build();
    Iterable<Event> iterable = new Parse(settings).parseString(data.getInput());
    for (Event event : iterable) {
      assertNotNull(event);
      //System.out.println(event);
    }
  }

  @Test
  @DisplayName("Run comprehensive test suite")
  void runAll() {
    for (SuiteData data : all) {
      ParseResult result = SuiteUtils.parseData(data);
      boolean shouldFail = data.hasError();
      if (SuiteUtils.deviationsWithSuccess.contains(data.getName())
          || SuiteUtils.deviationsWithError.contains(data.getName())) {
        shouldFail = !shouldFail;
      }
      if (shouldFail) {
        assertTrue(result.getError().isPresent(),
            "Expected error, but got none in file " + data.getName() + ", " +
                data.getLabel() + "\n" + result.getEvents());
      } else {
        if (result.getError().isPresent()) {
          fail("Testcase: " + data.getName() + "; label: " + data.getLabel()
              + "\nExpected NO error, but got: " + result.getError().get());
        } else {
          List<ParsePair> pairs = Streams.zip(data.getEvents().stream(),
                  result.getEvents().stream(), ParsePair::new)
              .collect(Collectors.toList());
          for (ParsePair pair : pairs) {
            EventRepresentation representation = new EventRepresentation(pair.getEvent());
            assertEquals(pair.getExpected(), representation.getRepresentation(),
                "Failure in " + data.getName());
          }
        }
      }
    }
  }
}


class ParsePair {

  private final String expected;
  private final Event event;

  public ParsePair(String expected, Event event) {
    this.expected = expected;
    this.event = event;
  }

  public String getExpected() {
    return expected;
  }

  public Event getEvent() {
    return event;
  }
}
