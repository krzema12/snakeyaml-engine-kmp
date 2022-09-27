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
package org.snakeyaml.engine.usecases.references;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;

/**
 * https://en.wikipedia.org/wiki/Billion_laughs_attack#Variations
 */
@Tag("fast")
public class BillionLaughsAttackTest {

  public static final String data =
      "a: &a [\"lol\",\"lol\",\"lol\",\"lol\",\"lol\",\"lol\",\"lol\",\"lol\",\"lol\"]\n"
          + "b: &b [*a,*a,*a,*a,*a,*a,*a,*a,*a]\n" + "c: &c [*b,*b,*b,*b,*b,*b,*b,*b,*b]\n"
          + "d: &d [*c,*c,*c,*c,*c,*c,*c,*c,*c]\n" + "e: &e [*d,*d,*d,*d,*d,*d,*d,*d,*d]\n"
          + "f: &f [*e,*e,*e,*e,*e,*e,*e,*e,*e]\n" + "g: &g [*f,*f,*f,*f,*f,*f,*f,*f,*f]\n"
          + "h: &h [*g,*g,*g,*g,*g,*g,*g,*g,*g]\n" + "i: &i [*h,*h,*h,*h,*h,*h,*h,*h,*h]";

  public static final String scalarAliasesData =
      "a: &a foo\n" + "b:  *a\n" + "c:  *a\n" + "d:  *a\n" + "e:  *a\n" + "f:  *a\n" + "g:  *a\n";

  @Test
  @DisplayName("Load many aliases if explicitly allowed")
  public void billionLaughsAttackLoaded() {
    LoadSettings settings = LoadSettings.builder().setMaxAliasesForCollections(72).build();
    Load load = new Load(settings);
    Map map = (Map) load.loadFromString(data);
    assertNotNull(map);
  }

  @Test
  @DisplayName("Billion_laughs_attack if data expanded")
  public void billionLaughsAttackExpanded() {
    LoadSettings settings = LoadSettings.builder().setMaxAliasesForCollections(100).build();
    Load load = new Load(settings);
    Map map = (Map) load.loadFromString(data);
    assertNotNull(map);
    try {
      map.toString();
      fail("Expected overflow");
    } catch (Throwable e) {
      assertTrue(e.getMessage().contains("heap"));
    }
  }

  @Test
  @DisplayName("Prevent Billion_laughs_attack by default")
  public void billionLaughsAttackWithRestrictedAliases() {
    LoadSettings settings = LoadSettings.builder().build();
    Load load = new Load(settings);
    try {
      load.loadFromString(data);
      fail();
    } catch (YamlEngineException e) {
      assertEquals("Number of aliases for non-scalar nodes exceeds the specified max=50",
          e.getMessage());
    }
  }

  @Test
  @DisplayName("Number of aliases for scalar nodes is not restricted")
  public void doNotRestrictScalarAliases() {
    // smaller than number of aliases for scalars
    LoadSettings settings = LoadSettings.builder().setMaxAliasesForCollections(5).build();
    Load load = new Load(settings);
    load.loadFromString(scalarAliasesData);
  }
}
