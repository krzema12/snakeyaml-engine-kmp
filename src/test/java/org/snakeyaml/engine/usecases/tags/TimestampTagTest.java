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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.ConstructNode;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.resolver.JsonScalarResolver;

/**
 * Example of parsing a !!timestamp tag
 */
@org.junit.jupiter.api.Tag("fast")
public class TimestampTagTest {

  public static final Tag myTime = new Tag(Tag.PREFIX + "timestamp");

  @Test
  public void testExplicitTag() {
    Map<Tag, ConstructNode> tagConstructors = new HashMap<>();
    tagConstructors.put(myTime, new TimestampConstructor());
    LoadSettings settings = LoadSettings.builder().setTagConstructors(tagConstructors).build();
    Load loader = new Load(settings);
    LocalDateTime obj =
        (LocalDateTime) loader.loadFromString("!!timestamp 2020-03-24T12:34:00.333");
    assertEquals(LocalDateTime.of(2020, 3, 24, 12, 34, 00, 333000000), obj);
  }

  @Test
  public void testImplicitTag() {
    Map<Tag, ConstructNode> tagConstructors = new HashMap<>();
    tagConstructors.put(new Tag(Tag.PREFIX + "timestamp"), new TimestampConstructor());
    LoadSettings settings = LoadSettings.builder().setTagConstructors(tagConstructors)
        .setScalarResolver(new MyScalarResolver()).build();
    Load loader = new Load(settings);
    LocalDateTime obj = (LocalDateTime) loader.loadFromString("2020-03-24T12:34:00.333");
    assertEquals(LocalDateTime.of(2020, 3, 24, 12, 34, 00, 333000000), obj);

    assertEquals(2020, loader.loadFromString("2020"));
    assertEquals(3, ((List<String>) loader.loadFromString("[a, b, c]")).size());
    assertEquals("[a, b, c]", loader.loadFromString("[a, b, c]").toString());
  }

  public static final class TimestampConstructor implements ConstructNode {

    @Override
    public Object construct(Node node) {
      ScalarNode scalar = (ScalarNode) node;
      // the parsing depends on what should be parsed and to which object
      // examples can be found in SnakeYAML tests for the YAML 1.1 types format
      return LocalDateTime.parse(scalar.getValue());
    }
  }

  public static final class MyScalarResolver extends JsonScalarResolver {

    // this is taken from YAML 1.1 types
    public static final Pattern TIMESTAMP = Pattern.compile(
        "^(?:[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]|[0-9][0-9][0-9][0-9]-[0-9][0-9]?-[0-9][0-9]?(?:[Tt]|[ \t]+)[0-9][0-9]?:[0-9][0-9]:[0-9][0-9](?:\\.[0-9]*)?(?:[ \t]*(?:Z|[-+][0-9][0-9]?(?::[0-9][0-9])?))?)$");

    @Override
    public Tag resolve(String value, Boolean implicit) {
      if (TIMESTAMP.matcher(value).matches()) {
        return myTime;
      } else {
        return super.resolve(value, implicit);
      }
    }
  }
}
