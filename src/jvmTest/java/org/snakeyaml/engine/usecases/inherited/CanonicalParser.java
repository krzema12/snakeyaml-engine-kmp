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

import org.jetbrains.annotations.NotNull;
import it.krzeminski.snakeyaml.engine.kmp.common.Anchor;
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle;
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle;
import it.krzeminski.snakeyaml.engine.kmp.common.SpecVersion;
import it.krzeminski.snakeyaml.engine.kmp.events.*;
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag;
import it.krzeminski.snakeyaml.engine.kmp.parser.Parser;
import it.krzeminski.snakeyaml.engine.kmp.tokens.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.NoSuchElementException;

public class CanonicalParser implements Parser {

  private final String label;
  private final ArrayList<Event> events = new ArrayList<>();
  private final CanonicalScanner scanner;
  private boolean parsed = false;

  public CanonicalParser(String data, String label) {
    this.label = label;
    scanner = new CanonicalScanner(data, label);
  }

  // stream: STREAM-START document* STREAM-END
  private void parseStream() {
    scanner.getToken(Token.ID.StreamStart);
    events.add(new StreamStartEvent(null, null));
    while (!scanner.checkToken(Token.ID.StreamEnd)) {
      if (scanner.checkToken(Token.ID.Directive, Token.ID.DocumentStart)) {
        parseDocument();
      } else {
        throw new CanonicalException(
            "document is expected, got " + scanner.tokens.get(0) + " in " + label);
      }
    }
    scanner.getToken(Token.ID.StreamEnd);
    events.add(new StreamEndEvent(null, null));
  }

  // document: DIRECTIVE? DOCUMENT-START node
  private void parseDocument() {
    if (scanner.checkToken(Token.ID.Directive)) {
      scanner.getToken(Token.ID.Directive);
    }
    scanner.getToken(Token.ID.DocumentStart);
    events.add(new DocumentStartEvent(true, new SpecVersion(1, 2),
        Collections.emptyMap(), null, null));
    parseNode();
    if (scanner.checkToken(Token.ID.DocumentEnd)) {
      scanner.getToken(Token.ID.DocumentEnd);
    }
    events.add(new DocumentEndEvent(true, null, null));
  }

  // node: ALIAS | ANCHOR? TAG? (SCALAR|sequence|mapping)
  private void parseNode() {
    if (scanner.checkToken(Token.ID.Alias)) {
      AliasToken token = (AliasToken) scanner.next();
      events.add(new AliasEvent(token.getValue(), null, null));
    } else {
      Anchor anchor = null;
      if (scanner.checkToken(Token.ID.Anchor)) {
        AnchorToken token = (AnchorToken) scanner.next();
        anchor = token.getValue();
      }
      String tag = null;
      if (scanner.checkToken(Token.ID.Tag)) {
        TagToken token = (TagToken) scanner.next();
        tag = token.getValue().getHandle() + token.getValue().getSuffix();
      }
      if (scanner.checkToken(Token.ID.Scalar)) {
        ScalarToken token = (ScalarToken) scanner.next();
        events.add(new ScalarEvent(anchor, tag, new ImplicitTuple(false, false), token.getValue(),
            ScalarStyle.PLAIN, null, null));
      } else if (scanner.checkToken(Token.ID.FlowSequenceStart)) {
        events.add(new SequenceStartEvent(anchor, Tag.SEQ.getValue(), false,
            FlowStyle.AUTO, null, null));
        parseSequence();
      } else if (scanner.checkToken(Token.ID.FlowMappingStart)) {
        events.add(new MappingStartEvent(anchor, Tag.MAP.getValue(), false,
            FlowStyle.AUTO, null, null));
        parseMapping();
      } else {
        throw new CanonicalException(
            "SCALAR, '[', or '{' is expected, got " + scanner.tokens.get(0));
      }
    }
  }

  // sequence: SEQUENCE-START (node (ENTRY node)*)? ENTRY? SEQUENCE-END
  private void parseSequence() {
    scanner.getToken(Token.ID.FlowSequenceStart);
    if (!scanner.checkToken(Token.ID.FlowSequenceEnd)) {
      parseNode();
      while (!scanner.checkToken(Token.ID.FlowSequenceEnd)) {
        scanner.getToken(Token.ID.FlowEntry);
        if (!scanner.checkToken(Token.ID.FlowSequenceEnd)) {
          parseNode();
        }
      }
    }
    scanner.getToken(Token.ID.FlowSequenceEnd);
    events.add(new SequenceEndEvent(null, null));
  }

  // mapping: MAPPING-START (map_entry (ENTRY map_entry)*)? ENTRY? MAPPING-END
  private void parseMapping() {
    scanner.getToken(Token.ID.FlowMappingStart);
    if (!scanner.checkToken(Token.ID.FlowMappingEnd)) {
      parseMapEntry();
      while (!scanner.checkToken(Token.ID.FlowMappingEnd)) {
        scanner.getToken(Token.ID.FlowEntry);
        if (!scanner.checkToken(Token.ID.FlowMappingEnd)) {
          parseMapEntry();
        }
      }
    }
    scanner.getToken(Token.ID.FlowMappingEnd);
    events.add(new MappingEndEvent(null, null));
  }

  // map_entry: KEY node VALUE node
  private void parseMapEntry() {
    scanner.getToken(Token.ID.Key);
    parseNode();
    scanner.getToken(Token.ID.Value);
    parseNode();
  }

  public void parse() {
    parseStream();
    parsed = true;
  }

  @NotNull
  public Event next() {
    if (!parsed) {
      parse();
    }
    return events.remove(0);
  }

  /**
   * Check the type of the next event.
   */
  public boolean checkEvent(@NotNull Event.ID choice) {
    if (!parsed) {
      parse();
    }
    if (!events.isEmpty()) {
      return events.get(0).getEventId() == choice;
    }
    return false;
  }

  /**
   * Get the next event.
   */
  @NotNull
  public Event peekEvent() {
    if (!parsed) {
      parse();
    }
    if (events.isEmpty()) {
      throw new NoSuchElementException("No more Events found.");
    } else {
      return events.get(0);
    }
  }

  @Override
  public boolean hasNext() {
    if (!parsed) {
      parse();
    }
    return !events.isEmpty();
  }
}
