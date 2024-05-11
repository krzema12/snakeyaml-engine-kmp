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
package it.krzeminski.snakeyaml.engine.kmp.api.lowlevel

import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.events.Event
import okio.Source
import okio.source
import java.io.InputStream
import java.io.Reader

/**
 * Read the input stream and parse the content into events (opposite for Present or Emit)
 * @param settings - configuration
 */
actual class Parse(
    private val settings: LoadSettings,
) {
    private val common = ParseCommon(settings)

    actual fun parse(string: String): Iterable<Event> = common.parse(string)

    actual fun parse(source: Source): Iterable<Event> = common.parse(source)

    /**
     * Parse a YAML stream and produce parsing events. Since the encoding is already known the BOM
     * must not be present (it will be parsed as content)
     *
     * See [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107).
     *
     * @param reader YAML document(s).
     * @return parsed events
     */
    fun parse(reader: Reader): Iterable<Event> = parse(reader.readText())

    /**
     * Parse a YAML stream and produce parsing events.
     *
     * See [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107).
     *
     * @param inputStream YAML document(s). Default encoding is UTF-8. The BOM must be present if the
     * encoding is UTF-16 or UTF-32
     * @return parsed events
     */
    fun parse(inputStream: InputStream): Iterable<Event> = parse(inputStream.source())

    @Deprecated("renamed", ReplaceWith("parse(yaml)"))
    actual fun parseString(yaml: String): Iterable<Event> = parse(yaml)

    @Deprecated("renamed", ReplaceWith("parse(yaml)"))
    fun parseInputStream(yaml: InputStream): Iterable<Event> = parse(yaml)

    @Deprecated("renamed", ReplaceWith("parse(yaml)"))
    fun parseReader(yaml: Reader): Iterable<Event> = parse(yaml)
}
