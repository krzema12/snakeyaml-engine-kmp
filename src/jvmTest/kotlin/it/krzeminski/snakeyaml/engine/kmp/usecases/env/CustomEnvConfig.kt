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
package it.krzeminski.snakeyaml.engine.kmp.usecases.env

import it.krzeminski.snakeyaml.engine.kmp.env.EnvConfig

/**
 * Configurator for ENV format
 *
 * @see [Variable
 * substitution](https://bitbucket.org/snakeyaml/snakeyaml-engine/wiki/Documentation.markdown-header-variable-substitution)
 */
class CustomEnvConfig(private val provided: Map<String, String>) : EnvConfig {
    /**
     * Implement deviation from the standard logic. It chooses the value from the provided map, if not
     * found than check the system property, if not found than follow the standard logic.
     *
     * @param name        - variable name in the template
     * @param separator   - separator in the template, can be :-, -, :?, ? or null if not present
     * @param value       - default value or the error in the template or empty if not present
     * @param environment - the value from environment for the provided variable or null if unset
     * @return the value to apply in the template or empty to follow the standard logic
     */
    override fun getValueFor(
        name: String,
        separator: String?,
        value: String?,
        environment: String?
    ): String? = provided[name] ?: System.getProperty(name)
}
