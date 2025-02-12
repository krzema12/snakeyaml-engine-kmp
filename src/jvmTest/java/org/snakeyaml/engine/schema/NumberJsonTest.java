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

import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings;
import it.krzeminski.snakeyaml.engine.kmp.schema.JsonSchema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.api.Dump;
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings;
import it.krzeminski.snakeyaml.engine.kmp.api.Load;

@org.junit.jupiter.api.Tag("fast")
public class NumberJsonTest {

  private final Load loader = new Load(LoadSettings.builder().setSchema(new JsonSchema()).build());

  @Test
  @DisplayName("Test all integers which are define in the core schema & JSON")
  void parseInteger() {
    assertEquals(Integer.valueOf(1), loader.loadOne("1"));
    assertEquals(Integer.valueOf(-1), loader.loadOne("-1"));
    assertEquals(Integer.valueOf(0), loader.loadOne("0"));
    assertEquals(Integer.valueOf(0), loader.loadOne("-0"));
    assertEquals("012", loader.loadOne("012"), "Leading zeros are not allowed.");
    assertEquals(Integer.valueOf(1234567890), loader.loadOne("1234567890"));
    assertEquals(Long.valueOf(12345678901L), loader.loadOne("12345678901"));
    assertEquals(new BigInteger("1234567890123456789123"),
        loader.loadOne("1234567890123456789123"));
  }

  @Test
  @DisplayName("Test all strings which WERE integers or doubles in YAML 1.1")
  void parseString() {
    assertEquals("12:10:02", loader.loadOne("12:10:02"));
    assertEquals("0b1010", loader.loadOne("0b1010"));
    assertEquals("0xFF", loader.loadOne("0xFF"));
    assertEquals("1_000", loader.loadOne("1_000"));

    assertEquals("1_000.5", loader.loadOne("1_000.5"));
    assertEquals("+.inf", loader.loadOne("+.inf"));

    // start with +
    assertEquals("+1", loader.loadOne("+1"));
    assertEquals("+1223344", loader.loadOne("+1223344"));
    assertEquals("+12.23344", loader.loadOne("+12.23344"));
    assertEquals("+0.23344", loader.loadOne("+0.23344"));
    assertEquals("+0", loader.loadOne("+0"));

    // leading zero
    assertEquals("03", loader.loadOne("03"));
    assertEquals("03.67", loader.loadOne("03.67"));
    assertEquals("3.6", loader.loadOne("! 3.6"));
    assertEquals("3", loader.loadOne("! 3"));
  }

  @Test
  @DisplayName("Test all doubles which are define in the core schema & JSON")
  void parseDouble() {
    assertEquals(Double.valueOf(-1.345), loader.loadOne("-1.345"));
    assertEquals(Double.valueOf(0), loader.loadOne("0.0"));
    assertEquals(Double.valueOf(0f), loader.loadOne("0.0"));
    assertEquals(Double.valueOf(0d), loader.loadOne("0.0"));
    assertEquals(Double.valueOf(+0), loader.loadOne("0.0"));
    assertEquals(Double.valueOf(-0.0), loader.loadOne("-0.0"));
    assertEquals(Double.valueOf(0.123), loader.loadOne("0.123"));
    assertEquals(Double.valueOf(1.23E-6), loader.loadOne("1.23e-6"));
    assertEquals(Double.valueOf(1.23E6), loader.loadOne("1.23e+6"));
    assertEquals(Double.valueOf(1.23E6), loader.loadOne("1.23e6"));
    assertEquals(Double.valueOf(1.23), loader.loadOne("1.23E0"));
    assertEquals(Double.valueOf(-1.23E6), loader.loadOne("-1.23e6"));
    assertEquals(Double.valueOf(1000.25), loader.loadOne("1000.25"));
    assertEquals(Double.valueOf(9000.0), loader.loadOne("9000.00"));
    assertEquals(Double.valueOf(1.0), loader.loadOne("1."));
  }

  @Test
  @DisplayName("Parse special doubles which are defined in the JSON schema, but not in JSON")
  void parseDoubleSpecial() {
    assertEquals(Double.POSITIVE_INFINITY, loader.loadOne(".inf"));
    assertEquals(Double.NEGATIVE_INFINITY, loader.loadOne("-.inf"));
    assertEquals(Double.NaN, loader.loadOne(".nan"));

    assertEquals(".INF", loader.loadOne(".INF"));
    assertEquals(".NAN", loader.loadOne(".NAN"));
  }

  @Test
  @DisplayName("Dump special doubles which are defined in the JSON schema, but not in JSON")
  void dumpDoubleSpecial() {
    Dump dumper = new Dump(DumpSettings.builder().setSchema(new JsonSchema()).build());
    assertEquals(".inf\n", dumper.dumpToString(Double.POSITIVE_INFINITY));
    assertEquals(".inf\n", dumper.dumpToString(Float.POSITIVE_INFINITY));

    assertEquals("-.inf\n", dumper.dumpToString(Double.NEGATIVE_INFINITY));
    assertEquals("-.inf\n", dumper.dumpToString(Float.NEGATIVE_INFINITY));

    assertEquals(".nan\n", dumper.dumpToString(Double.NaN));
    assertEquals(".nan\n", dumper.dumpToString(Float.NaN));
  }
}
