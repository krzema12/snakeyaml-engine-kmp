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
package org.snakeyaml.engine.usecases.references;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.api.Load;
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings;

@Tag("fast")
public class RestrictAliasNamesTest {

  @Test
  public void testAliasFromRuby() {
    try {
      LoadSettings settings = LoadSettings.builder().build();
      Load yamlProcessor = new Load(settings);
      yamlProcessor.loadFromString("Exclude: **/*_old.rb");
      fail("Should not accept Alias **/*_old.rb");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("unexpected character found *(42)"));
    }
  }
}
