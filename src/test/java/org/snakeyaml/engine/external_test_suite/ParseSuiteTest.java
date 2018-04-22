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

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.events.Event;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@org.junit.jupiter.api.Tag("fast")
class ParseSuiteTest {
    private List<String> deviations = Lists.newArrayList("9C9N", "SU5Z", "QB6E", "QLJ7", "EB22");

    private List<SuiteData> all = SuiteUtils.getAll().stream()
            .filter(data -> !deviations.contains(data.getName()))
            .collect(Collectors.toList());

    @Test
    @DisplayName("Run comprehensive test suite")
    void runAll(TestInfo testInfo) {
        int errorCounter = 0;
        for (SuiteData data : all) {
            ParseResult result = SuiteUtils.parseData(data);
            if (data.getError()) {
                assertTrue(result.getError().isPresent(), "Expected error, but got none in file " + data.getName() + ", " +
                        data.getLabel() + "\n" + result.getEvents());
                System.out.println("Error");

            } else {
                if (result.getError().isPresent()) {
                    //println("Expected NO error, but got: " + result.second)
                    errorCounter++;
                } else {
                    List<Pair> pairs = Streams.zip(data.getEvents().stream(), result.getEvents().stream(), Pair::new)
                            .collect(Collectors.toList());
                    System.out.println(pairs);
                    for(Pair pair : pairs) {
                        assertEquals(pair.getEtalon(), pair.getEvent().toString());
                    }
                }
            }

        }
    }

}

class Pair {
    private String etalon;
    private Event event;

    public Pair(String etalon, Event event) {
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
