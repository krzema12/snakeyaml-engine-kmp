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
package org.snakeyaml.engine.usecases.inherited;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.events.Event;
import it.krzeminski.snakeyaml.engine.kmp.tokens.Token;

@org.junit.jupiter.api.Tag("fast")
public class InheritedCanonicalTest extends InheritedImportTest {

  @Test
  @DisplayName("Canonical scan")
  public void testCanonicalScanner() throws IOException {
    File[] files = getStreamsByExtension(".canonical");
    assertTrue(files.length > 0, "No test files found.");
    for (final File file : files) {
      try (InputStream input = Files.newInputStream(file.toPath())) {
        List<Token> tokens = canonicalScan(input, file.getName());
        assertFalse(tokens.isEmpty(), "expect tokens are not empty");
      }
    }
  }

  private List<Token> canonicalScan(InputStream input, String label) throws IOException {
    int ch = input.read();
    StringBuilder buffer = new StringBuilder();
    while (ch != -1) {
      buffer.append((char) ch);
      ch = input.read();
    }
    CanonicalScanner scanner =
        new CanonicalScanner(buffer.toString().replace(System.lineSeparator(), "\n"), label);
    List<Token> result = new ArrayList();
    while (scanner.hasNext()) {
      result.add(scanner.next());
    }
    return result;
  }

  @Test
  @DisplayName("Canonical parse")
  public void testCanonicalParser() throws IOException {
    final File[] files = getStreamsByExtension(".canonical");
    assertTrue(files.length > 0, "No test files found.");
    for (final File file : files) {
      try (InputStream input = Files.newInputStream(file.toPath())) {
        List<Event> tokens = canonicalParse(input, file.getName());
        assertFalse(tokens.isEmpty(), "expect tokens are not empty");
      }
    }
  }
}
