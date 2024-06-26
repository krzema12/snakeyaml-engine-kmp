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
package org.snakeyaml.engine.v2.constructor;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.constructor.*;
import it.krzeminski.snakeyaml.engine.kmp.api.ConstructNode;
import it.krzeminski.snakeyaml.engine.kmp.api.Load;
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings;
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException;
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node;

import static org.junit.jupiter.api.Assertions.*;

@Tag("fast")
class DefaultConstructorTest {

  @Test
  void constructNullWhenUnknown() {
    LoadSettings settings = LoadSettings.builder().build();
    Load load = new Load(settings, new MagicNullConstructor(settings));
    String str = (String) load.loadOne("!unknownLocalTag a");
    assertNull(str);
  }

  @Test
  void failWhenUnknown() {
    Load load = new Load();
    YamlEngineException exception =
        assertThrows(YamlEngineException.class, () -> load.loadOne("!unknownLocalTag a"));
    assertTrue(exception.getMessage()
        .startsWith("could not determine a constructor for the tag !unknownLocalTag"));
  }
}


/**
 * Make NULL if the tag is not recognized
 */
class MagicNullConstructor extends StandardConstructor {

  public MagicNullConstructor(LoadSettings settings) {
    super(settings);
  }

  @NotNull
  @Override
  public ConstructNode findConstructorFor(@NotNull Node node) {
    return new ConstructYamlNull();
  }
}
