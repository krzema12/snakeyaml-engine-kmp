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

import java.util.ArrayList;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.snakeyaml.engine.v2.common.Anchor;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.common.SpecVersion;
import org.snakeyaml.engine.v2.events.AliasEvent;
import org.snakeyaml.engine.v2.events.DocumentEndEvent;
import org.snakeyaml.engine.v2.events.DocumentStartEvent;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.events.ImplicitTuple;
import org.snakeyaml.engine.v2.events.MappingEndEvent;
import org.snakeyaml.engine.v2.events.MappingStartEvent;
import org.snakeyaml.engine.v2.events.ScalarEvent;
import org.snakeyaml.engine.v2.events.SequenceEndEvent;
import org.snakeyaml.engine.v2.events.SequenceStartEvent;
import org.snakeyaml.engine.v2.events.StreamEndEvent;
import org.snakeyaml.engine.v2.events.StreamStartEvent;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.parser.Parser;
import org.snakeyaml.engine.v2.tokens.AliasToken;
import org.snakeyaml.engine.v2.tokens.AnchorToken;
import org.snakeyaml.engine.v2.tokens.ScalarToken;
import org.snakeyaml.engine.v2.tokens.TagToken;
import org.snakeyaml.engine.v2.tokens.Token;

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
    events.add(new StreamStartEvent(Optional.empty(), Optional.empty()));
    while (!scanner.checkToken(Token.ID.StreamEnd)) {
      if (scanner.checkToken(Token.ID.Directive, Token.ID.DocumentStart)) {
        parseDocument();
      } else {
        throw new CanonicalException(
            "document is expected, got " + scanner.tokens.get(0) + " in " + label);
      }
    }
    scanner.getToken(Token.ID.StreamEnd);
    events.add(new StreamEndEvent(Optional.empty(), Optional.empty()));
  }

  // document: DIRECTIVE? DOCUMENT-START node
  private void parseDocument() {
    if (scanner.checkToken(Token.ID.Directive)) {
      scanner.getToken(Token.ID.Directive);
    }
    scanner.getToken(Token.ID.DocumentStart);
    events.add(new DocumentStartEvent(true, Optional.of(new SpecVersion(1, 2)),
        Collections.emptyMap(), Optional.empty(), Optional.empty()));
    parseNode();
    if (scanner.checkToken(Token.ID.DocumentEnd)) {
      scanner.getToken(Token.ID.DocumentEnd);
    }
    events.add(new DocumentEndEvent(true, Optional.empty(), Optional.empty()));
  }

  // node: ALIAS | ANCHOR? TAG? (SCALAR|sequence|mapping)
  private void parseNode() {
    if (scanner.checkToken(Token.ID.Alias)) {
      AliasToken token = (AliasToken) scanner.next();
      events.add(new AliasEvent(Optional.of(token.getValue()), Optional.empty(), Optional.empty()));
    } else {
      Optional<Anchor> anchor = Optional.empty();
      if (scanner.checkToken(Token.ID.Anchor)) {
        AnchorToken token = (AnchorToken) scanner.next();
        anchor = Optional.of(token.getValue());
      }
      Optional<String> tag = Optional.empty();
      if (scanner.checkToken(Token.ID.Tag)) {
        TagToken token = (TagToken) scanner.next();
        tag = Optional.of(token.getValue().getHandle() + token.getValue().getSuffix());
      }
      if (scanner.checkToken(Token.ID.Scalar)) {
        ScalarToken token = (ScalarToken) scanner.next();
        events.add(new ScalarEvent(anchor, tag, new ImplicitTuple(false, false), token.getValue(),
            ScalarStyle.PLAIN, Optional.empty(), Optional.empty()));
      } else if (scanner.checkToken(Token.ID.FlowSequenceStart)) {
        events.add(new SequenceStartEvent(anchor, Optional.of(Tag.SEQ.getValue()), false,
            FlowStyle.AUTO, Optional.empty(), Optional.empty()));
        parseSequence();
      } else if (scanner.checkToken(Token.ID.FlowMappingStart)) {
        events.add(new MappingStartEvent(anchor, Optional.of(Tag.MAP.getValue()), false,
            FlowStyle.AUTO, Optional.empty(), Optional.empty()));
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
    events.add(new SequenceEndEvent(Optional.empty(), Optional.empty()));
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
    events.add(new MappingEndEvent(Optional.empty(), Optional.empty()));
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
