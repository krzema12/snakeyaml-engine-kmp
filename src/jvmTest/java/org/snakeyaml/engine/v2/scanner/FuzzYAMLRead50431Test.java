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
package org.snakeyaml.engine.v2.scanner;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.api.Load;
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings;
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ScannerException;

/**
 * https://github.com/FasterXML/jackson-dataformats-text/issues/400
 * https://github.com/FasterXML/jackson-dataformats-text/pull/401
 */
@org.junit.jupiter.api.Tag("fast")
public class FuzzYAMLRead50431Test {

  private final Load load = new Load();

  @Test
  public void testIncompleteValue() {
    try {
      load.loadOne("\"\\UE30EEE");
      fail("Invalid escape code in double quoted scalar should not be accepted");
    } catch (ScannerException e) {
      assertEquals(
          "found unknown escape character E30EEE",
          e.getMessage().lines().findFirst().orElse(null)
      );
    }
  }

  @Test
  public void testProperValue() {
    String parsed = (String) load.loadOne("\"\\U0000003B\"");
    assertEquals(1, parsed.length());
    assertEquals("\u003B", parsed);
  }

  @Test
  public void testNotQuoted() {
    String parsed = (String) load.loadOne("\\UE30EEE");
    assertEquals(8, parsed.length());
    assertEquals("\\UE30EEE", parsed);
  }
}
