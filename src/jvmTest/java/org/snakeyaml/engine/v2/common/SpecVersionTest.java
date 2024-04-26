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
package org.snakeyaml.engine.v2.common;

import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings;
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlVersionException;
import it.krzeminski.snakeyaml.engine.kmp.nodes.ScalarNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("fast")
class SpecVersionTest {

  @Test
  @DisplayName("Version 1.2 is accepted")
  void version12() {
    LoadSettings settings = LoadSettings.builder().setLabel("spec 1.2").build();
    ScalarNode node = (ScalarNode) new Compose(settings).composeString("%YAML 1.2\n---\nfoo");
    assertEquals("foo", node.getValue());
  }

  @Test
  @DisplayName("Version 1.3 is accepted by default")
  void version13() {
    LoadSettings settings = LoadSettings.builder().setLabel("spec 1.3").build();
    ScalarNode node = (ScalarNode) new Compose(settings).composeString("%YAML 1.3\n---\nfoo");
    assertEquals("foo", node.getValue());
  }

  @Test
  @DisplayName("Version 1.3 is rejected if configured")
  void version13rejected() {
    LoadSettings settings =
        LoadSettings.builder().setLabel("spec 1.3").setVersionFunction(version -> {
          if (version.getMinor() > 2) {
            throw new IllegalArgumentException("Too high.");
          } else {
            return version;
          }
        }).build();
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> new Compose(settings).composeString("%YAML 1.3\n---\nfoo"));
    assertEquals("Too high.", exception.getMessage());
  }

  @Test
  @DisplayName("Version 2.0 is rejected")
  void version20() {
    LoadSettings settings = LoadSettings.builder().setLabel("spec 2.0").build();
    YamlVersionException exception = assertThrows(YamlVersionException.class,
        () -> new Compose(settings).composeString("%YAML 2.0\n---\nfoo"));
    assertEquals("Version{major=2, minor=0}", exception.getMessage());
  }
}
