/*
 * Copyright (c) 2018, SnakeYAML
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snakeyaml.engine.v2.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.exceptions.EmitterException;

@Tag("fast")
class AnchorTest {

  @Test
  @DisplayName("Anchor cannot be null")
  void testNull() {
    NullPointerException exception =
        assertThrows(NullPointerException.class, () -> new Anchor(null));
    assertNull(exception.getMessage());
  }

  @Test
  @DisplayName("Anchor cannot be empty")
  void testEmpty() {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> new Anchor(""));
    assertEquals("Empty anchor.", exception.getMessage());
  }

  @Test
  @DisplayName("Anchor cannot contain a space")
  void testSpaces() {
    EmitterException exception = assertThrows(EmitterException.class, () -> new Anchor("an chor"));
    assertEquals("Anchor may not contain spaces: an chor", exception.getMessage());
  }

  @Test
  @DisplayName("Anchor cannot contains some characters")
  void testAllInvalid() {
    assertEquals("Invalid character '[' in the anchor: anchor[", checkChar('[').getMessage());
    assertEquals("Invalid character ']' in the anchor: anchor]", checkChar(']').getMessage());
    assertEquals("Invalid character '{' in the anchor: anchor{", checkChar('{').getMessage());
    assertEquals("Invalid character '}' in the anchor: anchor}", checkChar('}').getMessage());
    assertEquals("Invalid character '*' in the anchor: anchor*", checkChar('*').getMessage());
    assertEquals("Invalid character '&' in the anchor: anchor&", checkChar('&').getMessage());
  }

  private EmitterException checkChar(Character ch) {
    return assertThrows(EmitterException.class, () -> new Anchor("anchor" + ch));
  }
}
