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
package org.snakeyaml.engine.usecases.inherited;

import org.snakeyaml.engine.v2.common.Anchor;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.scanner.Scanner;
import org.snakeyaml.engine.v2.tokens.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CanonicalScanner implements Scanner {

  public static final Map<Character, String> ESCAPE_REPLACEMENTS = new HashMap();
  private static final String DIRECTIVE = "%YAML 1.2";
  private static final Map<Character, Integer> ESCAPE_CODES = new HashMap();

  static {
    // ASCII null
    ESCAPE_REPLACEMENTS.put(Character.valueOf('0'), "\0");
    // ASCII bell
    ESCAPE_REPLACEMENTS.put(Character.valueOf('a'), "\u0007");
    // ASCII backspace
    ESCAPE_REPLACEMENTS.put(Character.valueOf('b'), "\u0008");
    // ASCII horizontal tab
    ESCAPE_REPLACEMENTS.put(Character.valueOf('t'), "\u0009");
    // ASCII newline (line feed; &#92;n maps to 0x0A)
    ESCAPE_REPLACEMENTS.put(Character.valueOf('n'), "\n");
    // ASCII vertical tab
    ESCAPE_REPLACEMENTS.put(Character.valueOf('v'), "\u000B");
    // ASCII form-feed
    ESCAPE_REPLACEMENTS.put(Character.valueOf('f'), "\u000C");
    // carriage-return (&#92;r maps to 0x0D)
    ESCAPE_REPLACEMENTS.put(Character.valueOf('r'), "\r");
    // ASCII escape character (Esc)
    ESCAPE_REPLACEMENTS.put(Character.valueOf('e'), "\u001B");
    // ASCII space
    ESCAPE_REPLACEMENTS.put(Character.valueOf(' '), "\u0020");
    // ASCII double-quote
    ESCAPE_REPLACEMENTS.put(Character.valueOf('"'), "\"");
    // ASCII backslash
    ESCAPE_REPLACEMENTS.put(Character.valueOf('\\'), "\\");
    // Unicode next line
    ESCAPE_REPLACEMENTS.put(Character.valueOf('N'), "\u0085");
    // Unicode non-breaking-space
    ESCAPE_REPLACEMENTS.put(Character.valueOf('_'), "\u00A0");
    // Unicode line-separator
    ESCAPE_REPLACEMENTS.put(Character.valueOf('L'), "\u2028");
    // Unicode paragraph separator
    ESCAPE_REPLACEMENTS.put(Character.valueOf('P'), "\u2029");

    // 8-bit Unicode
    ESCAPE_CODES.put(Character.valueOf('x'), 2);
    // 16-bit Unicode
    ESCAPE_CODES.put(Character.valueOf('u'), 4);
    // 32-bit Unicode (Supplementary characters are supported)
    ESCAPE_CODES.put(Character.valueOf('U'), 8);
  }

  private final String data;
  private final String label;
  private final Mark mark;
  public ArrayList<Token> tokens;
  private int index;
  private boolean scanned;

  public CanonicalScanner(String data, String label) {
    this.data = data + "\0";
    this.label = label;
    this.index = 0;
    this.tokens = new ArrayList<>();
    this.scanned = false;
    this.mark = new Mark("test", 0, 0, 0, data, 0);
  }

  public boolean checkToken(Token.ID... choices) {
    if (!scanned) {
      scan();
    }
    if (!tokens.isEmpty()) {
      if (choices.length == 0) {
        return true;
      }
      Token first = this.tokens.get(0);
      for (Token.ID choice : choices) {
        if (first.getTokenId() == choice) {
          return true;
        }
      }
    }
    return false;
  }

  public Token peekToken() {
    if (!scanned) {
      scan();
    }
    if (!tokens.isEmpty()) {
      return this.tokens.get(0);
    }
    return null;
  }

  @Override
  public boolean hasNext() {
    return checkToken();
  }

  public Token next() {
    if (!scanned) {
      scan();
    }
    return this.tokens.remove(0);
  }

  @Override
  public void resetDocumentIndex() {
    this.index = 0;
  }

  public Token getToken(Token.ID choice) {
    Token token = next();
    if (choice != null && token.getTokenId() != choice) {
      throw new CanonicalException("unexpected token " + token);
    }
    return token;
  }

  private void scan() {
    this.tokens.add(new StreamStartToken(mark, mark));
    boolean stop = false;
    while (!stop) {
      findToken();
      int c = data.codePointAt(index);
      switch (c) {
        case '\0':
          tokens.add(new StreamEndToken(mark, mark));
          stop = true;
          break;

        case '%':
          tokens.add(scanDirective());
          break;

        case '-':
          if ("---".equals(data.substring(index, index + 3))) {
            index += 3;
            tokens.add(new DocumentStartToken(mark, mark));
          }
          break;

        case '.':
          if ("...".equals(data.substring(index, index + 3))) {
            index += 3;
            tokens.add(new DocumentEndToken(mark, mark));
          }
          break;

        case '[':
          index++;
          tokens.add(new FlowSequenceStartToken(mark, mark));
          break;

        case '{':
          index++;
          tokens.add(new FlowMappingStartToken(mark, mark));
          break;

        case ']':
          index++;
          tokens.add(new FlowSequenceEndToken(mark, mark));
          break;

        case '}':
          index++;
          tokens.add(new FlowMappingEndToken(mark, mark));
          break;

        case '?':
          index++;
          tokens.add(new KeyToken(mark, mark));
          break;

        case ':':
          index++;
          tokens.add(new ValueToken(mark, mark));
          break;

        case ',':
          index++;
          tokens.add(new FlowEntryToken(mark, mark));
          break;

        case '*':
          tokens.add(scanAlias());
          break;

        case '&':
          tokens.add(scanAlias());
          break;

        case '!':
          tokens.add(scanTag());
          break;

        case '"':
          tokens.add(scanScalar());
          break;

        default:
          throw new CanonicalException(
              "invalid token: " + Character.valueOf((char) c) + " in " + label);
      }
    }
    scanned = true;
  }

  private Token scanDirective() {
    String chunk1 = data.substring(index, index + DIRECTIVE.length());
    char chunk2 = data.charAt(index + DIRECTIVE.length());
    if (DIRECTIVE.equals(chunk1) && "\n\0".indexOf(chunk2) != -1) {
      index += DIRECTIVE.length();
      final DirectiveToken.YamlDirective implicit = new DirectiveToken.YamlDirective(1, 1);
      return new DirectiveToken(implicit, mark, mark);
    } else {
      throw new CanonicalException("invalid directive: " + chunk1 + " " + chunk2 + " in " + label);
    }
  }

  private Token scanAlias() {
    boolean isTokenClassAlias;
    final int c = data.codePointAt(index);
    isTokenClassAlias = c == '*';
    index += Character.charCount(c);
    int start = index;
    while (", \n\0".indexOf(data.charAt(index)) == -1) {
      index++;
    }
    String value = data.substring(start, index);
    Token token;
    if (isTokenClassAlias) {
      token = new AliasToken(new Anchor(value), mark, mark);
    } else {
      token = new AnchorToken(new Anchor(value), mark, mark);
    }
    return token;
  }

  private Token scanTag() {
    index += Character.charCount(data.codePointAt(index));
    int start = index;
    while (" \n\0".indexOf(data.charAt(index)) == -1) {
      index++;
    }
    String value = data.substring(start, index);
    if (value.length() == 0) {
      value = "!";
    } else if (value.charAt(0) == '!') {
      value = Tag.PREFIX + value.substring(1);
    } else if (value.charAt(0) == '<' && value.charAt(value.length() - 1) == '>') {
      value = value.substring(1, value.length() - 1);
    } else {
      value = "!" + value;
    }
    return new TagToken(new TagTuple("", value), mark, mark);
  }

  private Token scanScalar() {
    index += Character.charCount(data.codePointAt(index));
    StringBuilder chunks = new StringBuilder();
    int start = index;
    boolean ignoreSpaces = false;
    while (data.charAt(index) != '"') {
      if (data.charAt(index) == '\\') {
        ignoreSpaces = false;
        chunks.append(data, start, index);
        index += Character.charCount(data.codePointAt(index));
        int c = data.codePointAt(index);
        index += Character.charCount(data.codePointAt(index));
        if (c == '\n') {
          ignoreSpaces = true;
        } else if (!Character.isSupplementaryCodePoint(c) && ESCAPE_CODES.containsKey((char) c)) {
          int length = ESCAPE_CODES.get((char) c);
          int code = Integer.parseInt(data.substring(index, index + length), 16);
          chunks.append((char) code);
          index += length;
        } else {
          if (Character.isSupplementaryCodePoint(c) || !ESCAPE_REPLACEMENTS.containsKey((char) c)) {
            throw new CanonicalException("invalid escape code");
          }
          chunks.append(ESCAPE_REPLACEMENTS.get((char) c));
        }
        start = index;
      } else if (data.charAt(index) == '\n') {
        chunks.append(data, start, index);
        chunks.append(" ");
        index += Character.charCount(data.codePointAt(index));
        start = index;
        ignoreSpaces = true;
      } else if (ignoreSpaces && data.charAt(index) == ' ') {
        index += Character.charCount(data.codePointAt(index));
        start = index;
      } else {
        ignoreSpaces = false;
        index += Character.charCount(data.codePointAt(index));
      }
    }
    chunks.append(data, start, index);
    index += Character.charCount(data.codePointAt(index));
    return new ScalarToken(chunks.toString(), false, mark, mark);
  }

  private void findToken() {
    boolean found = false;
    while (!found) {
      while (" \t".indexOf(data.charAt(index)) != -1) {
        index++;
      }
      if (data.charAt(index) == '#') {
        while (data.charAt(index) != '\n') {
          index++;
        }
      }
      if (data.charAt(index) == '\n') {
        index++;
      } else {
        found = true;
      }
    }
  }
}
