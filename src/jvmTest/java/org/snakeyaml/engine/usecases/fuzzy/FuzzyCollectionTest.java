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
package org.snakeyaml.engine.usecases.fuzzy;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.api.Load;
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings;

public class FuzzyCollectionTest {

  /**
   * https://bitbucket.org/snakeyaml/snakeyaml/issues/1064 This is different from SnakeYAML - the
   * YAML looks valid.
   */
  @Test
  public void testFuzzyInput() {
    String datastring =
        " ? - - ? - - ? ? - - ? ? ? - - ? ? - - ? ? ? - - ? ? - ? ? - - ? - - ? ? ? - - ? ? - ?  -? - ? ? - - ? - - ? ? ? - - ? ? - ?  -? - ? ? - - ? - ";
    InputStream datastream = new ByteArrayInputStream(datastring.getBytes());
    InputStreamReader reader = new InputStreamReader(datastream, StandardCharsets.UTF_8);

    LoadSettings settings = LoadSettings.builder().setAllowRecursiveKeys(true)
        .setMaxAliasesForCollections(1000).setAllowDuplicateKeys(true).build();
    Load yamlProcessor = new Load(settings);
    Object fuzzy = yamlProcessor.loadFromReader(reader);
    assertTrue(fuzzy.toString().startsWith("{[[{[[{{[[{{{[[{{[[{{{[[{{[{{[[{[[{{{[[{{[{-?"));
  }
}
