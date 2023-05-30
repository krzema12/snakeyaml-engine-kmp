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
package org.snakeyaml.engine.v2.api.lowlevel;

import com.google.common.io.CharSource;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.nodes.Node;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("fast")
class ComposeTest {

  @Test
  void composeEmptyReader() throws IOException {
    Compose compose = new Compose(LoadSettings.builder().build());
    Node node = compose.composeReader(CharSource.wrap("").openStream());
    assertNull(node);
  }

  @Test
  void composeEmptyInputStream() {
    Compose compose = new Compose(LoadSettings.builder().build());
    Node node = compose.composeInputStream(new ByteArrayInputStream("".getBytes()));
    assertNull(node);
  }

  @Test
  void composeAllFromEmptyReader() throws IOException {
    Compose compose = new Compose(LoadSettings.builder().build());
    Iterable<Node> nodes = compose.composeAllFromReader(CharSource.wrap("").openStream());
    assertFalse(nodes.iterator().hasNext());
  }

  @Test
  void composeAllFromEmptyInputStream() {
    Compose compose = new Compose(LoadSettings.builder().build());
    Iterable<Node> nodes = compose.composeAllFromInputStream(new ByteArrayInputStream("".getBytes()));
    assertFalse(nodes.iterator().hasNext());
  }
}
