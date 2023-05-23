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

import kotlin.NotImplementedError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.comments.CommentType;
import org.snakeyaml.engine.v2.common.CharConstants;
import org.snakeyaml.engine.v2.common.UriEncoder;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.ScannerException;
import org.snakeyaml.engine.v2.tokens.CommentToken;
import org.snakeyaml.engine.v2.tokens.DirectiveToken;
import org.snakeyaml.engine.v2.tokens.Token;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.*;
import java.util.regex.Pattern;

import static org.snakeyaml.engine.v2.common.CharConstants.ESCAPE_CODES;
import static org.snakeyaml.engine.v2.common.CharConstants.ESCAPE_REPLACEMENTS;

final class ScannerImplJava implements Scanner {

  static final String DIRECTIVE_PREFIX = "while scanning a directive";
  static final String EXPECTED_ALPHA_ERROR_PREFIX =
      "expected alphabetic or numeric character, but found ";
  static final String SCANNING_SCALAR = "while scanning a block scalar";
  static final String SCANNING_PREFIX = "while scanning a ";
  /**
   * A regular expression matching characters which are not in the hexadecimal set (0-9, A-F, a-f).
   */
  static final Pattern NOT_HEXA = Pattern.compile("[^0-9A-Fa-f]");

  final StreamReader reader;
  /**
   * List of processed tokens that are not yet emitted.
   */
  final List<Token> tokens;
  /**
   * Past indentation levels.
   */
  final kotlin.collections.ArrayDeque<Integer> indents;
  /**
   * Keep track of possible simple keys. This is a dictionary. The key is `flow_level`; there can be
   * no more than one possible simple key for each level. The value is a SimpleKey record:
   * (token_number, required, index, line, column, mark) A simple key may start with ALIAS, ANCHOR,
   * TAG, SCALAR(flow), '[', or '{' tokens.
   */
  final Map<Integer, SimpleKey> possibleSimpleKeys;
  final LoadSettings settings;
  /**
   * Had we reached the end of the stream?
   */
  boolean done = false;
  /**
   * The number of unclosed '{' and '['. `isBlockContext()` means block context.
   */
  int flowLevel = 0;
  /**
   * The last added token
   */
  Token lastToken;

  /**
   * Variables related to simple keys treatment.
   * Number of tokens that were emitted through the `get_token` method.
   */
  int tokensTaken = 0;
  /**
   * The current indentation level.
   */
  int indent = -1;
  /**
   * <pre>
   * A simple key is a key that is not denoted by the '?' indicator.
   * Example of simple keys:
   *   ---
   *   block simple key: value
   *   ? not a simple key:
   *   : { flow simple key: value }
   * We emit the KEY token before all keys, so when we find a potential
   * simple key, we try to locate the corresponding ':' indicator.
   * Simple keys should be limited to a single line and 1024 characters.
   *
   * Can a simple key start at the current position? A simple key may
   * start:
   * - at the beginning of the line, not counting indentation spaces
   *       (in block context),
   * - after '{', '[', ',' (in the flow context),
   * - after '?', ':', '-' (in the block context).
   * In the block context, this flag also signifies if a block collection
   * may start at the current position.
   * </pre>
   */
  boolean allowSimpleKey = true;

  /**
   * Create
   *
   * @param settings - configurable options
   * @param reader   - the input
   */
  public ScannerImplJava(LoadSettings settings, StreamReader reader) {
    this.reader = reader;
    this.settings = settings;
    this.tokens = new ArrayList<>(100);
    this.indents = new kotlin.collections.ArrayDeque<>(10);
    // The order in possibleSimpleKeys is kept for nextPossibleSimpleKey()
    this.possibleSimpleKeys = new LinkedHashMap<>();
    //fetchStreamStart();// Add the STREAM-START token.
  }

  public boolean checkToken(@NotNull Token.ID... choices) {
    throw new NotImplementedError("converted to Kotlin");
  }

  @NotNull
  public Token peekToken() {
    throw new NotImplementedError("converted to Kotlin");
  }

  @Override
  public boolean hasNext() {
    throw new NotImplementedError("converted to Kotlin");
  }

  @NotNull
  public Token next() {
    throw new NotImplementedError("converted to Kotlin");
  }


  boolean isBlockContext() {
    return this.flowLevel == 0;
  }


  CommentToken scanComment(CommentType type) {
    // See the specification for details.
    Optional<Mark> startMark = reader.getMark();
    reader.forward();
    int length = 0;
    while (CharConstants.NULL_OR_LINEBR.hasNo(reader.peek(length))) {
      length++;
    }
    String value = reader.prefixForward(length);
    Optional<Mark> endMark = reader.getMark();
    return new CommentToken(type, value, startMark, endMark);
  }

  List<Token> scanDirective() {
    // See the specification for details.
    Optional<Mark> startMark = reader.getMark();
    Optional<Mark> endMark;
    reader.forward();
    String name = scanDirectiveName(startMark);
    Optional<List<?>> value;
    if (DirectiveToken.YAML_DIRECTIVE.equals(name)) {
      value = Optional.of(scanYamlDirectiveValue(startMark));
      endMark = reader.getMark();
    } else if (DirectiveToken.TAG_DIRECTIVE.equals(name)) {
      value = Optional.of(scanTagDirectiveValue(startMark));
      endMark = reader.getMark();
    } else {
      endMark = reader.getMark();
      int ff = 0;
      while (CharConstants.NULL_OR_LINEBR.hasNo(reader.peek(ff))) {
        ff++;
      }
      if (ff > 0) {
        reader.forward(ff);
      }
      value = Optional.empty();
    }
    CommentToken commentToken = scanDirectiveIgnoredLine(startMark);
    DirectiveToken token = new DirectiveToken(name, value, startMark, endMark);
    return makeTokenList(token, commentToken);
  }

  /**
   * Scan a directive name. Directive names are a series of non-space characters.
   */
  String scanDirectiveName(Optional<Mark> startMark) {
    // See the specification for details.
    int length = 0;
    // A Directive-name is a sequence of alphanumeric characters
    // (a-z,A-Z,0-9). We scan until we find something that isn't.
    // This disagrees with the specification.
    int c = reader.peek(length);
    while (CharConstants.ALPHA.has(c)) {
      length++;
      c = reader.peek(length);
    }
    // If the peeked name is empty, an error occurs.
    if (length == 0) {
      final String s = String.valueOf(Character.toChars(c));
      throw new ScannerException(DIRECTIVE_PREFIX, startMark,
          EXPECTED_ALPHA_ERROR_PREFIX + s + "(" + c + ")", reader.getMark());
    }
    String value = reader.prefixForward(length);
    c = reader.peek();
    if (CharConstants.NULL_BL_LINEBR.hasNo(c)) {
      final String s = String.valueOf(Character.toChars(c));
      throw new ScannerException(DIRECTIVE_PREFIX, startMark,
          EXPECTED_ALPHA_ERROR_PREFIX + s + "(" + c + ")", reader.getMark());
    }
    return value;
  }

  List<Integer> scanYamlDirectiveValue(Optional<Mark> startMark) {
    // See the specification for details.
    while (reader.peek() == ' ') {
      reader.forward();
    }
    Integer major = scanYamlDirectiveNumber(startMark);
    int c = reader.peek();
    if (c != '.') {
      final String s = String.valueOf(Character.toChars(c));
      throw new ScannerException(DIRECTIVE_PREFIX, startMark,
          "expected a digit or '.', but found " + s + "(" + c + ")", reader.getMark());
    }
    reader.forward();
    Integer minor = scanYamlDirectiveNumber(startMark);
    c = reader.peek();
    if (CharConstants.NULL_BL_LINEBR.hasNo(c)) {
      final String s = String.valueOf(Character.toChars(c));
      throw new ScannerException(DIRECTIVE_PREFIX, startMark,
          "expected a digit or ' ', but found " + s + "(" + c + ")", reader.getMark());
    }
    List<Integer> result = new ArrayList<>(2);
    result.add(major);
    result.add(minor);
    return result;
  }

  /**
   * Read a %YAML directive number: this is either the major or the minor part. Stop reading at a
   * non-digit character (usually either '.' or '\n').
   */
  Integer scanYamlDirectiveNumber(Optional<Mark> startMark) {
    // See the specification for details.
    int c = reader.peek();
    if (!Character.isDigit(c)) {
      final String s = String.valueOf(Character.toChars(c));
      throw new ScannerException(DIRECTIVE_PREFIX, startMark,
          "expected a digit, but found " + s + "(" + (c) + ")", reader.getMark());
    }
    int length = 0;
    while (Character.isDigit(reader.peek(length))) {
      length++;
    }
    String number = reader.prefixForward(length);
    if (length > 3) {
      throw new ScannerException("while scanning a YAML directive", startMark,
          "found a number which cannot represent a valid version: " + number, reader.getMark());
    }
    return Integer.parseInt(number);
  }

  /**
   * <p>
   * Read a %TAG directive value:
   * <p>
   *
   * <pre>
   * s-ignored-space+ c-tag-handle s-ignored-space+ ns-tag-prefix s-l-comments
   * </pre>
   * <p>
   * </p>
   */
  List<String> scanTagDirectiveValue(Optional<Mark> startMark) {
    // See the specification for details.
    while (reader.peek() == ' ') {
      reader.forward();
    }
    String handle = scanTagDirectiveHandle(startMark);
    while (reader.peek() == ' ') {
      reader.forward();
    }
    String prefix = scanTagDirectivePrefix(startMark);
    List<String> result = new ArrayList<>(2);
    result.add(handle);
    result.add(prefix);
    return result;
  }

  /**
   * Scan a %TAG directive's handle. This is YAML's c-tag-handle.
   *
   * @param startMark - start
   * @return the directive value
   */
  String scanTagDirectiveHandle(Optional<Mark> startMark) {
    // See the specification for details.
    String value = scanTagHandle("directive", startMark);
    int c = reader.peek();
    if (c != ' ') {
      final String s = String.valueOf(Character.toChars(c));
      throw new ScannerException(DIRECTIVE_PREFIX, startMark,
          "expected ' ', but found " + s + "(" + c + ")", reader.getMark());
    }
    return value;
  }

  /**
   * Scan a %TAG directive's prefix. This is YAML's ns-tag-prefix.
   */
  String scanTagDirectivePrefix(Optional<Mark> startMark) {
    // See the specification for details.
    String value = scanTagUri("directive", CharConstants.URI_CHARS_FOR_TAG_PREFIX, startMark);
    int c = reader.peek();
    if (CharConstants.NULL_BL_LINEBR.hasNo(c)) {
      final String s = String.valueOf(Character.toChars(c));
      throw new ScannerException(DIRECTIVE_PREFIX, startMark,
          "expected ' ', but found " + s + "(" + c + ")", reader.getMark());
    }
    return value;
  }

  CommentToken scanDirectiveIgnoredLine(Optional<Mark> startMark) {
    // See the specification for details.
    while (reader.peek() == ' ') {
      reader.forward();
    }
    CommentToken commentToken = null;
    if (reader.peek() == '#') {
      CommentToken comment = scanComment(CommentType.IN_LINE);
      if (settings.parseComments) {
        commentToken = comment;
      }
    }
    int c = reader.peek();
    if (!scanLineBreak().isPresent() && c != 0) {
      final String s = String.valueOf(Character.toChars(c));
      throw new ScannerException(DIRECTIVE_PREFIX, startMark,
          "expected a comment or a line break, but found " + s + "(" + c + ")", reader.getMark());
    }
    return commentToken;
  }

  /**
   * <p>
   * Scan a Tag handle. A Tag handle takes one of three forms:
   * <p>
   *
   * <pre>
   * "!" (c-primary-tag-handle)
   * "!!" (ns-secondary-tag-handle)
   * "!(name)!" (c-named-tag-handle)
   * </pre>
   * <p>
   * Where (name) must be formatted as an ns-word-char.
   * </p>
   *
   *
   * <pre>
   * See the specification for details.
   * For some strange reasons, the specification does not allow '_' in
   * tag handles. I have allowed it anyway.
   * </pre>
   */
  String scanTagHandle(String name, Optional<Mark> startMark) {
    int c = reader.peek();
    if (c != '!') {
      final String s = String.valueOf(Character.toChars(c));
      throw new ScannerException(SCANNING_PREFIX + name, startMark,
          "expected '!', but found " + s + "(" + (c) + ")", reader.getMark());
    }
    // Look for the next '!' in the stream, stopping if we hit a
    // non-word-character. If the first character is a space, then the
    // tag-handle is a c-primary-tag-handle ('!').
    int length = 1;
    c = reader.peek(length);
    if (c != ' ') {
      // Scan through 0+ alphabetic characters.
      // According to the specification, these should be
      // ns-word-char only, which prohibits '_'. This might be a
      // candidate for a configuration option.
      while (CharConstants.ALPHA.has(c)) {
        length++;
        c = reader.peek(length);
      }
      // Found the next non-word-char. If this is not a space and not an
      // '!', then this is an error, as the tag-handle was specified as:
      // !(name) or similar; the trailing '!' is missing.
      if (c != '!') {
        reader.forward(length);
        final String s = String.valueOf(Character.toChars(c));
        throw new ScannerException(SCANNING_PREFIX + name, startMark,
            "expected '!', but found " + s + "(" + (c) + ")", reader.getMark());
      }
      length++;
    }
    return reader.prefixForward(length);
  }

  /**
   * Scan a Tag URI. This scanning is valid for both local and global tag directives, because both
   * appear to be valid URIs as far as scanning is concerned. The difference may be distinguished
   * later, in parsing. This method will scan for ns-uri-char*, which covers both cases.
   * <p>
   * This method performs no verification that the scanned URI conforms to any particular kind of
   * URI specification.
   */
  String scanTagUri(String name, CharConstants range, Optional<Mark> startMark) {
    // See the specification for details.
    // Note: we do not check if URI is well-formed.
    StringBuilder chunks = new StringBuilder();
    // Scan through accepted URI characters, which includes the standard
    // URI characters, plus the start-escape character ('%'). When we get
    // to a start-escape, scan the escaped sequence, then return.
    int length = 0;
    int c = reader.peek(length);
    while (range.has(c)) {
      if (c == '%') {
        chunks.append(reader.prefixForward(length));
        length = 0;
        chunks.append(scanUriEscapes(name, startMark));
      } else {
        length++;
      }
      c = reader.peek(length);
    }
    // Consume the last "chunk", which would not otherwise be consumed by
    // the loop above.
    if (length != 0) {
      chunks.append(reader.prefixForward(length));
    }
    if (chunks.length() == 0) {
      // If no URI was found, an error has occurred.
      final String s = String.valueOf(Character.toChars(c));
      throw new ScannerException(SCANNING_PREFIX + name, startMark,
          "expected URI, but found " + s + "(" + (c) + ")", reader.getMark());
    }
    return chunks.toString();
  }

  /**
   * <p>
   * Scan a sequence of %-escaped URI escape codes and convert them into a String representing the
   * unescaped values.
   * </p>
   * <p>
   * This method fails for more than 256 bytes' worth of URI-encoded characters in a row. Is this
   * possible? Is this a use-case?
   */
  String scanUriEscapes(String name, Optional<Mark> startMark) {
    // First, look ahead to see how many URI-escaped characters we should
    // expect, so we can use the correct buffer size.
    int length = 1;
    while (reader.peek(length * 3) == '%') {
      length++;
    }
    // See the specification for details.
    // URIs containing 16 and 32 bit Unicode characters are
    // encoded in UTF-8, and then each octet is written as a
    // separate character.
    Optional<Mark> beginningMark = reader.getMark();
    ByteBuffer buff = ByteBuffer.allocate(length);
    while (reader.peek() == '%') {
      reader.forward();
      try {
        byte code = (byte) Integer.parseInt(reader.prefix(2), 16);
        buff.put(code);
      } catch (NumberFormatException nfe) {
        int c1 = reader.peek();
        final String s1 = String.valueOf(Character.toChars(c1));
        int c2 = reader.peek(1);
        final String s2 = String.valueOf(Character.toChars(c2));
        throw new ScannerException(SCANNING_PREFIX + name, startMark,
            "expected URI escape sequence of 2 hexadecimal numbers, but found " + s1 + "(" + c1
                + ") and " + s2 + "(" + c2 + ")",
            reader.getMark());
      }
      reader.forward(2);
    }
    buff.flip();
    try {
      return UriEncoder.decode(buff);
    } catch (CharacterCodingException e) {
      throw new ScannerException(SCANNING_PREFIX + name, startMark,
          "expected URI in UTF-8: " + e.getMessage(), beginningMark);
    }
  }

  /**
   * Scan a line break, transforming:
   *
   * <pre>
   * '\r\n'   : '\n'
   * '\r'     : '\n'
   * '\n'     : '\n'
   * '\x85'   : '\n'
   * '\u2028' : '\u2028'
   * '\u2029  : '\u2029'
   * default : ''
   * </pre>
   *
   * @return transformed character or empty string if no line break detected
   */
  Optional<String> scanLineBreak() {
    int c = reader.peek();
    if (c == '\r' || c == '\n' || c == '\u0085') {
      if (c == '\r' && '\n' == reader.peek(1)) {
        reader.forward(2);
      } else {
        reader.forward();
      }
      return Optional.of("\n");
    } else if (c == '\u2028' || c == '\u2029') {
      reader.forward();
      return Optional.of(String.valueOf(Character.toChars(c)));
    }
    return Optional.empty();
  }

  //endregion

  /**
   * Ignore Comment token if they are null, or Comments should not be parsed
   *
   * @param tokens - token types
   * @return tokens to be used
   */
  List<Token> makeTokenList(Token... tokens) {
    List<Token> tokenList = new ArrayList<>();
    for (Token token : tokens) {
      if (token == null) {
        continue;
      }
      if (!settings.parseComments && (token instanceof CommentToken)) {
        continue;
      }
      tokenList.add(token);
    }
    return tokenList;
  }
  //endregion

  @Override
  public void resetDocumentIndex() {
    this.reader.resetDocumentIndex();
  }

}
