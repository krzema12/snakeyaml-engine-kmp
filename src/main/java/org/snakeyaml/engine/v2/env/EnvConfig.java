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
package org.snakeyaml.engine.v2.env;

import java.util.Map;
import java.util.Optional;

/**
 * Configurator for ENV format
 *
 * @see <a href="https://bitbucket.org/asomov/snakeyaml-engine/wiki/Documentation#markdown-header-variable-substitution">Variable substitution</a>
 */
public class EnvConfig {
    public EnvConfig(Map<String, String> provided) {
        this.provided = provided;
    }

    private final Map<String, String> provided;

    /**
     * Implement deviation from the standard logic. It chooses the value from the provided map or
     * follows the standard logic if the map does not have the key.
     * It can be overridden to implement the custom logic for variable substitution
     * (for instance to use system properties)
     * TODO implement wiki page
     *
     * @param name        - variable name in the template
     * @param separator   - separator in the template, can be :-, -, :?, ? or null if not present
     * @param value       - default value or the error in the template or empty if not present
     * @param environment - the value from environment for the provided variable or null if unset
     * @return the value to apply in the template or empty to follow the standard logic
     */
    public Optional<String> getValueFor(String name, String separator, String value, String environment) {
        if (provided.containsKey(name)) {
            return Optional.of(provided.get(name));
        } else {
            return Optional.empty();
        }
    }
}
