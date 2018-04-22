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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

@org.junit.jupiter.api.Tag("fast")
class ParseSuiteTest {
    private List<String> exceptions = Lists.newArrayList("9C9N", "SU5Z", "QB6E", "QLJ7", "EB22");

    private List<SuiteData> all = SuiteUtils.getAll().stream()
            .filter(data -> exceptions.contains(data.getLabel()))
            .collect(Collectors.toList());

    @Test
    @DisplayName("Resolve MAP does not depend on value or boolean")
    void resolveMap(TestInfo testInfo) {
        int errorCount = 0;
        for (SuiteData data : all) {
            ParseResult result = SuiteUtils.parseData(data);
            if (data.getError()) {
                assertTrue(result.getError().isPresent(), "Expected error, but got none in file " + data.getName() + ", " +
                        data.getLabel() + "\n" + result.getEvents());
            }

        }
    }

}
