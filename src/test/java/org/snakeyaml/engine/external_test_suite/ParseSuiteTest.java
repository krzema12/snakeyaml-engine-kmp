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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.api.LoadSettings;
import org.snakeyaml.engine.api.lowlevel.Parse;
import org.snakeyaml.engine.events.Event;

import com.google.common.collect.Streams;

@org.junit.jupiter.api.Tag("fast")
class ParseSuiteTest {

    private List<SuiteData> all = SuiteUtils.getAll().stream()
            .filter(data -> !SuiteUtils.deviationsWithSuccess.contains(data.getName()))
            .filter(data -> !SuiteUtils.deviationsWithError.contains(data.getName()))
            .collect(Collectors.toList());

    @Test
    @DisplayName("Parse: Run one test")
    void runOne(TestInfo testInfo) {
        SuiteData data = SuiteUtils.getOne("6FWR");
        LoadSettings settings = new LoadSettings();
        settings.setLabel(data.getLabel());
        Iterable<Event> iterable = new Parse(settings).parseString(data.getInput());
        for (Event event : iterable) {
            //System.out.println(event);
        }
    }

    @Test
    @DisplayName("Run comprehensive test suite")
    void runAll(TestInfo testInfo) {
        for (SuiteData data : all) {
            ParseResult result = SuiteUtils.parseData(data);
            if (data.getError()) {
                assertTrue(result.getError().isPresent(), "Expected error, but got none in file " + data.getName() + ", " +
                        data.getLabel() + "\n" + result.getEvents());
            } else {
                if (result.getError().isPresent()) {
                    fail("Expected NO error, but got: " + result.getError().get());
                } else {
                    List<ParsePair> pairs = Streams.zip(data.getEvents().stream(), result.getEvents().stream(), ParsePair::new)
                            .collect(Collectors.toList());
                    for (ParsePair pair : pairs) {
                        EventRepresentation representation = new EventRepresentation(pair.getEvent());
                        assertEquals(pair.getEtalon(), representation.getRepresentation());
                    }
                }
            }
        }
    }
}


class ParsePair {
    private String etalon;
    private Event event;

    public ParsePair(String etalon, Event event) {
        this.etalon = etalon;
        this.event = event;
    }

    public String getEtalon() {
        return etalon;
    }

    public Event getEvent() {
        return event;
    }
}
