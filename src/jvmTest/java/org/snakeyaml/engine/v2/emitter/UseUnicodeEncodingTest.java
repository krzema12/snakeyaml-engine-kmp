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
package org.snakeyaml.engine.v2.emitter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.api.Dump;
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings;

@Tag("fast")
public class UseUnicodeEncodingTest {

  @Test
  public void testEmitUnicode() {
    DumpSettings settings = DumpSettings.builder().build();
    Dump dump = new Dump(settings);
    String russianUnicode = "Пушкин - это наше всё! 😊";
    assertEquals(russianUnicode + "\n", dump.dumpToString(russianUnicode));
  }

  @Test
  public void testEscapeUnicode() {
    DumpSettings settings = DumpSettings.builder().setUseUnicodeEncoding(false).build();
    Dump dump = new Dump(settings);
    String russianUnicode = "Пушкин - это наше всё! 😊";
    assertEquals(
        "\"\\u041f\\u0443\\u0448\\u043a\\u0438\\u043d - \\u044d\\u0442\\u043e \\u043d\\u0430\\u0448\\u0435\\\n"
            + "  \\ \\u0432\\u0441\\u0451! \\U0001f60a\"\n",
        dump.dumpToString(russianUnicode));
  }
}
