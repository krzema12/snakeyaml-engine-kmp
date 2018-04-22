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
package org.snakeyaml.engine.api;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

import org.snakeyaml.engine.events.Event;
import org.snakeyaml.engine.parser.Parser;
import org.snakeyaml.engine.parser.ParserImpl;
import org.snakeyaml.engine.scanner.StreamReader;

public class Parse {

    private LoadSettings settings;

    /**
     * Create
     * @param settings - configuration
     */
    public Parse(LoadSettings settings) {
        this.settings = settings;
    }

    /**
     * Parse a YAML stream and produce parsing events.
     *
     * @see <a href="http://www.yaml.org/spec/1.2/spec.html#id2762107">Processing Overview</a>
     *
     * @param yaml - YAML document(s). Since the encoding is already known the BOM must not be present
     * @return parsed events
     */
    public Iterable<Event> parseReader(InputStream yaml) {
        return new EventIterable(new ParserImpl(new StreamReader(new YamlUnicodeReader(yaml), settings)));
    }

    /**
     * Parse a YAML stream and produce parsing events.
     *
     * @see <a href="http://www.yaml.org/spec/1.2/spec.html#id2762107">Processing Overview</a>
     *
     * @param yaml - YAML document(s). Default encoding is UTF-8. The BOM must be present if the encoding is UTF-16 or UTF-32
     * @return parsed events
     */
    public Iterable<Event> parseInputStream(Reader yaml) {
        return new EventIterable(new ParserImpl(new StreamReader(yaml, settings)));
    }

    /**
     * Parse a YAML stream and produce parsing events.
     *
     * @see <a href="http://www.yaml.org/spec/1.2/spec.html#id2762107">Processing Overview</a>
     *
     * @param yaml - YAML document(s).
     * @return parsed events
     */
    public Iterable<Event> parseString(String yaml) {
        return new EventIterable(new ParserImpl(new StreamReader(new StringReader(yaml), settings)));
    }
}

class EventIterable implements Iterable<Event> {
    private final Parser parser;

    public EventIterable(Parser parser) {
        this.parser = parser;
    }

    @Override
    public Iterator<Event> iterator() {
        return new Iterator<Event>() {
            @Override
            public boolean hasNext() {
                return parser.peekEvent() != null;
            }

            public Event next() {
                return parser.getEvent();
            }
        };
    }

}
