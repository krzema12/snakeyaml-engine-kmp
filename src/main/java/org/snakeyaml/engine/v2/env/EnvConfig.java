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

/**
 * Configurator for ENV format
 *
 * @see <a href=https://bitbucket.org/asomov/snakeyaml-engine/wiki/Documentation#markdown-header-variable-substitution">Variable substitution</a>
 */
public class EnvConfig {
    public EnvConfig(Map<String, String> undefined) {
        this.undefined = undefined;
    }

    private final Map<String, String> undefined;

    /**
     * Select the value to be parsed
     *
     * @param key          - environment variable name
     * @param value        - the value of the ENV variable
     * @param errorMessage - the error defined in ${VARIABLE:?err} format
     * @param emptyAllowed - true for either ${VARIABLE:-default} or ${VARIABLE:?err} formats. It indicates that
     *                     the provided default or error must be appied also if the ENV defined as empty
     * @return value to be returned by the parser
     */
    public String getValueFor(String key, String value, String defaultValue, String errorMessage, boolean emptyAllowed) {
        if (value == null || value.isEmpty()) {
            return undefined.get(key);
        } else {
            return value;
        }
    }
}
