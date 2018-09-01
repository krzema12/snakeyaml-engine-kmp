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

import java.util.List;

public class SuiteData {
    private final String name;
    private final String label;
    private final String input;
    private final List<String> events;
    private Boolean error;

    public SuiteData(String name, String label, String input, List<String> events, Boolean error) {
        this.name = name;
        this.label = label;
        this.input = input;
        this.events = events;
        this.error = error;
    }

    public SuiteData(String name, String label, String input, List<String> events) {
        this(name, label, input, events, false);
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public String getInput() {
        return input;
    }

    public List<String> getEvents() {
        return events;
    }

    public Boolean getError() {
        return error;
    }
}
