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
package org.snakeyaml.engine.issues.issue46;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

/**
 * https://github.com/jruby/jruby/issues/7698
 */
public class JRubyPsychTest {
  @Test
  @DisplayName("Issue 46: parse different values")
  void parseDifferentValues() {
    parse("\\n\\u2029*");
    parse("\\n\\u2028 C");
    parse("\\n\\u2028* C");
    parse("\\n\\u2029* 1");
    parse("\\n\\u2029");
  }

  private void parse(String data) {
    LoadSettings loadSettings = LoadSettings.builder().build();
    Load load = new Load(loadSettings);
    Object obj = load.loadFromString(data);
    assertEquals(data, obj);
  }
}
