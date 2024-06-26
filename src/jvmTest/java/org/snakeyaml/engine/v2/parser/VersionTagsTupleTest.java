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
package org.snakeyaml.engine.v2.parser;

import com.google.common.collect.ImmutableMap;
import it.krzeminski.snakeyaml.engine.kmp.parser.VersionTagsTuple;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("fast")
class VersionTagsTupleTest {

  @Test
  void testToString() {
    VersionTagsTuple tuple = new VersionTagsTuple(null, ImmutableMap.of());
    assertEquals("VersionTagsTuple<null, {}>", tuple.toString());
  }
}
