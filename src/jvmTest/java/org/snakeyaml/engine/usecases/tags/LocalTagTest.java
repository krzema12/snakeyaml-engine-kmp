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
package org.snakeyaml.engine.usecases.tags;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.ConstructNode;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.nodes.Tag;

/**
 * Example of parsing a local tag
 */
@org.junit.jupiter.api.Tag("fast")
public class LocalTagTest {

  @Test
  public void testLocalTag() {
    Map<Tag, ConstructNode> tagConstructors = new HashMap<>();
    // register to call CustomConstructor when the Tag !ImportValue is found
    tagConstructors.put(new Tag("!ImportValue"), new CustomConstructor());
    LoadSettings settings = LoadSettings.builder().setTagConstructors(tagConstructors).build();
    Load loader = new Load(settings);
    Object obj = loader.loadFromString("VpcId: !ImportValue SpokeVPC");
    Map<String, ImportValueImpl> map = (Map<String, ImportValueImpl>) obj;
    assertEquals("SpokeVPC", map.get("VpcId").getValue());
  }
}
