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
package org.snakeyaml.engine.usecases.external_test_suite;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.snakeyaml.engine.v1.api.DumpSettingsBuilder;
import org.snakeyaml.engine.v1.api.LoadSettingsBuilder;
import org.snakeyaml.engine.v1.api.lowlevel.Compose;
import org.snakeyaml.engine.v1.api.lowlevel.Present;

@org.junit.jupiter.api.Tag("fast")
class EmitSuiteTest {

    private List<SuiteData> all = SuiteUtils.getAll().stream()
            .filter(data -> !SuiteUtils.deviationsWithSuccess.contains(data.getName()))
            .filter(data -> !SuiteUtils.deviationsWithError.contains(data.getName()))
            .collect(Collectors.toList());

    @Test
    @DisplayName("Emit test suite")
    void runAll(TestInfo testInfo) {
        for (SuiteData data : all) {
            ParseResult result = SuiteUtils.parseData(data);
            if (data.getError()) {
                assertTrue(result.getError().isPresent(), "Expected error, but got none in file " + data.getName() + ", " +
                        data.getLabel() + "\n" + result.getEvents());
            } else {
                Present emit = new Present(new DumpSettingsBuilder().build());
                //emit without errors
                String yaml = emit.emitToString(result.getEvents().iterator());
                //eat your own dog food
                new Compose(new LoadSettingsBuilder().build()).composeAllFromString(yaml);
            }
        }
    }
}

