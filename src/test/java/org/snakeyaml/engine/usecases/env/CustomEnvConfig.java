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
package org.snakeyaml.engine.usecases.env;

import org.jetbrains.annotations.NotNull;
import org.snakeyaml.engine.v2.env.EnvConfig;

import java.util.Map;

/**
 * Configurator for ENV format
 *
 * @see <a href=
 * "https://bitbucket.org/snakeyaml/snakeyaml-engine/wiki/Documentation#markdown-header-variable-substitution">Variable
 * substitution</a>
 */
public class CustomEnvConfig implements EnvConfig {

  private final Map<String, String> provided;

  public CustomEnvConfig(Map<String, String> provided) {
    this.provided = provided;
  }

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
  @NotNull
  public String getValueFor(@NotNull String name,
                            String separator,
                            String value,
                            String environment) {
    if (provided.containsKey(name)) {
      return provided.get(name);
    } else if (System.getProperty(name) != null) {
      return System.getProperty(name);
    } else {
      return null;
    }
  }
}
