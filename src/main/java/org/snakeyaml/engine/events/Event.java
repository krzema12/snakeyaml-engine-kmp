/**
 * Copyright (c) 2018, http://www.snakeyaml.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snakeyaml.engine.events;

import static org.snakeyaml.engine.common.CharConstants.ESCAPES;

import java.util.Map;
import java.util.stream.Collectors;

import org.snakeyaml.engine.exceptions.Mark;

/**
 * Basic unit of output from a {@link org.snakeyaml.engine.parser.Parser} or input
 * of a {@link org.snakeyaml.engine.emitter.Emitter}.
 */
public abstract class Event {
    public enum ID {
        Alias, DocumentEnd, DocumentStart, MappingEnd, MappingStart, Scalar, SequenceEnd, SequenceStart, StreamEnd, StreamStart
    }

    private static final Map<Character, Integer> ESCAPES_TO_PRINT = ESCAPES.entrySet().stream()
            .filter(entry -> entry.getKey() != '"').collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

    private final Mark startMark;
    private final Mark endMark;

    public Event(Mark startMark, Mark endMark) {
        if ((startMark == null && endMark != null) || (startMark != null && endMark == null)) {
            throw new NullPointerException("Both marks must be either present or absent.");
        }
        this.startMark = startMark;
        this.endMark = endMark;
    }

    public String toString() {
        return "<" + this.getClass().getName() + "(" + getArguments() + ")>";
    }

    public Mark getStartMark() {
        return startMark;
    }

    public Mark getEndMark() {
        return endMark;
    }

    /**
     * Generate human readable representation of the Event
     *
     * @return representation fore humans
     */
    protected String getArguments() {
        return "";
    }

    //TODO refactor to be isEvent
    public abstract boolean is(Event.ID id);

    /*
     * for tests only
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Event) {
            return toString().equals(obj.toString());
        } else {
            return false;
        }
    }

    /*
     * for tests only
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
