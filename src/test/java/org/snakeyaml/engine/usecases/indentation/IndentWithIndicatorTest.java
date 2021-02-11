/*
 * Copyright (c) 2018, http://www.snakeyaml.org
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
package org.snakeyaml.engine.usecases.indentation;

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.utils.TestUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@org.junit.jupiter.api.Tag("fast")
public class IndentWithIndicatorTest {
    @Test
    public void testIndentWithIndicator1() {
        DumpSettings settings = DumpSettings.builder()
                .setDefaultFlowStyle(FlowStyle.BLOCK)
                .setIndentWithIndicator(true)
                .setIndent(2)
                .setIndicatorIndent(1)
                .build();
        Dump dumper = new Dump(settings);
        String output = dumper.dumpToString(createData());

        String doc = TestUtils.getResource("indentation/issue416-1.yaml");

        assertEquals(doc, output);
    }

    public void testIndentWithIndicator2() {
        DumpSettings settings = DumpSettings.builder()
                .setDefaultFlowStyle(FlowStyle.BLOCK)
                .setIndentWithIndicator(true)
                .setIndent(2)
                .setIndicatorIndent(2)
                .build();

        Dump dumper = new Dump(settings);
        String output = dumper.dumpToString(createData());

        String doc = TestUtils.getResource("indentation/issue416-2.yaml");

        assertEquals(doc, output);
    }

    public void testIndentWithIndicator3() {
        DumpSettings settings = DumpSettings.builder()
                .setDefaultFlowStyle(FlowStyle.BLOCK)
                .setIndentWithIndicator(false)
                .setIndent(4)
                .setIndicatorIndent(2)
                .build();

        Dump dumper = new Dump(settings);
        String output = dumper.dumpToString(createData());

        String doc = TestUtils.getResource("indentation/issue416_3.yaml");

        assertEquals(doc, output);
    }

    private Map<String, Object> createData() {
        Map<String, String> fred = new LinkedHashMap<>();
        fred.put("name", "Fred");
        fred.put("role", "creator");

        Map<String, String> john = new LinkedHashMap<>();
        john.put("name", "John");
        john.put("role", "committer");

        List<Map<String, String>> developers = new ArrayList<>();
        developers.add(fred);
        developers.add(john);

        Map<String, Object> company = new LinkedHashMap<>();
        company.put("developers", developers);
        company.put("name", "Yet Another Company");
        company.put("location", "Maastricht");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("company", company);

        return data;
    }
}
