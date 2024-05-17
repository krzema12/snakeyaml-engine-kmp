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
package org.snakeyaml.engine.issues.issue51;

import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings;
import it.krzeminski.snakeyaml.engine.kmp.composer.Composer;
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ParserException;
import it.krzeminski.snakeyaml.engine.kmp.parser.ParserImpl;
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@org.junit.jupiter.api.Tag("fast")
public class LoadSettingsBufferSizeTest {

  private void parse(String yaml) {
    LoadSettings settings = LoadSettings.builder().setBufferSize(yaml.length()).build();
    new Composer(settings, new ParserImpl(settings, new StreamReader(settings, yaml)))
      .getSingleNode();
  }

  @DisplayName("Issue 51 - exact buffer size")
  @Test
  public void setBufferSizeCutsError() {
    final String yaml = " - foo: bar\n" +
      "   if: 'aaa' == 'bbb'";

    ParserException exception = assertThrows(ParserException.class, () -> parse(yaml));

    String expectedError =
      "while parsing a block mapping\n" +
        " in reader, line 1, column 4:\n" +
        "     - foo: bar\n" +
        "       ^\n" +
        "expected <block end>, but found '<scalar>'\n" +
        " in reader, line 2, column 14:\n" +
        "    if: 'aaa' == 'bbb'\n" +
        "              ^\n";

    assertEquals(expectedError, exception.getMessage());
  }
}
