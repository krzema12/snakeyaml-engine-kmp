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

import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings;
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose;
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node;
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * test that implicit resolver assigns the tag
 */
public class EnvTagTest {

  @Test
  public void testImplicitResolverForEnvConstructor() {
    Compose loader = new Compose(LoadSettings.builder().build());
    Node loaded = loader.compose("${PATH}");
    assertEquals(Tag.ENV_TAG, loaded.getTag());
  }
}
