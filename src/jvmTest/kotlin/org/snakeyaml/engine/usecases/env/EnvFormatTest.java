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
package org.snakeyaml.engine.usecases.env;

import org.junit.jupiter.api.Test;
import it.krzeminski.snakeyaml.engine.kmp.resolver.BaseScalarResolver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@code ${VARIABLE:-default}} evaluates to default if {@code VARIABLE} is unset or empty in the environment.
 * {@code ${VARIABLE-default}} evaluates to default only if VARIABLE is unset in the environment.
 * <p>
 * Similarly, the following syntax allows you to specify mandatory variables:
 * <p>
 * {@code ${VARIABLE:?err}} exits with an error message containing err if {@code VARIABLE} is unset or empty in the
 * environment. {@code ${VARIABLE?err}} exits with an error message containing err if {@code VARIABLE} is unset in
 * the environment.
 */
@org.junit.jupiter.api.Tag("fast")
public class EnvFormatTest {

  public static final Pattern ENV_FORMAT = BaseScalarResolver.ENV_FORMAT.toPattern();

  @Test
  public void testMatchBasic() {
    assertTrue(ENV_FORMAT.matcher("${V}").matches());
    assertTrue(ENV_FORMAT.matcher("${PATH}").matches());
    assertTrue(ENV_FORMAT.matcher("${VARIABLE}").matches());
    assertTrue(ENV_FORMAT.matcher("${ VARIABLE}").matches());
    assertTrue(ENV_FORMAT.matcher("${ VARIABLE}").matches());
    assertTrue(ENV_FORMAT.matcher("${ VARIABLE }").matches());
    assertTrue(ENV_FORMAT.matcher("${ VARIABLE}").matches());
    assertTrue(ENV_FORMAT.matcher("${\tVARIABLE  }").matches());

    Matcher matcher = ENV_FORMAT.matcher("${VARIABLE}");
    assertTrue(matcher.matches(), "expect that ENV_FORMAT matches '${VARIABLE}'");
    assertEquals("VARIABLE", matcher.group(1));
    assertNull(matcher.group(2));
    assertNull(matcher.group(3));

    assertFalse(ENV_FORMAT.matcher("${VARI ABLE}").matches());
  }

  @Test
  public void testMatchDefault() {
    assertTrue(ENV_FORMAT.matcher("${VARIABLE-default}").matches());
    assertTrue(ENV_FORMAT.matcher("${ VARIABLE-default}").matches());
    assertTrue(ENV_FORMAT.matcher("${ VARIABLE-default }").matches());
    assertTrue(ENV_FORMAT.matcher("${ VARIABLE-default}").matches());
    assertTrue(ENV_FORMAT.matcher("${ VARIABLE-}").matches());

    Matcher matcher = ENV_FORMAT.matcher("${VARIABLE-default}");
    assertTrue(matcher.matches(), "expect that ENV_FORMAT matches '${VARIABLE-default}'");
    assertEquals("VARIABLE", matcher.group(1));
    assertEquals("-", matcher.group(2));
    assertEquals("default", matcher.group(3));

    assertFalse(ENV_FORMAT.matcher("${VARIABLE -default}").matches());
    assertFalse(ENV_FORMAT.matcher("${VARIABLE - default}").matches());
    assertFalse(ENV_FORMAT.matcher("${VARIABLE -default}").matches());
  }

  @Test
  public void testMatchDefaultOrEmpty() {
    assertTrue(ENV_FORMAT.matcher("${VARIABLE:-default}").matches());
    assertTrue(ENV_FORMAT.matcher("${ VARIABLE:-default }").matches());
    assertTrue(ENV_FORMAT.matcher("${ VARIABLE:-}").matches());

    Matcher matcher = ENV_FORMAT.matcher("${VARIABLE:-default}");
    assertTrue(matcher.matches(), "expect that ENV_FORMAT matches '${VARIABLE:-default}'");
    assertEquals("VARIABLE", matcher.group(1));
    assertEquals(":-", matcher.group(2));
    assertEquals("default", matcher.group(3));

    assertFalse(ENV_FORMAT.matcher("${VARIABLE :-default}").matches());
    assertFalse(ENV_FORMAT.matcher("${VARIABLE : -default}").matches());
    assertFalse(ENV_FORMAT.matcher("${VARIABLE : - default}").matches());
  }

  @Test
  public void testMatchErrorDefaultOrEmpty() {
    assertTrue(ENV_FORMAT.matcher("${VARIABLE:?err}").matches());
    assertTrue(ENV_FORMAT.matcher("${ VARIABLE:?err }").matches());
    assertTrue(ENV_FORMAT.matcher("${ VARIABLE:? }").matches());

    Matcher matcher = ENV_FORMAT.matcher("${VARIABLE:?err}");
    assertTrue(matcher.matches(), "expect that ENV_FORMAT matches '${VARIABLE:?err}'");
    assertEquals("VARIABLE", matcher.group(1));
    assertEquals(":?", matcher.group(2));
    assertEquals("err", matcher.group(3));

    assertFalse(ENV_FORMAT.matcher("${ VARIABLE :?err }").matches());
    assertFalse(ENV_FORMAT.matcher("${ VARIABLE : ?err }").matches());
    assertFalse(ENV_FORMAT.matcher("${ VARIABLE : ? err }").matches());
  }

  @Test
  public void testMatchErrorDefault() {
    assertTrue(ENV_FORMAT.matcher("${VARIABLE?err}").matches());
    assertTrue(ENV_FORMAT.matcher("${ VARIABLE:?err }").matches());
    assertTrue(ENV_FORMAT.matcher("${ VARIABLE:?}").matches());

    Matcher matcher = ENV_FORMAT.matcher("${ VARIABLE?err }");
    assertTrue(matcher.matches(), "expect that ENV_FORMAT matches '${ VARIABLE?err }'");
    assertEquals("VARIABLE", matcher.group(1));
    assertEquals("?", matcher.group(2));
    assertEquals("err", matcher.group(3));

    assertFalse(ENV_FORMAT.matcher("${ VARIABLE ?err }").matches());
    assertFalse(ENV_FORMAT.matcher("${ VARIABLE ?err }").matches());
    assertFalse(ENV_FORMAT.matcher("${ VARIABLE ? err }").matches());
  }
}
