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

/**
 * Copyright (c) 2008, SnakeYAML
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.exceptions.ScannerException;

/**
 * https://github.com/FasterXML/jackson-dataformats-text/issues/406
 * https://bugs.chromium.org/p/oss-fuzz/issues/detail?id=56902
 */
@org.junit.jupiter.api.Tag("fast")
public class FuzzYAMLRead56902Test {

  @Test
  public void testHugeMinorValue() {
    try {
      LoadSettings settings = LoadSettings.builder().build();
      Load yamlProcessor = new Load(settings);
      yamlProcessor.loadFromString("%YAML 1.9224775801");
      fail("Invalid escape code in double quoted scalar should not be accepted");
    } catch (ScannerException e) {
      assertTrue(e.getMessage().contains(
          "found a number which cannot represent a valid version: 9224775801"), e.getMessage());
    }
  }

  @Test
  public void testHugeMajorValue() {
    try {
      LoadSettings settings = LoadSettings.builder().build();
      Load yamlProcessor = new Load(settings);
      yamlProcessor.loadFromString("%YAML 100651234565.1");
      fail("Invalid escape code in double quoted scalar should not be accepted");
    } catch (ScannerException e) {
      assertTrue(
          e.getMessage()
              .contains("found a number which cannot represent a valid version: 100651234565"),
          e.getMessage());
    }
  }
}
