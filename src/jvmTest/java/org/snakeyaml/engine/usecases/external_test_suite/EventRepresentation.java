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
package org.snakeyaml.engine.usecases.external_test_suite;

import com.google.common.base.Splitter;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.events.*;
import org.snakeyaml.engine.v2.nodes.Tag;

import java.util.List;

/**
 * Event representation for the external test suite
 */
public class EventRepresentation {

  private final Event event;

  public EventRepresentation(Event event) {
    this.event = event;
  }

  public String getRepresentation() {
    return event.toString();
  }


  public boolean isSameAs(String data) {
    List<String> split = Splitter.on(' ').splitToList(data);
    if (!event.toString().startsWith(split.get(0))) {
      return false;
    }
    /*
     * if (event instanceof DocumentStartEvent) { DocumentStartEvent e = (DocumentStartEvent) event;
     * if (e.isExplicit()) { if (split.size() != 2 || !split.get(1).equals("---")) return false; }
     * else { if (split.size() != 1) return false; } } if (event instanceof DocumentEndEvent) {
     * DocumentEndEvent e = (DocumentEndEvent) event; if (e.isExplicit()) { if (split.size() != 2 ||
     * !split.get(1).equals("...")) return false; } else { if (split.size() != 1) return false; } }
     */
    if (event instanceof MappingStartEvent) {
      CollectionStartEvent e = (CollectionStartEvent) event;
      boolean tagIsPresent = e.getTag() != null;
      String mapTag = Tag.MAP.getValue();
      if (tagIsPresent && !mapTag.equals(e.getTag())) {
        String last = split.get(split.size() - 1);
        if (!last.equals("<" + e.getTag() + ">")) {
          return false;
        }
      }
    }
    if (event instanceof SequenceStartEvent) {
      SequenceStartEvent e = (SequenceStartEvent) event;
      if (e.getTag() != null && !Tag.SEQ.getValue().equals(e.getTag())) {
        String last = split.get(split.size() - 1);
        if (!last.equals("<" + e.getTag() + ">")) {
          return false;
        }
      }
    }
    if (event instanceof NodeEvent) {
      NodeEvent e = (NodeEvent) event;
      if (e.getAnchor() != null) {
        int indexOfAlias = 1;
        if (event.getEventId().equals(Event.ID.SequenceStart)
            || event.getEventId().equals(Event.ID.MappingStart)) {
          CollectionStartEvent start = (CollectionStartEvent) event;
          if (start.getFlowStyle() == FlowStyle.FLOW) {
            indexOfAlias = 2;
          }
        }
        if (event instanceof AliasEvent) {
          if (!split.get(indexOfAlias).startsWith("*")) {
            return false;
          }
        } else {
          if (!split.get(indexOfAlias).startsWith("&")) {
            return false;
          }
        }
      }
    }
    if (event instanceof ScalarEvent) {
      ScalarEvent e = (ScalarEvent) event;
      if (e.getTag() != null) {
        String tag = e.getTag();
        ImplicitTuple implicit = e.getImplicit();
        if (implicit.bothFalse()) {
          if (!data.contains("<" + e.getTag() + ">")) {
            return false;
          }
        }
      }
      String end = e.getScalarStyle() + e.escapedValue();
      return data.endsWith(end);
    }
    return true;
  }
}
