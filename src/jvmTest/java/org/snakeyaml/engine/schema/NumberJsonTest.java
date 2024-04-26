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
package org.snakeyaml.engine.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.api.Dump;
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings;
import it.krzeminski.snakeyaml.engine.kmp.api.Load;
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings;

@org.junit.jupiter.api.Tag("fast")
public class NumberJsonTest {

  Load loader = new Load(LoadSettings.builder().build());

  @Test
  @DisplayName("Test all integers which are define in the core schema & JSON")
  void parseInteger() {
    assertEquals(Integer.valueOf(1), loader.loadFromString("1"));
    assertEquals(Integer.valueOf(-1), loader.loadFromString("-1"));
    assertEquals(Integer.valueOf(0), loader.loadFromString("0"));
    assertEquals(Integer.valueOf(0), loader.loadFromString("-0"));
    assertEquals("012", loader.loadFromString("012"), "Leading zeros are not allowed.");
    assertEquals(Integer.valueOf(1234567890), loader.loadFromString("1234567890"));
    assertEquals(Long.valueOf(12345678901L), loader.loadFromString("12345678901"));
    assertEquals(new BigInteger("1234567890123456789123"),
        loader.loadFromString("1234567890123456789123"));
  }

  @Test
  @DisplayName("Test all strings which WERE integers or doubles in YAML 1.1")
  void parseString() {
    assertEquals("12:10:02", loader.loadFromString("12:10:02"));
    assertEquals("0b1010", loader.loadFromString("0b1010"));
    assertEquals("0xFF", loader.loadFromString("0xFF"));
    assertEquals("1_000", loader.loadFromString("1_000"));

    assertEquals("1_000.5", loader.loadFromString("1_000.5"));
    assertEquals("+.inf", loader.loadFromString("+.inf"));

    // start with +
    assertEquals("+1", loader.loadFromString("+1"));
    assertEquals("+1223344", loader.loadFromString("+1223344"));
    assertEquals("+12.23344", loader.loadFromString("+12.23344"));
    assertEquals("+0.23344", loader.loadFromString("+0.23344"));
    assertEquals("+0", loader.loadFromString("+0"));

    // leading zero
    assertEquals("03", loader.loadFromString("03"));
    assertEquals("03.67", loader.loadFromString("03.67"));
    assertEquals("3.6", loader.loadFromString("! 3.6"));
    assertEquals("3", loader.loadFromString("! 3"));
  }

  @Test
  @DisplayName("Test all doubles which are define in the core schema & JSON")
  void parseDouble() {
    assertEquals(Double.valueOf(-1.345), loader.loadFromString("-1.345"));
    assertEquals(Double.valueOf(0), loader.loadFromString("0.0"));
    assertEquals(Double.valueOf(0f), loader.loadFromString("0.0"));
    assertEquals(Double.valueOf(0d), loader.loadFromString("0.0"));
    assertEquals(Double.valueOf(+0), loader.loadFromString("0.0"));
    assertEquals(Double.valueOf(-0.0), loader.loadFromString("-0.0"));
    assertEquals(Double.valueOf(0.123), loader.loadFromString("0.123"));
    assertEquals(Double.valueOf(1.23E-6), loader.loadFromString("1.23e-6"));
    assertEquals(Double.valueOf(1.23E6), loader.loadFromString("1.23e+6"));
    assertEquals(Double.valueOf(1.23E6), loader.loadFromString("1.23e6"));
    assertEquals(Double.valueOf(1.23), loader.loadFromString("1.23E0"));
    assertEquals(Double.valueOf(-1.23E6), loader.loadFromString("-1.23e6"));
    assertEquals(Double.valueOf(1000.25), loader.loadFromString("1000.25"));
    assertEquals(Double.valueOf(9000.0), loader.loadFromString("9000.00"));
    assertEquals(Double.valueOf(1.0), loader.loadFromString("1."));
  }

  @Test
  @DisplayName("Parse special doubles which are defined in the JSON schema, but not in JSON")
  void parseDoubleSpecial() {
    assertEquals(Double.POSITIVE_INFINITY, loader.loadFromString(".inf"));
    assertEquals(Double.NEGATIVE_INFINITY, loader.loadFromString("-.inf"));
    assertEquals(Double.NaN, loader.loadFromString(".nan"));

    assertEquals(".INF", loader.loadFromString(".INF"));
    assertEquals(".NAN", loader.loadFromString(".NAN"));
  }

  @Test
  @DisplayName("Dump special doubles which are defined in the JSON schema, but not in JSON")
  void dumpDoubleSpecial() {
    Dump dumper = new Dump(DumpSettings.builder().build());
    assertEquals(".inf\n", dumper.dumpToString(Double.POSITIVE_INFINITY));
    assertEquals(".inf\n", dumper.dumpToString(Float.POSITIVE_INFINITY));

    assertEquals("-.inf\n", dumper.dumpToString(Double.NEGATIVE_INFINITY));
    assertEquals("-.inf\n", dumper.dumpToString(Float.NEGATIVE_INFINITY));

    assertEquals(".nan\n", dumper.dumpToString(Double.NaN));
    assertEquals(".nan\n", dumper.dumpToString(Float.NaN));
  }
}
