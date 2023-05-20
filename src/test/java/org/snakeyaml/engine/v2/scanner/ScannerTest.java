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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.tokens.AnchorToken;
import org.snakeyaml.engine.v2.tokens.ScalarToken;
import org.snakeyaml.engine.v2.tokens.Token;
import org.snakeyaml.engine.v2.tokens.Token.ID;

@org.junit.jupiter.api.Tag("fast")
class ScannerTest {

  // @Test
  @DisplayName("Run scanner")
  void scan() {
    LoadSettings settings = LoadSettings.builder().build();
    String input = "empty block scalar: >\n" + " \n" + "  \n" + "   \n" + " # comment\n";
    StreamReader reader = new StreamReader(settings, input);
    ScannerImpl scanner = new ScannerImpl(settings, reader);
    while (scanner.checkToken()) {
      Token token = scanner.next();
      System.out.println(token);
    }
  }

  @Test
  @DisplayName("Expected NoSuchElementException after all the tokens are finished.")
  void testToString() {
    LoadSettings settings = LoadSettings.builder().build();
    StreamReader reader = new StreamReader(settings, "444222");
    ScannerImpl scanner = new ScannerImpl(settings, reader);
    assertTrue(scanner.hasNext());
    assertEquals(Token.ID.StreamStart, scanner.next().getTokenId());
    assertTrue(scanner.hasNext());
    assertEquals(Token.ID.Scalar, scanner.next().getTokenId());
    assertTrue(scanner.hasNext());
    assertEquals(Token.ID.StreamEnd, scanner.next().getTokenId());
    assertFalse(scanner.hasNext());
    try {
      scanner.next();
      fail("Expected NoSuchElementException");
    } catch (NoSuchElementException e) {
      assertEquals("No more Tokens found.", e.getMessage());
    }
  }

  @Test
  @DisplayName("652Z: ? is part of the key if no space after it")
  void testQuestionMarkStartsToken() {
    Token token = scanTo("{ ?foo: bar }", 3);
    assertEquals(ID.Scalar, token.getTokenId());
    ScalarToken scalar = (ScalarToken) token;
    assertEquals("?foo", scalar.getValue());
  }

  @Test
  @DisplayName("Y2GN: anchor may contain colon ':'")
  void testAnchor() {
    Token token = scanTo("key: &an:chor value", 5);
    assertEquals(ID.Anchor, token.getTokenId());
    AnchorToken anchorToken = (AnchorToken) token;
    assertEquals("an:chor", anchorToken.value.getValue());
  }

  private Token scanTo(String input, int skip) {
    LoadSettings settings = LoadSettings.builder().build();
    StreamReader reader = new StreamReader(settings, input);
    ScannerImpl scanner = new ScannerImpl(settings, reader);
    int i = 0;
    while (i < skip) {
      assertTrue(scanner.hasNext());
      Token token = scanner.next();
      // System.out.println(token);
      assertNotNull(token);
      i++;
    }
    assertTrue(scanner.hasNext());
    return scanner.next();
  }
}
