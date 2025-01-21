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
package org.snakeyaml.engine.v2.api.types;

import com.google.common.collect.Lists;
import it.krzeminski.snakeyaml.engine.kmp.api.Dump;
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings;
import it.krzeminski.snakeyaml.engine.kmp.api.Load;
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node;
import it.krzeminski.snakeyaml.engine.kmp.representer.StandardRepresenter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@org.junit.jupiter.api.Tag("fast")
class OptionalTest {

  @Test
  @DisplayName("Represent Optional as value")
  void representOptional() {
    StandardRepresenter standardRepresenter =
      new StandardRepresenter(DumpSettings.builder().build());
    Node node = standardRepresenter.represent(Optional.of("a"));
    assertEquals("tag:yaml.org,2002:java.util.Optional", node.getTag().getValue());
  }

  @Test
  @DisplayName("Represent Optional.empty as null")
  void representEmptyOptional() {
    StandardRepresenter standardRepresenter =
      new StandardRepresenter(DumpSettings.builder().build());
    Node node = standardRepresenter.represent(Optional.empty());
    assertEquals("tag:yaml.org,2002:null", node.getTag().getValue());
  }

  @Test
  @DisplayName("Dump Optional as its value")
  void dumpOptional() {
    DumpSettings settings = DumpSettings.builder().build();
    Dump dump = new Dump(settings);
    String str = dump.dumpToString(Optional.of("a"));
    assertEquals("!!java.util.Optional 'a'\n", str);
  }

  @Test
  @DisplayName("Dump empty Optional as null")
  void dumpEmptyOptional() {
    DumpSettings settings = DumpSettings.builder().build();
    Dump dump = new Dump(settings);
    String str = dump.dumpToString(Optional.empty());
    assertEquals("null\n", str);
  }

  @Test
  @DisplayName("Dump Optionals")
  void dumpListOfOptional() {
    DumpSettings settings = DumpSettings.builder().build();
    Dump dump = new Dump(settings);
    String str = dump.dumpToString(Lists.newArrayList(
      Optional.of(2),
      Optional.empty(),
      Optional.of("a"))
    );
    assertEquals("[!!java.util.Optional '2', null, !!java.util.Optional 'a']\n", str);
  }

  @Test
  @DisplayName("Dump Optionals")
  void dumpListOfOptional2() {
    DumpSettings settings = DumpSettings.builder().build();
    Dump dump = new Dump(settings);
    String str = dump.dumpToString(Optional.of(Lists.newArrayList(1, 2)));
    assertEquals("!!java.util.Optional [1, 2]\n", str);
  }

  // parse
  @Test
  @DisplayName("Optional 'a' is parsed")
  void parseOptional() {
    Load load = new Load();
    Optional<String> str = (Optional<String>) load.loadOne("!!java.util.Optional a");
    assertEquals(Optional.of("a"), str);
  }

  @Test
  @DisplayName("Empty Optional parsed")
  void parseEmptyOptional() {
    Load load = new Load();
    Optional<String> str = (Optional<String>) load.loadOne("!!java.util.Optional null");
    assertEquals(Optional.empty(), str);
  }

  @Test
  @DisplayName("Empty Optional parsed")
  void parseEmptyOptional2() {
    Load load = new Load();
    Optional<String> str = (Optional<String>) load.loadOne("!!java.util.Optional ");
    assertEquals(Optional.empty(), str);
  }
}
