/*
 * Copyright (c) 2018, SnakeYAML
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
package org.snakeyaml.engine.issues.issue1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.ConstructNode;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.Tag;

@org.junit.jupiter.api.Tag("fast")
class NullConstructorTest {

  @Test
  void customConstructorMustBeCalledWithoutNode() {
    Map<Tag, ConstructNode> tagConstructors = new HashMap<>();
    tagConstructors.put(Tag.NULL, new MyConstructNull());
    LoadSettings settings = LoadSettings.builder().setTagConstructors(tagConstructors).build();
    Load loader = new Load(settings);
    assertNotNull(loader.loadFromString(""), "Expected MyConstructNull to be called.");
    assertEquals("absent", loader.loadFromString(""), "Expected MyConstructNull to be called.");
  }

  @Test
  void customConstructorMustBeCalledWithNode() {
    Map<Tag, ConstructNode> tagConstructors = new HashMap<>();
    tagConstructors.put(Tag.NULL, new MyConstructNull());
    LoadSettings settings = LoadSettings.builder().setTagConstructors(tagConstructors).build();
    Load loader = new Load(settings);
    assertEquals("present", loader.loadFromString("!!null null"),
        "Expected MyConstructNull to be called.");
  }

  private class MyConstructNull implements ConstructNode {

    @Override
    public Object construct(Node node) {
      if (node == null) {
        return "absent";
      } else {
        return "present";
      }
    }
  }
}
