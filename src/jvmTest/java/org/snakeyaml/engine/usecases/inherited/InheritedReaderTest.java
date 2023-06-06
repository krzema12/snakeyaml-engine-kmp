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

import okio.Okio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.YamlUnicodeReader;
import org.snakeyaml.engine.v2.exceptions.ReaderException;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;
import org.snakeyaml.engine.v2.scanner.StreamReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@org.junit.jupiter.api.Tag("fast")
public class InheritedReaderTest extends InheritedImportTest {

  @Test
  @DisplayName("Reader errors")
  public void testReaderUnicodeErrors() throws IOException {
    File[] inputs = getStreamsByExtension(".stream-error");
    for (File file : inputs) {
      if (
        // Skip these files - Okio seems to parse them correctly, so the test fails.
        // Supporting UTF-16 will be much more difficult anyway as more code is transferred to KMP, because
        // KMP basically only supports UTF-8.
          file.getName().equals("odd-utf16.stream-error")
              || file.getName().equals("invalid-utf8-byte.stream-error")
      ) {
        continue;
      }
      InputStream input = new FileInputStream(file);
      YamlUnicodeReader unicodeReader = new YamlUnicodeReader(Okio.source(input));
      StreamReader stream = new StreamReader(LoadSettings.builder().build(), unicodeReader);
      try {
        while (stream.peek() != '\u0000') {
          stream.forward();
        }
        fail("Invalid stream must not be accepted: " + file.getAbsolutePath() + "; encoding="
            + unicodeReader.getEncoding());
      } catch (ReaderException e) {
        assertTrue(e.toString().contains(" special characters are not allowed"), e.toString());
      } catch (YamlEngineException e) {
        assertTrue(e.toString().contains("MalformedInputException"), e.toString());
      } finally {
        input.close();
      }
    }
  }
}
