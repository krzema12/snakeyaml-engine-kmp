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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.YamlUnicodeReader;
import org.snakeyaml.engine.v2.exceptions.ReaderException;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;
import org.snakeyaml.engine.v2.scanner.StreamReader;

@org.junit.jupiter.api.Tag("fast")
public class InheritedReaderTest extends InheritedImportTest {

  @Test
  @DisplayName("Reader errors")
  public void testReaderUnicodeErrors() throws IOException {
    File[] inputs = getStreamsByExtension(".stream-error");
    for (int i = 0; i < inputs.length; i++) {
      InputStream input = new FileInputStream(inputs[i]);
      YamlUnicodeReader unicodeReader = new YamlUnicodeReader(input);
      StreamReader stream = new StreamReader(LoadSettings.builder().build(), unicodeReader);
      try {
        while (stream.peek() != '\u0000') {
          stream.forward();
        }
        fail("Invalid stream must not be accepted: " + inputs[i].getAbsolutePath() + "; encoding="
            + unicodeReader.encoding);
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
