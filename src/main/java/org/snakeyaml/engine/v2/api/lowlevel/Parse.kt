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
package org.snakeyaml.engine.v2.api.lowlevel

import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.api.YamlUnicodeReader
import org.snakeyaml.engine.v2.events.Event
import org.snakeyaml.engine.v2.parser.ParserImpl
import org.snakeyaml.engine.v2.scanner.StreamReader
import java.io.InputStream
import java.io.Reader
import java.io.StringReader
import java.util.Objects

/**
 * Read the input stream and parse the content into events (opposite for Present or Emit)
 */
class Parse(settings: LoadSettings) {
    private val settings: LoadSettings

    /**
     * Create instance with provided [LoadSettings]
     *
     * @param settings - configuration
     */
    init {
        Objects.requireNonNull(settings, "LoadSettings cannot be null")
        this.settings = settings
    }

    /**
     * Parse a YAML stream and produce parsing events.
     *
     * @param yaml - YAML document(s). Default encoding is UTF-8. The BOM must be present if the
     * encoding is UTF-16 or UTF-32
     * @return parsed events
     * @see [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107)
     */
    fun parseInputStream(yaml: InputStream): Iterable<Event> {
        Objects.requireNonNull(yaml, "InputStream cannot be null")
        return Iterable { ParserImpl(settings, StreamReader(settings, YamlUnicodeReader(yaml))) }
    }

    /**
     * Parse a YAML stream and produce parsing events. Since the encoding is already known the BOM
     * must not be present (it will be parsed as content)
     *
     * @param yaml - YAML document(s).
     * @return parsed events
     * @see [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107)
     */
    fun parseReader(yaml: Reader): Iterable<Event> {
        Objects.requireNonNull(yaml, "Reader cannot be null")
        return Iterable { ParserImpl(settings, StreamReader(settings, yaml)) }
    }

    /**
     * Parse a YAML stream and produce parsing events.
     *
     * @param yaml - YAML document(s). The BOM must not be present (it will be parsed as content)
     * @return parsed events
     * @see [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107)
     */
    fun parseString(yaml: String): Iterable<Event> {
        Objects.requireNonNull(yaml, "String cannot be null")
        // do not use lambda to keep Iterable and Iterator visible
        return object : Iterable<Event> {
            override fun iterator(): Iterator<Event> {
                return ParserImpl(settings, StreamReader(settings, StringReader(yaml)))
            }
        }
    }
}